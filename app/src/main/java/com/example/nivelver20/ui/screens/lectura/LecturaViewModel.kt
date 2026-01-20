package com.example.nivelver20.ui.screens.lectura

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

enum class AnswerState {
    NORMAL,      // Обычное состояние
    SELECTED,    // Выбран (синяя рамка)
    SHOWING_SUCCESS, // Показываем зеленую рамку поверх серого фона (400мс)
    INCORRECT,   // Неправильный ответ (красная рамка)
    MATCHED      // Серый фон, уже правильно ответили (без рамки)
}

data class LecturaQuestion(
    val text: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)

data class AnswerItem(
    val id: Int,
    val text: String,
    val isCorrect: Boolean,
    val state: AnswerState = AnswerState.NORMAL
)

data class LecturaUiState(
    val nivelLabel: String = "NIVEL",
    val nivel: String = "A1",
    val userName: String = "NOMBRE",
    val title: String = "LECTURA",
    val text: String = "",
    val question: String = "",
    val answers: List<AnswerItem> = emptyList(),
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
    val pendingNavigation: (() -> Unit)? = null
)

class LecturaViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LecturaUiState())
    val uiState: StateFlow<LecturaUiState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)

    private var allAvailableQuestions: List<LecturaQuestion> = emptyList()
    private var usedQuestionsStartIndex = 0
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

            // Загружаем вопросы из Firestore
            val result = repository.getLecturaQuestionsByNivel(nivel)

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

                // Конвертируем данные из Firestore в наш формат
                val questions = firestoreQuestions.map { firestoreQuestion ->
                    // Находим индекс правильного ответа
                    val correctIndex = firestoreQuestion.opciones.indexOfFirst { it.correcta }

                    LecturaQuestion(
                        text = firestoreQuestion.pregunta,
                        answers = firestoreQuestion.opciones.map { it.texto },
                        correctAnswerIndex = correctIndex
                    )
                }

                if (questions.size < 7) {
                    Log.w("LecturaVM", "Недостаточно вопросов: ${questions.size}, будем использовать повторно")
                }

                allAvailableQuestions = questions.shuffled()
                usedQuestionsStartIndex = 0

                loadNextQuestion()

                _uiState.update { it.copy(isLoading = false, nivel = nivel) }

                Log.d("LecturaVM", "Loaded ${questions.size} questions for nivel $nivel")
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
            Log.e("LecturaVM", "No questions available")
            return
        }

        val questionIndex = usedQuestionsStartIndex % allAvailableQuestions.size
        val question = allAvailableQuestions[questionIndex]
        usedQuestionsStartIndex++

        // ПЕРЕМЕШИВАЕМ ОТВЕТЫ
        val answersWithOriginalIndex = question.answers.mapIndexed { index, text ->
            Triple(index, text, index == question.correctAnswerIndex)
        }.shuffled()

        val answerItems = answersWithOriginalIndex.mapIndexed { newIndex, (_, text, isCorrect) ->
            AnswerItem(
                id = newIndex,
                text = text,
                isCorrect = isCorrect,
                state = AnswerState.NORMAL
            )
        }

        // Загружаем текст для чтения из Firestore
        viewModelScope.launch {
            val textResult = repository.getLecturaText(question.text)
            val readingText = if (textResult.isSuccess) {
                textResult.getOrNull() ?: ""
            } else {
                ""
            }

            _uiState.update {
                it.copy(
                    text = readingText,
                    question = question.text,
                    answers = answerItems,
                    selectedAnswer = null,
                    isChecking = false
                )
            }

            Log.d("LecturaVM", "Loaded question ${_uiState.value.currentRound} / ${_uiState.value.totalRounds}")
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
        usedQuestionsStartIndex = 0
        loadAllQuestionsForNivel(nivel)
    }

    fun onAnswerClick(index: Int) {
        // БЛОКИРУЕМ клики, если идет проверка правильного ответа
        if (_uiState.value.isChecking) return

        val answer = _uiState.value.answers.getOrNull(index) ?: return

        // Игнорируем, если ответ уже выбран правильно
        if (answer.state == AnswerState.MATCHED) return

        // ОТМЕНЯЕМ текущую анимацию ТОЛЬКО для неправильных ответов
        if (!_uiState.value.isChecking) {
            checkingJob?.cancel()
            checkingJob = null
        }

        viewModelScope.launch {
            // Возвращаем только INCORRECT ответы в NORMAL
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.state == AnswerState.INCORRECT)
                            it.copy(state = AnswerState.NORMAL)
                        else it
                    }
                )
            }

            // Снимаем выделение с предыдущего
            _uiState.update { state ->
                val updatedAnswers = state.answers.map {
                    if (it.state == AnswerState.SELECTED) it.copy(state = AnswerState.NORMAL)
                    else it
                }
                state.copy(
                    answers = updatedAnswers,
                    selectedAnswer = null
                )
            }

            // Выделяем текущий
            _uiState.update { state ->
                val updatedAnswers = state.answers.toMutableList()
                updatedAnswers[index] = updatedAnswers[index].copy(state = AnswerState.SELECTED)
                state.copy(
                    answers = updatedAnswers,
                    selectedAnswer = answer.id
                )
            }

            // Проверяем ответ
            checkAnswer()
        }
    }

    private suspend fun checkAnswer() {
        val selectedId = _uiState.value.selectedAnswer ?: return
        val selectedAnswer = _uiState.value.answers.find { it.id == selectedId } ?: return

        val isCorrect = selectedAnswer.isCorrect

        if (isCorrect) {
            Log.d("LecturaVM", "Correct answer!")

            // 1. СРАЗУ делаем СЕРЫМ (MATCHED) + показываем зеленую рамку (SHOWING_SUCCESS)
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AnswerState.SHOWING_SUCCESS) else it
                    },
                    correctCount = state.correctCount + 1,
                    selectedAnswer = null
                )
            }

            // 2. Запускаем анимацию в фоне (НЕ блокируем UI)
            viewModelScope.launch {
                // Через 400мс убираем зеленую рамку, оставляем только серый фон
                delay(400)

                _uiState.update { state ->
                    state.copy(
                        answers = state.answers.map {
                            if (it.id == selectedId) it.copy(state = AnswerState.MATCHED) else it
                        }
                    )
                }
            }

            // Проверяем, не последний ли это раунд
            checkIfTestComplete()

        } else {
            Log.d("LecturaVM", "Incorrect answer!")

            // Показываем красную рамку
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AnswerState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            // Через 400мс возвращаем в обычное состояние
            delay(400)

            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AnswerState.NORMAL) else it
                    },
                    selectedAnswer = null
                )
            }
        }
    }

    private suspend fun checkIfTestComplete() {
        val allMatched = _uiState.value.answers.all {
            it.state == AnswerState.MATCHED || it.state == AnswerState.SHOWING_SUCCESS || !it.isCorrect
        }

        if (allMatched) {
            val currentRound = _uiState.value.currentRound
            val totalRounds = _uiState.value.totalRounds

            Log.d("LecturaVM", "Round $currentRound completed!")

            if (currentRound < totalRounds) {
                delay(1500)
                _uiState.update { it.copy(currentRound = currentRound + 1) }
                loadNextQuestion()
                Log.d("LecturaVM", "Starting round ${currentRound + 1}")
            } else {
                Log.d("LecturaVM", "All rounds completed!")
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
}