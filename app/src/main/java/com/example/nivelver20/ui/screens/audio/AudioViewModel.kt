package com.example.nivelver20.ui.screens.audio

import android.app.Application
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
    val currentTimeText: String = "0:00",
    val durationText: String = "0:00",
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
                // Silently handle
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

                allAvailableQuestions = questions.shuffled()
                usedQuestionsStartIndex = 0

                loadNextQuestion()

                _uiState.update { it.copy(isLoading = false, nivel = nivel) }
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

            if (currentRound < totalRounds) {
                delay(1500)
                _uiState.update { it.copy(currentRound = currentRound + 1) }
                loadNextQuestion()
            } else {
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
        val player = mediaPlayer ?: return

        try {
            val duration = player.duration
            if (duration <= 0) return

            if (_uiState.value.isPlaying) {
                player.pause()
                progressUpdateJob?.cancel()
                _uiState.update { it.copy(isPlaying = false) }
            } else {
                if (_uiState.value.currentPosition >= 0.99f) {
                    player.seekTo(0)
                    _uiState.update {
                        it.copy(
                            currentPosition = 0f,
                            currentTimeText = "0:00"
                        )
                    }
                }

                _uiState.update { it.copy(isPlaying = true) }
                player.start()
                startProgressUpdates()
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    fun onSliderValueChange(value: Float) {
        _uiState.update { it.copy(currentPosition = value) }
    }

    fun onSliderValueChangeFinished() {
        val player = mediaPlayer ?: return
        val duration = player.duration
        val value = _uiState.value.currentPosition

        if (duration > 0) {
            val position = (value * duration).toInt()
            player.seekTo(position)

            _uiState.update {
                it.copy(
                    currentTimeText = formatTime(position)
                )
            }
        }
    }

    private fun prepareMediaPlayer(audioUrl: String) {
        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(audioUrl)

                setOnPreparedListener { player ->
                    val duration = player.duration

                    if (duration > 0) {
                        _uiState.update {
                            it.copy(
                                durationText = formatTime(duration),
                                currentTimeText = "0:00",
                                currentPosition = 0f
                            )
                        }
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
                }

                setOnErrorListener { _, _, _ ->
                    _uiState.update { it.copy(isPlaying = false) }
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
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