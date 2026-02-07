package com.example.nivelver20.ui.screens.nivelTest

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nivelver20.data.repository.*
import com.example.nivelver20.data.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Тип текущего вопроса
enum class NivelQuestionType {
    VOCABULARIO,
    GRAMMAR,
    AUDIO,
    LECTURA
}

// Состояния для карточек и ответов
enum class NivelCardState {
    NORMAL,
    SELECTED,
    SHOWING_SUCCESS,
    INCORRECT,
    MATCHED
}

// Данные для vocabulario карточек
data class NivelWordCard(
    val id: Int,
    val pairId: Int,
    val spanish: String,
    val russian: String,
    val isSpanish: Boolean,
    val state: NivelCardState = NivelCardState.NORMAL
)

// Данные для ответов (grammar, audio, lectura)
data class NivelAnswerItem(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val state: NivelCardState = NivelCardState.NORMAL
)

// Универсальный вопрос
data class NivelQuestion(
    val type: NivelQuestionType,
    val questionText: String = "",

    // Vocabulario
    val vocabWords: List<Pair<String, String>>? = null, // (spanish, russian)

    // Grammar
    val grammarType: String? = null, // multiple_choice, error_correction, drag_drop
    val grammarOptions: List<NivelAnswerItem>? = null,
    val grammarIncorrectPhrase: String? = null,
    val grammarDragDropWords: List<String>? = null,
    val grammarCorrectAnswer: String? = null,

    // Audio
    val audioUrl: String? = null,
    val audioOptions: List<NivelAnswerItem>? = null,

    // Lectura
    val lecturaText: String? = null,
    val lecturaOptions: List<NivelAnswerItem>? = null
)

data class NivelUiState(
    val nivel: String = "A1",
    val userName: String = "NOMBRE",
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 12,
    val currentQuestion: NivelQuestion? = null,

    // Vocabulario
    val spanishCards: List<NivelWordCard> = emptyList(),
    val russianCards: List<NivelWordCard> = emptyList(),
    val selectedSpanish: Int? = null,
    val selectedRussian: Int? = null,

    // Grammar/Audio/Lectura
    val answers: List<NivelAnswerItem> = emptyList(),
    val selectedAnswer: Int? = null,

    // Grammar drag-drop
    val dragDropWords: List<String> = emptyList(),
    val userDragDropAnswer: String = "",

    // Audio player
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0f,
    val currentTimeText: String = "0:00",
    val durationText: String = "0:00",

    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isChecking: Boolean = false,
    val showExitDialog: Boolean = false,
    val pendingNavigation: (() -> Unit)? = null
)

class NivelViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(NivelUiState())
    val uiState: StateFlow<NivelUiState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)

    private var allQuestions = mutableListOf<NivelQuestion>()
    private var checkingJob: Job? = null
    private var onNavigateToResults: ((String, Int, Int) -> Unit)? = null

    var mediaPlayer: android.media.MediaPlayer? = null
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
            } catch (e: Exception) {}
        }
        mediaPlayer = null
    }

    fun setResultsCallback(callback: (String, Int, Int) -> Unit) {
        onNavigateToResults = callback
    }

    fun loadNivelTest(nivel: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, nivel = nivel) }
                Log.d("NivelViewModel", "Загрузка теста для уровня: $nivel")

                allQuestions.clear()

                // 1. Vocabulario - 3 набора по 8 слов
                val vocabResult = repository.getWordsByNivel(nivel)
                if (vocabResult.isSuccess) {
                    val words = vocabResult.getOrNull() ?: emptyList()
                    if (words.size >= 24) {
                        val shuffled = words.shuffled()
                        for (i in 0 until 3) {
                            val wordsSet = shuffled.subList(i * 8, (i + 1) * 8)
                            allQuestions.add(
                                NivelQuestion(
                                    type = NivelQuestionType.VOCABULARIO,
                                    vocabWords = wordsSet.map { it.es to it.ru }
                                )
                            )
                        }
                    }
                }

                // 2. Grammar - 3 вопроса
                val grammarResult = repository.getGrammarQuestionsByNivel(nivel)
                if (grammarResult.isSuccess) {
                    val questions = grammarResult.getOrNull() ?: emptyList()
                    questions.shuffled().take(3).forEach { q ->
                        when (q.tipo) {
                            "drag_drop" -> {
                                allQuestions.add(
                                    NivelQuestion(
                                        type = NivelQuestionType.GRAMMAR,
                                        questionText = q.pregunta,
                                        grammarType = "drag_drop",
                                        grammarDragDropWords = q.palabras ?: emptyList(),
                                        grammarCorrectAnswer = q.respuestaCorrecta
                                    )
                                )
                            }
                            "error_correction" -> {
                                val shuffled = q.opciones.shuffled()
                                allQuestions.add(
                                    NivelQuestion(
                                        type = NivelQuestionType.GRAMMAR,
                                        questionText = q.pregunta,
                                        grammarType = "error_correction",
                                        grammarIncorrectPhrase = q.fraseIncorrecta,
                                        grammarOptions = shuffled.mapIndexed { idx, opt ->
                                            NivelAnswerItem(idx, opt.texto, opt.correcta)
                                        }
                                    )
                                )
                            }
                            else -> {
                                val shuffled = q.opciones.shuffled()
                                allQuestions.add(
                                    NivelQuestion(
                                        type = NivelQuestionType.GRAMMAR,
                                        questionText = q.pregunta,
                                        grammarType = "multiple_choice",
                                        grammarOptions = shuffled.mapIndexed { idx, opt ->
                                            NivelAnswerItem(idx, opt.texto, opt.correcta)
                                        }
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Audio - 3 вопроса
                val audioResult = repository.getAudioQuestionsByNivel(nivel)
                if (audioResult.isSuccess) {
                    val questions = audioResult.getOrNull() ?: emptyList()
                    questions.shuffled().take(3).forEach { q ->
                        val shuffled = q.opciones.shuffled()
                        allQuestions.add(
                            NivelQuestion(
                                type = NivelQuestionType.AUDIO,
                                questionText = q.pregunta,
                                audioUrl = q.audioUrl,
                                audioOptions = shuffled.mapIndexed { idx, opt ->
                                    NivelAnswerItem(idx, opt.texto, opt.correcta)
                                }
                            )
                        )
                    }
                }

                // 4. Lectura - 3 вопроса
                val lecturaResult = repository.getLecturaQuestionsByNivel(nivel)
                if (lecturaResult.isSuccess) {
                    val questions = lecturaResult.getOrNull() ?: emptyList()
                    questions.shuffled().take(3).forEach { q ->
                        val shuffled = q.opciones.shuffled()
                        allQuestions.add(
                            NivelQuestion(
                                type = NivelQuestionType.LECTURA,
                                questionText = q.pregunta,
                                lecturaText = q.texto,
                                lecturaOptions = shuffled.mapIndexed { idx, opt ->
                                    NivelAnswerItem(idx, opt.texto, opt.correcta)
                                }
                            )
                        )
                    }
                }

                // Перемешиваем все вопросы
                allQuestions.shuffle()

                if (allQuestions.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Нет вопросов для уровня $nivel") }
                    return@launch
                }

                _uiState.update { it.copy(isLoading = false, totalQuestions = allQuestions.size) }
                loadQuestion(0)

                Log.d("NivelViewModel", "Загружено ${allQuestions.size} вопросов")

            } catch (e: Exception) {
                Log.e("NivelViewModel", "Ошибка загрузки: ${e.message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = "Ошибка: ${e.message}") }
            }
        }
    }

    private fun loadQuestion(index: Int) {
        if (index >= allQuestions.size) {
            finishTest()
            return
        }

        val question = allQuestions[index]
        releaseMediaPlayer()

        when (question.type) {
            NivelQuestionType.VOCABULARIO -> loadVocabQuestion(question, index)
            NivelQuestionType.GRAMMAR -> loadGrammarQuestion(question, index)
            NivelQuestionType.AUDIO -> loadAudioQuestion(question, index)
            NivelQuestionType.LECTURA -> loadLecturaQuestion(question, index)
        }
    }

    private fun loadVocabQuestion(question: NivelQuestion, index: Int) {
        val words = question.vocabWords ?: emptyList()

        val spanishCards = words.mapIndexed { idx, (es, ru) ->
            NivelWordCard(idx, idx, es, ru, true, NivelCardState.NORMAL)
        }

        val russianCards = words.mapIndexed { idx, (es, ru) ->
            NivelWordCard(idx + 100, idx, es, ru, false, NivelCardState.NORMAL)
        }.shuffled()

        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = question,
                spanishCards = spanishCards,
                russianCards = russianCards,
                selectedSpanish = null,
                selectedRussian = null,
                answers = emptyList(),
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }
    }

    private fun loadGrammarQuestion(question: NivelQuestion, index: Int) {
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = question,
                spanishCards = emptyList(),
                russianCards = emptyList(),
                answers = question.grammarOptions ?: emptyList(),
                dragDropWords = question.grammarDragDropWords ?: emptyList(),
                userDragDropAnswer = "",
                selectedAnswer = null
            )
        }
    }

    private fun loadAudioQuestion(question: NivelQuestion, index: Int) {
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = question,
                spanishCards = emptyList(),
                russianCards = emptyList(),
                answers = question.audioOptions ?: emptyList(),
                selectedAnswer = null,
                isPlaying = false,
                currentPosition = 0f,
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }

        question.audioUrl?.let { prepareMediaPlayer(it) }
    }

    private fun loadLecturaQuestion(question: NivelQuestion, index: Int) {
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = question,
                spanishCards = emptyList(),
                russianCards = emptyList(),
                answers = question.lecturaOptions ?: emptyList(),
                selectedAnswer = null,
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }
    }

    // VOCABULARIO
    fun onSpanishCardClick(index: Int) {
        if (_uiState.value.isChecking) return
        val card = _uiState.value.spanishCards.getOrNull(index) ?: return
        if (card.state == NivelCardState.MATCHED) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        when {
                            it.state == NivelCardState.INCORRECT -> it.copy(state = NivelCardState.NORMAL)
                            it.state == NivelCardState.SELECTED -> it.copy(state = NivelCardState.NORMAL)
                            it.id == card.id -> it.copy(state = NivelCardState.SELECTED)
                            else -> it
                        }
                    },
                    russianCards = state.russianCards.map {
                        if (it.state == NivelCardState.INCORRECT) it.copy(state = NivelCardState.NORMAL) else it
                    },
                    selectedSpanish = card.id
                )
            }
            checkVocabMatch()
        }
    }

    fun onRussianCardClick(index: Int) {
        if (_uiState.value.isChecking) return
        val card = _uiState.value.russianCards.getOrNull(index) ?: return
        if (card.state == NivelCardState.MATCHED) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    russianCards = state.russianCards.map {
                        when {
                            it.state == NivelCardState.INCORRECT -> it.copy(state = NivelCardState.NORMAL)
                            it.state == NivelCardState.SELECTED -> it.copy(state = NivelCardState.NORMAL)
                            it.id == card.id -> it.copy(state = NivelCardState.SELECTED)
                            else -> it
                        }
                    },
                    spanishCards = state.spanishCards.map {
                        if (it.state == NivelCardState.INCORRECT) it.copy(state = NivelCardState.NORMAL) else it
                    },
                    selectedRussian = card.id
                )
            }
            checkVocabMatch()
        }
    }

    private suspend fun checkVocabMatch() {
        val selectedSpanishId = _uiState.value.selectedSpanish ?: return
        val selectedRussianId = _uiState.value.selectedRussian ?: return

        val spanishCard = _uiState.value.spanishCards.find { it.id == selectedSpanishId } ?: return
        val russianCard = _uiState.value.russianCards.find { it.id == selectedRussianId } ?: return

        val isCorrect = spanishCard.pairId == russianCard.pairId

        if (isCorrect) {
            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        if (it.id == selectedSpanishId) it.copy(state = NivelCardState.SHOWING_SUCCESS) else it
                    },
                    russianCards = state.russianCards.map {
                        if (it.id == selectedRussianId) it.copy(state = NivelCardState.SHOWING_SUCCESS) else it
                    },
                    correctCount = state.correctCount + 1,
                    selectedSpanish = null,
                    selectedRussian = null
                )
            }

            viewModelScope.launch {
                delay(400)
                _uiState.update { state ->
                    state.copy(
                        spanishCards = state.spanishCards.map {
                            if (it.id == selectedSpanishId) it.copy(state = NivelCardState.MATCHED) else it
                        },
                        russianCards = state.russianCards.map {
                            if (it.id == selectedRussianId) it.copy(state = NivelCardState.MATCHED) else it
                        }
                    )
                }
            }

            checkVocabComplete()
        } else {
            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        if (it.id == selectedSpanishId) it.copy(state = NivelCardState.INCORRECT) else it
                    },
                    russianCards = state.russianCards.map {
                        if (it.id == selectedRussianId) it.copy(state = NivelCardState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            delay(400)

            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        if (it.id == selectedSpanishId) it.copy(state = NivelCardState.NORMAL) else it
                    },
                    russianCards = state.russianCards.map {
                        if (it.id == selectedRussianId) it.copy(state = NivelCardState.NORMAL) else it
                    },
                    selectedSpanish = null,
                    selectedRussian = null
                )
            }
        }
    }

    private suspend fun checkVocabComplete() {
        val allMatched = _uiState.value.spanishCards.all {
            it.state == NivelCardState.MATCHED || it.state == NivelCardState.SHOWING_SUCCESS
        }

        if (allMatched) {
            delay(1500)
            loadQuestion(_uiState.value.currentQuestionIndex + 1)
        }
    }

    // GRAMMAR / AUDIO / LECTURA
    fun onAnswerClick(index: Int) {
        if (_uiState.value.isChecking) return
        val answer = _uiState.value.answers.getOrNull(index) ?: return
        if (answer.state == NivelCardState.MATCHED) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        when {
                            it.state == NivelCardState.INCORRECT -> it.copy(state = NivelCardState.NORMAL)
                            it.state == NivelCardState.SELECTED -> it.copy(state = NivelCardState.NORMAL)
                            it.id == answer.id -> it.copy(state = NivelCardState.SELECTED)
                            else -> it
                        }
                    },
                    selectedAnswer = answer.id
                )
            }

            checkAnswer()
        }
    }

    private suspend fun checkAnswer() {
        val selectedId = _uiState.value.selectedAnswer ?: return
        val answer = _uiState.value.answers.find { it.id == selectedId } ?: return

        if (answer.isCorrect) {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = NivelCardState.SHOWING_SUCCESS) else it
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
                            if (it.id == selectedId) it.copy(state = NivelCardState.MATCHED) else it
                        }
                    )
                }
            }

            delay(1500)
            loadQuestion(_uiState.value.currentQuestionIndex + 1)
        } else {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = NivelCardState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            delay(400)

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = NivelCardState.NORMAL) else it
                    },
                    selectedAnswer = null
                )
            }
        }
    }

    // GRAMMAR DRAG-DROP
    fun onDragDropWordClick(word: String) {
        val current = _uiState.value.userDragDropAnswer
        _uiState.update {
            it.copy(userDragDropAnswer = if (current.isEmpty()) word else "$current $word")
        }
    }

    fun onDragDropClear() {
        _uiState.update { it.copy(userDragDropAnswer = "") }
    }

    fun onDragDropSubmit() {
        viewModelScope.launch {
            val userAnswer = _uiState.value.userDragDropAnswer.trim()
            val correctAnswer = _uiState.value.currentQuestion?.grammarCorrectAnswer?.trim() ?: ""
            val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)

            _uiState.update {
                it.copy(
                    correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                    incorrectCount = if (!isCorrect) it.incorrectCount + 1 else it.incorrectCount
                )
            }

            delay(1000)
            loadQuestion(_uiState.value.currentQuestionIndex + 1)
        }
    }

    // AUDIO PLAYER
    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        try {
            if (_uiState.value.isPlaying) {
                player.pause()
                progressUpdateJob?.cancel()
                _uiState.update { it.copy(isPlaying = false) }
            } else {
                if (_uiState.value.currentPosition >= 0.99f) {
                    player.seekTo(0)
                    _uiState.update { it.copy(currentPosition = 0f, currentTimeText = "0:00") }
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
        if (duration > 0) {
            val position = (_uiState.value.currentPosition * duration).toInt()
            player.seekTo(position)
            _uiState.update { it.copy(currentTimeText = formatTime(position)) }
        }
    }

    private fun prepareMediaPlayer(audioUrl: String) {
        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener { player ->
                    _uiState.update {
                        it.copy(
                            durationText = formatTime(player.duration),
                            currentTimeText = "0:00",
                            currentPosition = 0f
                        )
                    }
                }
                setOnCompletionListener {
                    progressUpdateJob?.cancel()
                    _uiState.update {
                        it.copy(isPlaying = false, currentPosition = 1f, currentTimeText = it.durationText)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _uiState.update { it.copy(isPlaying = false) }
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {}
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
                                _uiState.update {
                                    it.copy(
                                        currentPosition = position.toFloat() / duration,
                                        currentTimeText = formatTime(position)
                                    )
                                }
                            }
                        } catch (e: Exception) {}
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

    private fun finishTest() {
        val nivel = _uiState.value.nivel
        val correct = _uiState.value.correctCount
        val incorrect = _uiState.value.incorrectCount

        sessionManager.saveNivelResult(nivel, correct, incorrect)

        viewModelScope.launch {
            delay(500)
            onNavigateToResults?.invoke(nivel, correct, incorrect)
        }
    }

    fun requestExit(onConfirm: () -> Unit) {
        _uiState.update { it.copy(showExitDialog = true, pendingNavigation = onConfirm) }
    }

    fun confirmExit() {
        val pendingNav = _uiState.value.pendingNavigation
        _uiState.update { it.copy(showExitDialog = false, pendingNavigation = null) }
        pendingNav?.invoke()
    }

    fun cancelExit() {
        _uiState.update { it.copy(showExitDialog = false, pendingNavigation = null) }
    }
}