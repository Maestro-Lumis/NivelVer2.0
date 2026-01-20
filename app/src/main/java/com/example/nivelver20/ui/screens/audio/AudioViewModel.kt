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
    NORMAL,      // Обычное состояние
    SELECTED,    // Выбран (синяя рамка)
    SHOWING_SUCCESS, // Показываем зеленую рамку поверх серого фона (400мс)
    INCORRECT,   // Неправильный ответ (красная рамка)
    MATCHED      // Серый фон, уже правильно ответили (без рамки)
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

            val mockQuestions = generateMockQuestions()

            if (mockQuestions.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No hay preguntas para este nivel"
                    )
                }
                return@launch
            }

            allAvailableQuestions = mockQuestions.shuffled()
            usedQuestionsStartIndex = 0

            loadNextQuestion()

            _uiState.update { it.copy(isLoading = false, nivel = nivel) }

            Log.d("AudioVM", "Loaded ${mockQuestions.size} questions for nivel $nivel")
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

        val answerItems = question.answers.mapIndexed { index, text ->
            AudioAnswerItem(
                id = index,
                text = text,
                isCorrect = index == question.correctAnswerIndex,
                state = AudioAnswerState.NORMAL
            )
        }

        _uiState.update {
            it.copy(
                audioUrl = question.audioUrl,
                question = question.question,
                answers = answerItems,
                selectedAnswer = null,
                isChecking = false,
                isPlaying = false,
                currentPosition = 0f
            )
        }

        Log.d("AudioVM", "Loaded question ${_uiState.value.currentRound} / ${_uiState.value.totalRounds}")
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
        if (answer.state == AudioAnswerState.MATCHED) return

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
                        if (it.state == AudioAnswerState.INCORRECT)
                            it.copy(state = AudioAnswerState.NORMAL)
                        else it
                    }
                )
            }

            // Снимаем выделение с предыдущего
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

            // Выделяем текущий
            _uiState.update { state ->
                val updatedAnswers = state.answers.toMutableList()
                updatedAnswers[index] = updatedAnswers[index].copy(state = AudioAnswerState.SELECTED)
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
            Log.d("AudioVM", "Correct answer!")

            // 1. СРАЗУ делаем СЕРЫМ (MATCHED) + показываем зеленую рамку (SHOWING_SUCCESS)
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AudioAnswerState.SHOWING_SUCCESS) else it
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
                            if (it.id == selectedId) it.copy(state = AudioAnswerState.MATCHED) else it
                        }
                    )
                }
            }

            // Проверяем, не последний ли это раунд
            checkIfTestComplete()

        } else {
            Log.d("AudioVM", "Incorrect answer!")

            // Показываем красную рамку
            _uiState.update { state ->
                state.copy(
                    answers = state.answers.map {
                        if (it.id == selectedId) it.copy(state = AudioAnswerState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            // Через 400мс возвращаем в обычное состояние
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
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
        // TODO: Здесь будет логика воспроизведения аудио
    }

    fun onSliderValueChange(value: Float) {
        _uiState.update { it.copy(currentPosition = value) }
        // TODO: Здесь будет логика перемотки аудио
    }

    private fun generateMockQuestions(): List<AudioQuestion> {
        return listOf(
            AudioQuestion(
                audioUrl = "audio1.mp3",
                question = "¿Qué escuchaste en el audio?",
                answers = listOf(
                    "Una conversación sobre el clima",
                    "Una conversación sobre comida",
                    "Una conversación sobre viajes"
                ),
                correctAnswerIndex = 1
            ),
            AudioQuestion(
                audioUrl = "audio2.mp3",
                question = "¿De qué hablan las personas?",
                answers = listOf(
                    "De sus familias",
                    "De su trabajo",
                    "De deportes"
                ),
                correctAnswerIndex = 0
            ),
            AudioQuestion(
                audioUrl = "audio3.mp3",
                question = "¿Cuál es el tema principal?",
                answers = listOf(
                    "La música",
                    "El cine",
                    "Los libros"
                ),
                correctAnswerIndex = 2
            ),
            AudioQuestion(
                audioUrl = "audio4.mp3",
                question = "¿Qué planean hacer?",
                answers = listOf(
                    "Ir al parque",
                    "Ir al cine",
                    "Ir a un restaurante"
                ),
                correctAnswerIndex = 0
            ),
            AudioQuestion(
                audioUrl = "audio5.mp3",
                question = "¿Qué opinan sobre el tema?",
                answers = listOf(
                    "Están de acuerdo",
                    "No están de acuerdo",
                    "No tienen opinión"
                ),
                correctAnswerIndex = 1
            ),
            AudioQuestion(
                audioUrl = "audio6.mp3",
                question = "¿Cuándo van a encontrarse?",
                answers = listOf(
                    "Mañana",
                    "Hoy",
                    "La próxima semana"
                ),
                correctAnswerIndex = 0
            ),
            AudioQuestion(
                audioUrl = "audio7.mp3",
                question = "¿Qué necesitan comprar?",
                answers = listOf(
                    "Ropa",
                    "Comida",
                    "Libros"
                ),
                correctAnswerIndex = 1
            )
        )
    }
}