package com.example.nivelver20.ui.screens.grammar

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

enum class GrammarAnswerState {
    NORMAL,
    SELECTED,
    SHOWING_SUCCESS,
    INCORRECT,
    MATCHED
}

data class GrammarQuestion(
    val nivel: String = "",
    val tipo: String = "",
    val pregunta: String = "",

    // Multiple Choice, Error Correction
    val opciones: List<Opcion> = emptyList(),

    // Error Correction
    val fraseIncorrecta: String? = null,
    val errorPalabra: String? = null,
    val indicePalabra: Int? = null,

    // Drag & Drop
    val palabras: List<String>? = null,
    val respuestaCorrecta: String? = null,

    val explicacion: String? = null
)

data class Opcion(
    val texto: String = "",
    val correcta: Boolean = false
)

data class GrammarAnswerItem(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val state: GrammarAnswerState = GrammarAnswerState.NORMAL
)

data class GrammarUiState(
    val nivelLabel: String = "NIVEL",
    val nivel: String = "A1",
    val userName: String = "NOMBRE",
    val title: String = "GRAMÁTICA",
    val currentQuestion: GrammarQuestion? = null,
    val answers: List<GrammarAnswerItem> = emptyList(),
    val selectedAnswer: Int? = null,
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
    val pendingNavigation: (() -> Unit)? = null,

    // Для Drag & Drop
    val dragDropWords: List<String> = emptyList(),
    val userDragDropAnswer: String = ""
)

class GrammarViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(GrammarUiState())
    val uiState: StateFlow<GrammarUiState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)

    private var allQuestions: List<GrammarQuestion> = emptyList()
    private var currentQuestionIndex = 0
    private var checkingJob: Job? = null
    private var onNavigateToResults: ((String, Int, Int) -> Unit)? = null

    init {
        val username = sessionManager.getCurrentUser()
        if (username != null) {
            _uiState.update { it.copy(userName = username) }
        }
    }

    fun setResultsCallback(callback: (String, Int, Int) -> Unit) {
        onNavigateToResults = callback
    }

    private fun loadAllQuestionsForNivel(nivel: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getGrammarQuestionsByNivel(nivel)

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

                // Конвертируем GrammarQuestionFirestore → GrammarQuestion
                val questions = firestoreQuestions.map { firestoreQuestion ->
                    GrammarQuestion(
                        nivel = firestoreQuestion.nivel,
                        tipo = firestoreQuestion.tipo,
                        pregunta = firestoreQuestion.pregunta,
                        opciones = firestoreQuestion.opciones.map { opcion ->
                            Opcion(
                                texto = opcion.texto,
                                correcta = opcion.correcta
                            )
                        },
                        fraseIncorrecta = firestoreQuestion.fraseIncorrecta,
                        errorPalabra = firestoreQuestion.errorPalabra,
                        indicePalabra = firestoreQuestion.indicePalabra,
                        palabras = firestoreQuestion.palabras,
                        respuestaCorrecta = firestoreQuestion.respuestaCorrecta,
                        explicacion = firestoreQuestion.explicacion
                    )
                }

                // Перемешиваем и берем 7 случайных
                allQuestions = questions.shuffled().take(7)
                currentQuestionIndex = 0

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
        if (currentQuestionIndex >= allQuestions.size) {
            return
        }

        val question = allQuestions[currentQuestionIndex]

        when (question.tipo) {
            "multiple_choice" -> loadMultipleChoice(question)
            "error_correction" -> loadErrorCorrection(question)
            "drag_drop" -> loadDragDrop(question)
        }
    }

    private fun loadMultipleChoice(question: GrammarQuestion) {
        val shuffledOpciones = question.opciones.shuffled()

        val answerItems = shuffledOpciones.mapIndexed { index, opcion ->
            GrammarAnswerItem(
                id = index,
                text = opcion.texto,
                isCorrect = opcion.correcta,
                state = GrammarAnswerState.NORMAL
            )
        }

        _uiState.update {
            it.copy(
                currentQuestion = question,
                answers = answerItems,
                selectedAnswer = null,
                isChecking = false,
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }
    }

    private fun loadErrorCorrection(question: GrammarQuestion) {
        val shuffledOpciones = question.opciones.shuffled()

        val answerItems = shuffledOpciones.mapIndexed { index, opcion ->
            GrammarAnswerItem(
                id = index,
                text = opcion.texto,
                isCorrect = opcion.correcta,
                state = GrammarAnswerState.NORMAL
            )
        }

        _uiState.update {
            it.copy(
                currentQuestion = question,
                answers = answerItems,
                selectedAnswer = null,
                isChecking = false,
                dragDropWords = emptyList(),
                userDragDropAnswer = ""
            )
        }
    }

    private fun loadDragDrop(question: GrammarQuestion) {
        val palabras = question.palabras ?: emptyList()
        val shuffledPalabras = palabras.shuffled()

        _uiState.update {
            it.copy(
                currentQuestion = question,
                answers = emptyList(),
                selectedAnswer = null,
                isChecking = false,
                dragDropWords = shuffledPalabras,
                userDragDropAnswer = ""
            )
        }
    }

    fun loadQuestions(nivel: String) {
        _uiState.update {
            it.copy(
                correctCount = 0,
                incorrectCount = 0,
                currentRound = 1
            )
        }
        currentQuestionIndex = 0
        loadAllQuestionsForNivel(nivel)
    }

    fun onAnswerClick(index: Int) {
        if (_uiState.value.isChecking) return

        val answer = _uiState.value.answers.getOrNull(index) ?: return

        if (answer.state == GrammarAnswerState.MATCHED) return

        if (!_uiState.value.isChecking) {
            checkingJob?.cancel()
            checkingJob = null
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.state == GrammarAnswerState.INCORRECT)
                            it.copy(state = GrammarAnswerState.NORMAL)
                        else it
                    }
                )
            }

            _uiState.update { state ->
                val updatedAnswers = state.answers.map {
                    if (it.state == GrammarAnswerState.SELECTED) it.copy(state = GrammarAnswerState.NORMAL)
                    else it
                }
                state.copy(
                    answers = updatedAnswers,
                    selectedAnswer = null
                )
            }

            _uiState.update { state ->
                val updatedAnswers = state.answers.toMutableList()
                updatedAnswers[index] = updatedAnswers[index].copy(state = GrammarAnswerState.SELECTED)
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
                        if (it.id == selectedId) it.copy(state = GrammarAnswerState.SHOWING_SUCCESS) else it
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
                            if (it.id == selectedId) it.copy(state = GrammarAnswerState.MATCHED) else it
                        }
                    )
                }
            }

            checkIfTestComplete()

        } else {
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = GrammarAnswerState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            delay(400)

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = GrammarAnswerState.NORMAL) else it
                    },
                    selectedAnswer = null
                )
            }
        }
    }

    fun onDragDropWordClick(word: String) {
        val currentAnswer = _uiState.value.userDragDropAnswer
        val newAnswer = if (currentAnswer.isEmpty()) {
            word
        } else {
            "$currentAnswer $word"
        }

        // Убираем слово из списка доступных
        val currentWords = _uiState.value.dragDropWords.toMutableList()
        val wordIndex = currentWords.indexOf(word)
        if (wordIndex != -1) {
            currentWords.removeAt(wordIndex)
        }

        _uiState.update {
            it.copy(
                userDragDropAnswer = newAnswer,
                dragDropWords = currentWords
            )
        }
    }

    fun onDragDropClear() {
        val currentAnswer = _uiState.value.userDragDropAnswer
        if (currentAnswer.isEmpty()) return

        // Разделяем на слова
        val words = currentAnswer.split(" ").filter { it.isNotEmpty() }
        if (words.isEmpty()) return

        // Берем последнее слово
        val lastWord = words.last()

        // Удаляем последнее слово из ответа
        val newAnswer = words.dropLast(1).joinToString(" ")

        // Возвращаем последнее слово в список
        val currentWords = _uiState.value.dragDropWords.toMutableList()
        currentWords.add(lastWord)

        _uiState.update {
            it.copy(
                userDragDropAnswer = newAnswer,
                dragDropWords = currentWords
            )
        }
    }

    fun onDragDropSubmit() {
        viewModelScope.launch {
            val question = _uiState.value.currentQuestion ?: return@launch
            val userAnswer = _uiState.value.userDragDropAnswer.trim()
            val correctAnswer = question.respuestaCorrecta?.trim() ?: ""

            val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)

            if (isCorrect) {
                _uiState.update { it.copy(correctCount = it.correctCount + 1) }
            } else {
                _uiState.update { it.copy(incorrectCount = it.incorrectCount + 1) }
            }

            delay(500)
            checkIfTestComplete()
        }
    }

    private suspend fun checkIfTestComplete() {
        val currentRound = _uiState.value.currentRound
        val totalRounds = _uiState.value.totalRounds

        if (currentRound < totalRounds) {
            delay(1500)
            _uiState.update { it.copy(currentRound = currentRound + 1) }
            currentQuestionIndex++
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
}