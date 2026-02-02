package com.example.nivelver20.ui.screens.audio

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nivelver20.data.repository.FirestoreRepository
import com.example.nivelver20.data.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AudioAnswerState {
    NORMAL,
    SELECTED,
    SHOWING_SUCCESS,
    INCORRECT,
    MATCHED
}

data class AudioQuestion(
    val audioUrl: String,
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)

data class AudioAnswerItem(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val state: AudioAnswerState = AudioAnswerState.NORMAL
)

data class AudioUiState(
    val nivelLabel: String = "NIVEL",
    val nivel: String = "A1",
    val userName: String = "NOMBRE",
    val title: String = "AUDIO",
    val audioUrl: String = "",
    val question: String = "",
    val answers: List<AudioAnswerItem> = emptyList(),
    val selectedAnswer: Int? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0f,
    val currentTimeText: String = "0:00", // Текущее время (0:15)
    val durationText: String = "0:00",     // Длительность (1:23)
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val testButton: String = "TEST",
    val perfilButton: String = "PERFIL",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentRound: Int = 1,
    val totalRounds: Int = 7,
    val isChecking: Boolean = false,
    val showExitDialog: Boolean = false,
    val pendingNavigation: (() -> Unit)? = null
)

class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)

    private var allAvailableQuestions: List<AudioQuestion> = emptyList()
    private var usedQuestionsStartIndex = 0
    private var checkingJob: Job? = null
    private var onNavigateToResults: ((String, Int, Int) -> Unit)? = null

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var progressUpdateJob: Job? = null

    init {
        val username = sessionManager.getCurrentUser()
        if (username != null) {
            _uiState.update { it.copy(userName = username) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null

        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                reset()
                release()
            } catch (e: Exception) {
                Log.e("AudioVM", "Error releasing MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null
    }

    fun setResultsCallback(callback: (String, Int, Int) -> Unit) {
        onNavigateToResults = callback
    }

    private fun loadAllQuestionsForNivel(nivel: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getAudioQuestionsByNivel(nivel)

            if (result.isSuccess) {
                val firestoreQuestions = result.getOrNull() ?: emptyList()

                if (firestoreQuestions.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No hay preguntas para este nivel"
                        )
                    }
                    return@launch
                }

                val questions = firestoreQuestions.map { firestoreQuestion ->
                    val correctIndex = firestoreQuestion.opciones.indexOfFirst { it.correcta }

                    AudioQuestion(
                        audioUrl = firestoreQuestion.audioUrl,
                        question = firestoreQuestion.pregunta,
                        answers = firestoreQuestion.opciones.map { it.texto },
                        correctAnswerIndex = correctIndex
                    )
                }

                if (questions.size < 7) {
                    Log.w("AudioVM", "Недостаточно вопросов: ${questions.size}")
                }

                allAvailableQuestions = questions.shuffled()
                usedQuestionsStartIndex = 0

                loadNextQuestion()

                _uiState.update { it.copy(isLoading = false, nivel = nivel) }

                Log.d("AudioVM", "Loaded ${questions.size} questions")
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar preguntas"
                    )
                }
            }
        }
    }

    private fun loadNextQuestion() {
        if (allAvailableQuestions.isEmpty()) {
            Log.e("AudioVM", "No questions available")
            return
        }

        val questionIndex = usedQuestionsStartIndex % allAvailableQuestions.size
        val question = allAvailableQuestions[questionIndex]
        usedQuestionsStartIndex++

        val answersWithOriginalIndex = question.answers.mapIndexed { index, text ->
            Triple(index, text, index == question.correctAnswerIndex)
        }.shuffled()

        val answerItems = answersWithOriginalIndex.mapIndexed { newIndex, (_, text, isCorrect) ->
            AudioAnswerItem(
                id = newIndex,
                text = text,
                isCorrect = isCorrect,
                state = AudioAnswerState.NORMAL
            )
        }

        releaseMediaPlayer()

        _uiState.update {
            it.copy(
                audioUrl = question.audioUrl,
                question = question.question,
                answers = answerItems,
                selectedAnswer = null,
                isChecking = false,
                isPlaying = false,
                currentPosition = 0f,
                currentTimeText = "0:00",
                durationText = "0:00"
            )
        }

        prepareMediaPlayer(question.audioUrl)

        Log.d("AudioVM", "Loaded round ${_uiState.value.currentRound}/${_uiState.value.totalRounds}")
    }

    fun loadQuestions(nivel: String) {
        _uiState.update {
            it.copy(
                correctCount = 0,
                incorrectCount = 0,
                currentRound = 1
            )
        }
        usedQuestionsStartIndex = 0
        loadAllQuestionsForNivel(nivel)
    }

    fun onAnswerClick(index: Int) {
        if (_uiState.value.isChecking) return

        val answer = _uiState.value.answers.getOrNull(index) ?: return

        if (answer.state == AudioAnswerState.MATCHED) return

        if (!_uiState.value.isChecking) {
            checkingJob?.cancel()
            checkingJob = null
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.state == AudioAnswerState.INCORRECT)
                            it.copy(state = AudioAnswerState.NORMAL)
                        else it
                    }
                )
            }

            _uiState.update { state ->
                val updatedAnswers = state.answers.map {
                    if (it.state == AudioAnswerState.SELECTED) it.copy(state = AudioAnswerState.NORMAL)
                    else it
                }
                state.copy(
                    answers = updatedAnswers,
                    selectedAnswer = null
                )
            }

            _uiState.update { state ->
                val updatedAnswers = state.answers.toMutableList()
                updatedAnswers[index] = updatedAnswers[index].copy(state = AudioAnswerState.SELECTED)
                state.copy(
                    answers = updatedAnswers,
                    selectedAnswer = answer.id
                )
            }

            checkAnswer()
        }
    }

    private suspend fun checkAnswer() {
        val selectedId = _uiState.value.selectedAnswer ?: return
        val selectedAnswer = _uiState.value.answers.find { it.id == selectedId } ?: return

        val isCorrect = selectedAnswer.isCorrect

        if (isCorrect) {
            Log.d("AudioVM", "Correct answer!")

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AudioAnswerState.SHOWING_SUCCESS) else it
                    },
                    correctCount = state.correctCount + 1,
                    selectedAnswer = null
                )
            }

            viewModelScope.launch {
                delay(400)

                _uiState.update { state ->
                    state.copy(
                        answers = state.answers.map {
                            if (it.id == selectedId) it.copy(state = AudioAnswerState.MATCHED) else it
                        }
                    )
                }
            }

            checkIfTestComplete()

        } else {
            Log.d("AudioVM", "Incorrect answer!")

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AudioAnswerState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            delay(400)

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AudioAnswerState.NORMAL) else it
                    },
                    selectedAnswer = null
                )
            }
        }
    }

    private suspend fun checkIfTestComplete() {
        val allMatched = _uiState.value.answers.all {
            it.state == AudioAnswerState.MATCHED || it.state == AudioAnswerState.SHOWING_SUCCESS || !it.isCorrect
        }

        if (allMatched) {
            val currentRound = _uiState.value.currentRound
            val totalRounds = _uiState.value.totalRounds

            Log.d("AudioVM", "Round $currentRound completed!")

            if (currentRound < totalRounds) {
                delay(1500)
                _uiState.update { it.copy(currentRound = currentRound + 1) }
                loadNextQuestion()
                Log.d("AudioVM", "Starting round ${currentRound + 1}")
            } else {
                Log.d("AudioVM", "All rounds completed!")
                delay(500)
                onNavigateToResults?.invoke(
                    _uiState.value.nivel,
                    _uiState.value.correctCount,
                    _uiState.value.incorrectCount
                )
            }
        }
    }

    fun setNivel(nivel: String) {
        _uiState.update { it.copy(nivel = nivel) }
        loadQuestions(nivel)
    }

    fun requestExit(onConfirm: () -> Unit) {
        _uiState.update {
            it.copy(
                showExitDialog = true,
                pendingNavigation = onConfirm
            )
        }
    }

    fun confirmExit() {
        val pendingNav = _uiState.value.pendingNavigation
        _uiState.update {
            it.copy(
                showExitDialog = false,
                pendingNavigation = null
            )
        }
        pendingNav?.invoke()
    }

    fun cancelExit() {
        _uiState.update {
            it.copy(
                showExitDialog = false,
                pendingNavigation = null
            )
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            Log.e("AudioVM", "MediaPlayer is NULL!")
            return
        }

        try {
            // Проверяем, что MediaPlayer готов
            val duration = player.duration
            if (duration <= 0) {
                Log.e("AudioVM", "MediaPlayer not ready yet! Duration: $duration")
                return
            }

            if (_uiState.value.isPlaying) {
                // ПАУЗА
                player.pause()
                progressUpdateJob?.cancel()
                _uiState.update { it.copy(isPlaying = false) }
                Log.d("AudioVM", "⏸️ Paused at ${_uiState.value.currentTimeText}")
            } else {
                // PLAY
                if (_uiState.value.currentPosition >= 0.99f) {
                    player.seekTo(0)
                    _uiState.update {
                        it.copy(
                            currentPosition = 0f,
                            currentTimeText = "0:00"
                        )
                    }
                    Log.d("AudioVM", "Restarting from beginning")
                }

                player.start()
                startProgressUpdates()
                _uiState.update { it.copy(isPlaying = true) }
                Log.d("AudioVM", "Playing from ${_uiState.value.currentTimeText}")
            }
        } catch (e: Exception) {
            Log.e("AudioVM", "Error in togglePlayPause: ${e.message}")
            e.printStackTrace()
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    fun onSliderValueChange(value: Float) {
        // Когда пользователь двигает слайдер - обновляем только UI
        _uiState.update { it.copy(currentPosition = value) }
    }

    fun onSliderValueChangeFinished() {
        // Когда пользователь отпустил слайдер - перематываем аудио
        val player = mediaPlayer ?: return
        val duration = player.duration
        val value = _uiState.value.currentPosition

        if (duration > 0) {
            val position = (value * duration).toInt()
            player.seekTo(position)

            // Обновляем время
            _uiState.update {
                it.copy(
                    currentTimeText = formatTime(position)
                )
            }

            Log.d("AudioVM", "Seeked to: ${formatTime(position)}")
        }
    }

    private fun prepareMediaPlayer(audioUrl: String) {
        Log.d("AudioVM", "Preparing media player for URL: $audioUrl")

        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(audioUrl)

                setOnPreparedListener { player ->
                    val duration = player.duration

                    // ВАЖНО: Обновляем UI только если длительность > 0
                    if (duration > 0) {
                        _uiState.update {
                            it.copy(
                                durationText = formatTime(duration),
                                currentTimeText = "0:00",
                                currentPosition = 0f
                            )
                        }
                        Log.d("AudioVM", "MediaPlayer ready! Duration: ${formatTime(duration)}")
                    } else {
                        Log.e("AudioVM", "Duration is 0!")
                    }
                }

                setOnCompletionListener {
                    progressUpdateJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isPlaying = false,
                            currentPosition = 1f,
                            currentTimeText = it.durationText
                        )
                    }
                    Log.d("AudioVM", "Audio completed")
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e("AudioVM", "MediaPlayer ERROR! what=$what, extra=$extra")
                    _uiState.update { it.copy(isPlaying = false) }
                    true
                }

                prepareAsync()
                Log.d("AudioVM", "prepareAsync() called, waiting for onPrepared...")
            }
        } catch (e: Exception) {
            Log.e("AudioVM", "Exception preparing MediaPlayer: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        try {
                            val duration = player.duration
                            val position = player.currentPosition

                            if (duration > 0) {
                                val progress = position.toFloat() / duration.toFloat()
                                _uiState.update {
                                    it.copy(
                                        currentPosition = progress,
                                        currentTimeText = formatTime(position),
                                        durationText = formatTime(duration)
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("AudioVM", "Error updating progress: ${e.message}")
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}