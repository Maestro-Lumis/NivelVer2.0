package com.example.nivelver20.ui.screens.vocabulario

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

enum class CardState {
    NORMAL,      // Обычное состояние
    SELECTED,    // Выбрана (синяя рамка)
    SHOWING_SUCCESS, // Показываем зеленую рамку поверх серого фона (400мс)
    INCORRECT,   // Неправильная пара (красная рамка)
    MATCHED      // Серый фон, уже сопоставлена (без рамки)
}

data class WordCard(
    val id: Int,              // Уникальный ID карточки
    val pairId: Int,          // ID пары (одинаковый для испанского и русского)
    val spanish: String,
    val russian: String,
    val isSpanish: Boolean,   // true = испанская, false = русская
    val state: CardState = CardState.NORMAL
)

data class VocabularioUiState(
    val nivelLabel: String = "NIVEL",
    val nivel: String = "A1",
    val userName: String = "NOMBRE",
    val title: String = "VOCABULARIO",
    val spanishWords: List<WordCard> = emptyList(),
    val russianWords: List<WordCard> = emptyList(),
    val selectedSpanish: Int? = null,  // ID выбранной испанской карточки
    val selectedRussian: Int? = null,  // ID выбранной русской карточки
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val testButton: String = "TEST",
    val perfilButton: String = "PERFIL",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentRound: Int = 1,         // Текущий раунд (1-7)
    val totalRounds: Int = 1,          // Всего раундов
    val isChecking: Boolean = false,   // Идет проверка пары (блокировка кликов)
    val showExitDialog: Boolean = false, // Показать диалог выхода
    val isTestComplete: Boolean = false,  // Тест завершен
    val pendingNavigation: (() -> Unit)? = null,  // Отложенная навигация после подтверждения
    val resultTextVocabulario: String = "VOCABULARIO"
)

class VocabularioViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(VocabularioUiState())
    val uiState: StateFlow<VocabularioUiState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)

    // Список всех доступных слов для уровня
    private var allAvailableWords: List<com.example.nivelver20.data.repository.VocabularioWord> = emptyList()
    // Индекс для отслеживания использованных слов
    private var usedWordsStartIndex = 0

    private var checkingJob: Job? = null

    init {
        val username = sessionManager.getCurrentUser()
        if (username != null) {
            _uiState.update { it.copy(userName = username) }
            //loadUserNivel(username)
        }
    }

    /*private fun loadUserNivel(username: String) {
        viewModelScope.launch {
            val userResult = repository.getUserByUsername(username)
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                if (user != null) {
                    _uiState.update { it.copy(nivel = user.nivel) }
                    loadAllWordsForNivel(user.nivel)
                }
            }
        }
    }*/

    // Загружаем ВСЕ слова для уровня один раз

    private fun loadAllWordsForNivel(nivel: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.getWordsByNivel(nivel)

            if (result.isSuccess) {
                val words = result.getOrNull() ?: emptyList()

                if (words.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No hay palabras para este nivel"
                        )
                    }
                    return@launch
                }

                // Проверяем, достаточно ли слов для 7 раундов (56 слов)
                if (words.size < 56) {
                    Log.w("VocabularioVM", "Недостаточно слов: ${words.size}, будем использовать повторно")
                }

                // Перемешиваем и сохраняем все слова
                allAvailableWords = words.shuffled()
                usedWordsStartIndex = 0

                // Загружаем первые 8 слов
                loadNextWordSet()

                _uiState.update { it.copy(isLoading = false, nivel = nivel) }

                Log.d("VocabularioVM", "Loaded ${words.size} words total for nivel $nivel")
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar palabras"
                    )
                }
            }
        }
    }

    // Загружает следующие 8 слов для нового раунда
    private fun loadNextWordSet() {
        if (allAvailableWords.isEmpty()) {
            Log.e("VocabularioVM", "No words available")
            return
        }

        // Берем следующие 8 слов (или с начала, если закончились)
        val wordsToUse = mutableListOf<com.example.nivelver20.data.repository.VocabularioWord>()

        for (i in 0 until 8) {
            val index = (usedWordsStartIndex + i) % allAvailableWords.size
            wordsToUse.add(allAvailableWords[index])
        }

        usedWordsStartIndex = (usedWordsStartIndex + 8) % allAvailableWords.size

        // Создаем испанские карточки
        val spanishCards = wordsToUse.mapIndexed { index, word ->
            WordCard(
                id = index,
                pairId = index,
                spanish = word.es,
                russian = word.ru,
                isSpanish = true,
                state = CardState.NORMAL
            )
        }

        // Создаем русские карточки и перемешиваем их
        val russianCards = wordsToUse.mapIndexed { index, word ->
            WordCard(
                id = index + 100, // Разные ID для русских карточек
                pairId = index,
                spanish = word.es,
                russian = word.ru,
                isSpanish = false,
                state = CardState.NORMAL
            )
        }.shuffled()

        _uiState.update {
            it.copy(
                spanishWords = spanishCards,
                russianWords = russianCards,
                selectedSpanish = null,
                selectedRussian = null
            )
        }

        Log.d("VocabularioVM", "Loaded round ${_uiState.value.currentRound} / ${_uiState.value.totalRounds}")
    }

    fun loadWords(nivel: String) {
        // Сбрасываем счетчики и раунды
        _uiState.update {
            it.copy(
                correctCount = 0,
                incorrectCount = 0,
                currentRound = 1
            )
        }
        usedWordsStartIndex = 0
        loadAllWordsForNivel(nivel)
    }

    // Нажатие на испанскую карточку
    fun onSpanishCardClick(index: Int) {
        // БЛОКИРУЕМ клики, если идет проверка правильного ответа
        if (_uiState.value.isChecking) return

        val card = _uiState.value.spanishWords.getOrNull(index) ?: return

        // Игнорируем, если карточка уже сопоставлена
        if (card.state == CardState.MATCHED) return

        // ОТМЕНЯЕМ текущую анимацию ТОЛЬКО для неправильных ответов
        if (!_uiState.value.isChecking) {
            checkingJob?.cancel()
            checkingJob = null
        }

        viewModelScope.launch {
            // Возвращаем только INCORRECT карточки в NORMAL
            _uiState.update { state ->
                state.copy(
                    spanishWords = state.spanishWords.map {
                        if (it.state == CardState.INCORRECT)
                            it.copy(state = CardState.NORMAL)
                        else it
                    },
                    russianWords = state.russianWords.map {
                        if (it.state == CardState.INCORRECT)
                            it.copy(state = CardState.NORMAL)
                        else it
                    }
                )
            }

            // Снимаем выделение с предыдущей
            _uiState.update { state ->
                val updatedCards = state.spanishWords.map {
                    if (it.state == CardState.SELECTED) it.copy(state = CardState.NORMAL)
                    else it
                }
                state.copy(
                    spanishWords = updatedCards,
                    selectedSpanish = null
                )
            }

            // Выделяем текущую
            _uiState.update { state ->
                val updatedCards = state.spanishWords.toMutableList()
                updatedCards[index] = updatedCards[index].copy(state = CardState.SELECTED)
                state.copy(
                    spanishWords = updatedCards,
                    selectedSpanish = card.id
                )
            }

            // Проверяем пару, если обе выбраны
            checkMatch()
        }
    }

    // Нажатие на русскую карточку
    fun onRussianCardClick(index: Int) {
        // БЛОКИРУЕМ клики, если идет проверка правильного ответа
        if (_uiState.value.isChecking) return

        val card = _uiState.value.russianWords.getOrNull(index) ?: return

        // Игнорируем, если карточка уже сопоставлена
        if (card.state == CardState.MATCHED) return

        // ОТМЕНЯЕМ текущую анимацию ТОЛЬКО для неправильных ответов
        if (!_uiState.value.isChecking) {
            checkingJob?.cancel()
            checkingJob = null
        }

        viewModelScope.launch {
            // Возвращаем только INCORRECT карточки в NORMAL
            _uiState.update { state ->
                state.copy(
                    spanishWords = state.spanishWords.map {
                        if (it.state == CardState.INCORRECT)
                            it.copy(state = CardState.NORMAL)
                        else it
                    },
                    russianWords = state.russianWords.map {
                        if (it.state == CardState.INCORRECT)
                            it.copy(state = CardState.NORMAL)
                        else it
                    }
                )
            }

            // Снимаем выделение с предыдущей
            _uiState.update { state ->
                val updatedCards = state.russianWords.map {
                    if (it.state == CardState.SELECTED) it.copy(state = CardState.NORMAL)
                    else it
                }
                state.copy(
                    russianWords = updatedCards,
                    selectedRussian = null
                )
            }

            // Выделяем текущую
            _uiState.update { state ->
                val updatedCards = state.russianWords.toMutableList()
                updatedCards[index] = updatedCards[index].copy(state = CardState.SELECTED)
                state.copy(
                    russianWords = updatedCards,
                    selectedRussian = card.id
                )
            }

            // Проверяем пару, если обе выбраны
            checkMatch()
        }
    }

    //Проверка совпадения пары
    private suspend fun checkMatch() {
        val selectedSpanishId = _uiState.value.selectedSpanish ?: return
        val selectedRussianId = _uiState.value.selectedRussian ?: return

        val spanishCard = _uiState.value.spanishWords.find { it.id == selectedSpanishId } ?: return
        val russianCard = _uiState.value.russianWords.find { it.id == selectedRussianId } ?: return

        // Проверяем, совпадают ли pairId
        val isCorrect = spanishCard.pairId == russianCard.pairId

        if (isCorrect) {
            Log.d("VocabularioVM", "Correct match: ${spanishCard.spanish} = ${russianCard.russian}")

            // 1. СРАЗУ делаем СЕРЫМИ (MATCHED) + показываем зеленую рамку (SHOWING_SUCCESS)
            _uiState.update { state ->
                state.copy(
                    spanishWords = state.spanishWords.map {
                        if (it.id == selectedSpanishId) it.copy(state = CardState.SHOWING_SUCCESS) else it
                    },
                    russianWords = state.russianWords.map {
                        if (it.id == selectedRussianId) it.copy(state = CardState.SHOWING_SUCCESS) else it
                    },
                    correctCount = state.correctCount + 1,
                    selectedSpanish = null,
                    selectedRussian = null
                )
            }

            // 2. Запускаем анимацию в фоне (НЕ блокируем UI)
            viewModelScope.launch {
                // Через 400мс убираем зеленую рамку, оставляем только серый фон
                delay(400)

                _uiState.update { state ->
                    state.copy(
                        spanishWords = state.spanishWords.map {
                            if (it.id == selectedSpanishId) it.copy(state = CardState.MATCHED) else it
                        },
                        russianWords = state.russianWords.map {
                            if (it.id == selectedRussianId) it.copy(state = CardState.MATCHED) else it
                        }
                    )
                }
            }

            // Проверяем, все ли пары найдены
            checkIfRoundComplete()

        } else {
            Log.d("VocabularioVM", "Incorrect match")

            // Показываем красную рамку
            _uiState.update { state ->
                state.copy(
                    spanishWords = state.spanishWords.map {
                        if (it.id == selectedSpanishId) it.copy(state = CardState.INCORRECT) else it
                    },
                    russianWords = state.russianWords.map {
                        if (it.id == selectedRussianId) it.copy(state = CardState.INCORRECT) else it
                    },
                    incorrectCount = state.incorrectCount + 1
                )
            }

            // Через 700мс возвращаем в обычное состояние
            delay(400)

            _uiState.update { state ->
                state.copy(
                    spanishWords = state.spanishWords.map {
                        if (it.id == selectedSpanishId) it.copy(state = CardState.NORMAL) else it
                    },
                    russianWords = state.russianWords.map {
                        if (it.id == selectedRussianId) it.copy(state = CardState.NORMAL) else it
                    },
                    selectedSpanish = null,
                    selectedRussian = null
                )
            }
        }
    }

   //Проверяем, завершен ли раунд (все 8 пар найдены)
    private var onNavigateToResults: ((String, Int, Int) -> Unit)? = null

    fun setResultsCallback(callback: (String, Int, Int) -> Unit) {
        onNavigateToResults = callback
    }

    // Обновите checkIfRoundComplete():
    private suspend fun checkIfRoundComplete() {
        val allMatched = _uiState.value.spanishWords.all {
            it.state == CardState.MATCHED || it.state == CardState.SHOWING_SUCCESS
        }

        if (allMatched) {
            val currentRound = _uiState.value.currentRound
            val totalRounds = _uiState.value.totalRounds

            Log.d("VocabularioVM", "Round $currentRound completed!")

            if (currentRound < totalRounds) {
                delay(1500)
                _uiState.update { it.copy(currentRound = currentRound + 1) }
                loadNextWordSet()
                Log.d("VocabularioVM", "Starting round ${currentRound + 1}")
            } else {
                // Все раунды завершены - переход на экран результатов
                Log.d("VocabularioVM", "All rounds completed!")
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
        _uiState.update { it.copy(nivel = nivel) } // Сохраняем nivel в state
        loadWords(nivel) // Загружаем слова
    }

    //Запрос на выход (показываем диалог)
    fun requestExit(onConfirm: () -> Unit) {
        _uiState.update {
            it.copy(
                showExitDialog = true,
                pendingNavigation = onConfirm
            )
        }
    }

    //Подтверждение выхода
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

    //Отмена выхода
    fun cancelExit() {
        _uiState.update {
            it.copy(
                showExitDialog = false,
                pendingNavigation = null
            )
        }
    }
}