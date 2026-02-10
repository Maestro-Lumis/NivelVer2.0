package com.example.nivelver20.ui.screens.flujoTest

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

// ========== ENUMS & DATA CLASSES ==========

enum class FlujoQuestionType {
    VOCABULARIO,
    GRAMMAR,
    AUDIO,
    LECTURA
}

enum class FlujoCardState {
    NORMAL,
    SELECTED,
    SHOWING_SUCCESS,
    INCORRECT,
    MATCHED
}

// Vocabulario
data class FlujoWord(
    val espanol: String,
    val ruso: String,
    val nivel: String
)

data class FlujoWordCard(
    val id: Int,
    val pairId: Int,
    val spanish: String,
    val russian: String,
    val isSpanish: Boolean,
    val state: FlujoCardState = FlujoCardState.NORMAL
)

// Grammar
data class FlujoGrammarQuestion(
    val nivel: String,
    val tipo: String, // multiple_choice, error_correction, drag_drop
    val pregunta: String,
    val opciones: List<FlujoGrammarOpcion>,
    val fraseIncorrecta: String? = null,
    val errorPalabra: String? = null,
    val indicePalabra: Int? = null,
    val palabras: List<String>? = null,
    val respuestaCorrecta: String? = null,
    val explicacion: String? = null
)

data class FlujoGrammarOpcion(
    val texto: String,
    val correcta: Boolean
)

// Audio
data class FlujoAudioQuestion(
    val audioUrl: String,
    val pregunta: String,
    val opciones: List<FlujoAudioOpcion>,
    val nivel: String
)

data class FlujoAudioOpcion(
    val texto: String,
    val correcta: Boolean
)

// Lectura
data class FlujoLecturaQuestion(
    val pregunta: String,
    val texto: String,
    val opciones: List<FlujoLecturaOpcion>,
    val nivel: String
)

data class FlujoLecturaOpcion(
    val texto: String,
    val correcta: Boolean
)

// Answer item (for Grammar, Audio, Lectura)
data class FlujoAnswerItem(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val state: FlujoCardState = FlujoCardState.NORMAL
)

// Sealed class for different question types
sealed class FlujoQuestion {
    data class Vocabulario(val word: FlujoWord) : FlujoQuestion()
    data class Grammar(val question: FlujoGrammarQuestion) : FlujoQuestion()
    data class Audio(val question: FlujoAudioQuestion) : FlujoQuestion()
    data class Lectura(val question: FlujoLecturaQuestion) : FlujoQuestion()
}

// Level result
data class FlujoLevelResult(
    val nivel: String,
    val correctas: Int,
    val total: Int,
    val aprobado: Boolean
)

// UI State
data class FlujoState(
    val isLoading: Boolean = false,
    val currentLevel: String = "A1",
    val currentQuestionIndex: Int = 0,
    val currentQuestion: FlujoQuestion? = null,
    val answeredInLevel: Int = 0,
    val correctInLevel: Int = 0,
    val incorrectInLevel: Int = 0,
    val levelResults: Map<String, FlujoLevelResult> = emptyMap(),
    val showLevelCompleteDialog: Boolean = false,
    val showStopDialog: Boolean = false,
    val isTestComplete: Boolean = false,
    val error: String? = null,
    val questionTypes: List<FlujoQuestionType> = emptyList(),

    // Vocabulario
    val spanishCards: List<FlujoWordCard> = emptyList(),
    val russianCards: List<FlujoWordCard> = emptyList(),
    val selectedSpanish: Int? = null,
    val selectedRussian: Int? = null,

    // Grammar/Audio/Lectura
    val answers: List<FlujoAnswerItem> = emptyList(),
    val selectedAnswer: Int? = null,

    // Grammar drag-drop
    val dragDropWords: List<String> = emptyList(),
    val userDragDropAnswer: String = "",

    // Audio player
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0f,
    val currentTimeText: String = "0:00",
    val durationText: String = "0:00",

    // Exit dialog
    val showExitDialog: Boolean = false,
    val pendingNavigation: (() -> Unit)? = null
)

// ========== VIEW MODEL ==========

class FlujoViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FlujoState())
    val uiState: StateFlow<FlujoState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)

    private val levelSequence = listOf("A1", "A2", "B1", "B2")
    private var currentLevelQuestions = mutableListOf<FlujoQuestion>()
    private var checkingJob: Job? = null

    var mediaPlayer: android.media.MediaPlayer? = null
    private var progressUpdateJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
        saveResults()
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

    // ========== TEST FLOW ==========

    fun startTest() {
        _uiState.update { it.copy(currentLevel = "A1") }
        cargarPreguntasNivel("A1")
    }

    private fun cargarPreguntasNivel(nivel: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d("FlujoVM", "Loading questions for level: $nivel")

                currentLevelQuestions.clear()

                // Load 1 question of each type (4 total per level)
                val questionTypes = FlujoQuestionType.values().toList().shuffled()

                questionTypes.forEach { type ->
                    when (type) {
                        FlujoQuestionType.VOCABULARIO -> loadVocabularioQuestion(nivel)
                        FlujoQuestionType.GRAMMAR -> loadGrammarQuestion(nivel)
                        FlujoQuestionType.AUDIO -> loadAudioQuestion(nivel)
                        FlujoQuestionType.LECTURA -> loadLecturaQuestion(nivel)
                    }
                }

                if (currentLevelQuestions.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No hay preguntas para nivel $nivel"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentLevel = nivel,
                        currentQuestionIndex = 0,
                        answeredInLevel = 0,
                        correctInLevel = 0,
                        incorrectInLevel = 0,
                        questionTypes = questionTypes
                    )
                }

                loadQuestion(0)

                Log.d("FlujoVM", "Loaded ${currentLevelQuestions.size} questions for $nivel")

            } catch (e: Exception) {
                Log.e("FlujoVM", "Error loading questions: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun loadVocabularioQuestion(nivel: String) {
        val result = repository.getWordsByNivel(nivel)
        if (result.isSuccess) {
            val words = result.getOrNull() ?: emptyList()
            if (words.size >= 8) {
                val selectedWords = words.shuffled().take(8)
                val word = FlujoWord(
                    espanol = selectedWords[0].es,
                    ruso = selectedWords[0].ru,
                    nivel = nivel
                )
                currentLevelQuestions.add(FlujoQuestion.Vocabulario(word))
            }
        }
    }

    private suspend fun loadGrammarQuestion(nivel: String) {
        val result = repository.getGrammarQuestionsByNivel(nivel)
        if (result.isSuccess) {
            val questions = result.getOrNull() ?: emptyList()
            questions.shuffled().firstOrNull()?.let { q ->
                val grammarQuestion = FlujoGrammarQuestion(
                    nivel = q.nivel,
                    tipo = q.tipo,
                    pregunta = q.pregunta,
                    opciones = q.opciones.map { FlujoGrammarOpcion(it.texto, it.correcta) },
                    fraseIncorrecta = q.fraseIncorrecta,
                    errorPalabra = q.errorPalabra,
                    indicePalabra = q.indicePalabra,
                    palabras = q.palabras,
                    respuestaCorrecta = q.respuestaCorrecta,
                    explicacion = q.explicacion
                )
                currentLevelQuestions.add(FlujoQuestion.Grammar(grammarQuestion))
            }
        }
    }

    private suspend fun loadAudioQuestion(nivel: String) {
        val result = repository.getAudioQuestionsByNivel(nivel)
        if (result.isSuccess) {
            val questions = result.getOrNull() ?: emptyList()
            questions.shuffled().firstOrNull()?.let { q ->
                val audioQuestion = FlujoAudioQuestion(
                    audioUrl = q.audioUrl,
                    pregunta = q.pregunta,
                    opciones = q.opciones.map { FlujoAudioOpcion(it.texto, it.correcta) },
                    nivel = nivel
                )
                currentLevelQuestions.add(FlujoQuestion.Audio(audioQuestion))
            }
        }
    }

    private suspend fun loadLecturaQuestion(nivel: String) {
        val result = repository.getLecturaQuestionsByNivel(nivel)
        if (result.isSuccess) {
            val questions = result.getOrNull() ?: emptyList()
            questions.shuffled().firstOrNull()?.let { q ->
                val lecturaQuestion = FlujoLecturaQuestion(
                    pregunta = q.pregunta,
                    texto = q.texto,
                    opciones = q.opciones.map { FlujoLecturaOpcion(it.texto, it.correcta) },
                    nivel = nivel
                )
                currentLevelQuestions.add(FlujoQuestion.Lectura(lecturaQuestion))
            }
        }
    }

    private fun loadQuestion(index: Int) {
        if (index >= currentLevelQuestions.size) {
            // Level complete
            terminarNivel(_uiState.value.correctInLevel)
            return
        }

        val question = currentLevelQuestions[index]
        releaseMediaPlayer()

        when (question) {
            is FlujoQuestion.Vocabulario -> loadVocabUI(question.word, index)
            is FlujoQuestion.Grammar -> loadGrammarUI(question.question, index)
            is FlujoQuestion.Audio -> loadAudioUI(question.question, index)
            is FlujoQuestion.Lectura -> loadLecturaUI(question.question, index)
        }
    }

    private fun loadVocabUI(word: FlujoWord, index: Int) {
        viewModelScope.launch {
            val result = repository.getWordsByNivel(word.nivel)
            if (result.isSuccess) {
                val words = result.getOrNull() ?: emptyList()
                if (words.size >= 8) {
                    val selectedWords = words.shuffled().take(8)

                    val spanishCards = selectedWords.mapIndexed { idx, w ->
                        FlujoWordCard(idx, idx, w.es, w.ru, true, FlujoCardState.NORMAL)
                    }

                    val russianCards = selectedWords.mapIndexed { idx, w ->
                        FlujoWordCard(idx + 100, idx, w.es, w.ru, false, FlujoCardState.NORMAL)
                    }.shuffled()

                    _uiState.update {
                        it.copy(
                            currentQuestionIndex = index,
                            currentQuestion = FlujoQuestion.Vocabulario(word),
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
            }
        }
    }

    private fun loadGrammarUI(question: FlujoGrammarQuestion, index: Int) {
        val shuffledOptions = question.opciones.shuffled()
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = FlujoQuestion.Grammar(question),
                spanishCards = emptyList(),
                russianCards = emptyList(),
                answers = shuffledOptions.mapIndexed { idx, opt ->
                    FlujoAnswerItem(idx, opt.texto, opt.correcta)
                },
                dragDropWords = question.palabras?.shuffled() ?: emptyList(),
                userDragDropAnswer = "",
                selectedAnswer = null
            )
        }
    }

    private fun loadAudioUI(question: FlujoAudioQuestion, index: Int) {
        val shuffledOptions = question.opciones.shuffled()
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = FlujoQuestion.Audio(question),
                spanishCards = emptyList(),
                russianCards = emptyList(),
                answers = shuffledOptions.mapIndexed { idx, opt ->
                    FlujoAnswerItem(idx, opt.texto, opt.correcta)
                },
                selectedAnswer = null,
                isPlaying = false,
                currentPosition = 0f,
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }

        prepareMediaPlayer(question.audioUrl)
    }

    private fun loadLecturaUI(question: FlujoLecturaQuestion, index: Int) {
        val shuffledOptions = question.opciones.shuffled()
        _uiState.update {
            it.copy(
                currentQuestionIndex = index,
                currentQuestion = FlujoQuestion.Lectura(question),
                spanishCards = emptyList(),
                russianCards = emptyList(),
                answers = shuffledOptions.mapIndexed { idx, opt ->
                    FlujoAnswerItem(idx, opt.texto, opt.correcta)
                },
                selectedAnswer = null,
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }
    }

    // ========== VOCABULARIO ==========

    fun onSpanishCardClick(index: Int) {
        val card = _uiState.value.spanishCards.getOrNull(index) ?: return
        if (card.state == FlujoCardState.MATCHED) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        when {
                            it.state == FlujoCardState.INCORRECT -> it.copy(state = FlujoCardState.NORMAL)
                            it.state == FlujoCardState.SELECTED -> it.copy(state = FlujoCardState.NORMAL)
                            it.id == card.id -> it.copy(state = FlujoCardState.SELECTED)
                            else -> it
                        }
                    },
                    russianCards = state.russianCards.map {
                        if (it.state == FlujoCardState.INCORRECT) it.copy(state = FlujoCardState.NORMAL) else it
                    },
                    selectedSpanish = card.id
                )
            }
            checkVocabMatch()
        }
    }

    fun onRussianCardClick(index: Int) {
        val card = _uiState.value.russianCards.getOrNull(index) ?: return
        if (card.state == FlujoCardState.MATCHED) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    russianCards = state.russianCards.map {
                        when {
                            it.state == FlujoCardState.INCORRECT -> it.copy(state = FlujoCardState.NORMAL)
                            it.state == FlujoCardState.SELECTED -> it.copy(state = FlujoCardState.NORMAL)
                            it.id == card.id -> it.copy(state = FlujoCardState.SELECTED)
                            else -> it
                        }
                    },
                    spanishCards = state.spanishCards.map {
                        if (it.state == FlujoCardState.INCORRECT) it.copy(state = FlujoCardState.NORMAL) else it
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
                        if (it.id == selectedSpanishId) it.copy(state = FlujoCardState.SHOWING_SUCCESS) else it
                    },
                    russianCards = state.russianCards.map {
                        if (it.id == selectedRussianId) it.copy(state = FlujoCardState.SHOWING_SUCCESS) else it
                    },
                    correctInLevel = state.correctInLevel + 1,
                    selectedSpanish = null,
                    selectedRussian = null
                )
            }

            viewModelScope.launch {
                delay(400)
                _uiState.update { state ->
                    state.copy(
                        spanishCards = state.spanishCards.map {
                            if (it.id == selectedSpanishId) it.copy(state = FlujoCardState.MATCHED) else it
                        },
                        russianCards = state.russianCards.map {
                            if (it.id == selectedRussianId) it.copy(state = FlujoCardState.MATCHED) else it
                        }
                    )
                }
            }

            checkVocabComplete()
        } else {
            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        if (it.id == selectedSpanishId) it.copy(state = FlujoCardState.INCORRECT) else it
                    },
                    russianCards = state.russianCards.map {
                        if (it.id == selectedRussianId) it.copy(state = FlujoCardState.INCORRECT) else it
                    },
                    incorrectInLevel = state.incorrectInLevel + 1
                )
            }

            delay(400)

            _uiState.update { state ->
                state.copy(
                    spanishCards = state.spanishCards.map {
                        if (it.id == selectedSpanishId) it.copy(state = FlujoCardState.NORMAL) else it
                    },
                    russianCards = state.russianCards.map {
                        if (it.id == selectedRussianId) it.copy(state = FlujoCardState.NORMAL) else it
                    },
                    selectedSpanish = null,
                    selectedRussian = null
                )
            }
        }
    }

    private suspend fun checkVocabComplete() {
        val allMatched = _uiState.value.spanishCards.all {
            it.state == FlujoCardState.MATCHED || it.state == FlujoCardState.SHOWING_SUCCESS
        }

        if (allMatched) {
            delay(1500)
            _uiState.update { it.copy(answeredInLevel = it.answeredInLevel + 1) }
            loadQuestion(_uiState.value.currentQuestionIndex + 1)
        }
    }

    // ========== GRAMMAR / AUDIO / LECTURA ==========

    fun onAnswerClick(index: Int) {
        val answer = _uiState.value.answers.getOrNull(index) ?: return
        if (answer.state == FlujoCardState.MATCHED) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        when {
                            it.state == FlujoCardState.INCORRECT -> it.copy(state = FlujoCardState.NORMAL)
                            it.state == FlujoCardState.SELECTED -> it.copy(state = FlujoCardState.NORMAL)
                            it.id == answer.id -> it.copy(state = FlujoCardState.SELECTED)
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
                        if (it.id == selectedId) it.copy(state = FlujoCardState.SHOWING_SUCCESS) else it
                    },
                    correctInLevel = state.correctInLevel + 1,
                    selectedAnswer = null
                )
            }

            viewModelScope.launch {
                delay(400)
                _uiState.update { state ->
                    state.copy(
                        answers = state.answers.map {
                            if (it.id == selectedId) it.copy(state = FlujoCardState.MATCHED) else it
                        }
                    )
                }
            }

            delay(1500)
            _uiState.update { it.copy(answeredInLevel = it.answeredInLevel + 1) }
            loadQuestion(_uiState.value.currentQuestionIndex + 1)
        } else {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = FlujoCardState.INCORRECT) else it
                    },
                    incorrectInLevel = state.incorrectInLevel + 1
                )
            }

            delay(400)

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = FlujoCardState.NORMAL) else it
                    },
                    selectedAnswer = null
                )
            }
        }
    }

    // ========== GRAMMAR DRAG-DROP ==========

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
            val question = _uiState.value.currentQuestion as? FlujoQuestion.Grammar
            val correctAnswer = question?.question?.respuestaCorrecta?.trim() ?: ""
            val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)

            _uiState.update {
                it.copy(
                    correctInLevel = if (isCorrect) it.correctInLevel + 1 else it.correctInLevel,
                    incorrectInLevel = if (!isCorrect) it.incorrectInLevel + 1 else it.incorrectInLevel,
                    answeredInLevel = it.answeredInLevel + 1
                )
            }

            delay(1000)
            loadQuestion(_uiState.value.currentQuestionIndex + 1)
        }
    }

    // ========== AUDIO PLAYER ==========

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

    // ========== LEVEL PROGRESSION ==========

    private fun terminarNivel(respuestasCorrectas: Int) {
        val nivel = _uiState.value.currentLevel
        val total = _uiState.value.answeredInLevel
        val aprobado = respuestasCorrectas >= 3 // Need 3/4 to pass

        val result = FlujoLevelResult(
            nivel = nivel,
            correctas = respuestasCorrectas,
            total = total,
            aprobado = aprobado
        )

        val newResults = _uiState.value.levelResults.toMutableMap()
        newResults[nivel] = result

        _uiState.update {
            it.copy(
                levelResults = newResults,
                showLevelCompleteDialog = aprobado,
                showStopDialog = !aprobado
            )
        }

        Log.d("FlujoVM", "Level $nivel completed: $respuestasCorrectas/$total (${if (aprobado) "PASS" else "FAIL"})")
    }

    fun continueToNextLevel() {
        val currentIndex = levelSequence.indexOf(_uiState.value.currentLevel)
        if (currentIndex < levelSequence.size - 1) {
            val nextLevel = levelSequence[currentIndex + 1]
            _uiState.update { it.copy(showLevelCompleteDialog = false) }
            cargarPreguntasNivel(nextLevel)
        } else {
            stopTest()
        }
    }

    fun continueDespiteFailure() {
        val currentIndex = levelSequence.indexOf(_uiState.value.currentLevel)
        if (currentIndex < levelSequence.size - 1) {
            val nextLevel = levelSequence[currentIndex + 1]
            _uiState.update { it.copy(showStopDialog = false) }
            cargarPreguntasNivel(nextLevel)
        } else {
            stopTest()
        }
    }

    fun stopTest() {
        _uiState.update {
            it.copy(
                showLevelCompleteDialog = false,
                showStopDialog = false,
                isTestComplete = true
            )
        }
        saveResults()
    }

    fun getFinalLevel(): String {
        val passedLevels = _uiState.value.levelResults.filter { it.value.aprobado }
        return passedLevels.keys.maxByOrNull { levelSequence.indexOf(it) } ?: "A1"
    }

    fun saveResults() {
        val finalLevel = getFinalLevel()
        val totalQuestions = _uiState.value.levelResults.values.sumOf { it.total }
        val totalCorrect = _uiState.value.levelResults.values.sumOf { it.correctas }

        // Manual JSON building (no Gson dependency)
        val levelResultsJson = buildString {
            append("{")
            _uiState.value.levelResults.entries.forEachIndexed { index, entry ->
                if (index > 0) append(",")
                append("\"${entry.key}\":{")
                append("\"correctas\":${entry.value.correctas},")
                append("\"total\":${entry.value.total},")
                append("\"aprobado\":${entry.value.aprobado}")
                append("}")
            }
            append("}")
        }

        sessionManager.saveFlujoResult(finalLevel, totalQuestions, totalCorrect, levelResultsJson)
        Log.d("FlujoVM", "Results saved: $finalLevel ($totalCorrect/$totalQuestions)")
    }

    // ========== EXIT DIALOG ==========

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
        saveResults()
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
}