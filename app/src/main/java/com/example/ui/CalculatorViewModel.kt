package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.engine.EvaluationResult
import com.example.engine.MathEvaluator
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BackgroundStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = CalculatorDatabase.getDatabase(application)
    private val repository = HistoryRepository(database.historyDao())
    private val settingsManager = SettingsManager(application)

    // UI state flows
    private val _inputExpression = MutableStateFlow("")
    val inputExpression: StateFlow<String> = _inputExpression.asStateFlow()

    private val _resultText = MutableStateFlow("")
    val resultText: StateFlow<String> = _resultText.asStateFlow()

    private val _realTimePreview = MutableStateFlow("")
    val realTimePreview: StateFlow<String> = _realTimePreview.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(false)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    // Preferences/Settings State Flows
    private val _currentTheme = MutableStateFlow(settingsManager.theme)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _currentBgStyle = MutableStateFlow(settingsManager.background)
    val currentBgStyle: StateFlow<BackgroundStyle> = _currentBgStyle.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(settingsManager.isSoundEnabled)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _soundVolume = MutableStateFlow(settingsManager.soundVolume)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _isAnimationEnabled = MutableStateFlow(settingsManager.isAnimationEnabled)
    val isAnimationEnabled: StateFlow<Boolean> = _isAnimationEnabled.asStateFlow()

    // History and sorting filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlyFavoritesFilter = MutableStateFlow(false)
    val onlyFavoritesFilter: StateFlow<Boolean> = _onlyFavoritesFilter.asStateFlow()

    // History sorted lists from database Flow
    val calculationHistory: StateFlow<List<HistoryItem>> = combine(
        _searchQuery,
        _onlyFavoritesFilter,
        repository.allHistory
    ) { query, onlyFavs, rawList ->
        var list = if (query.isBlank()) {
            rawList
        } else {
            rawList.filter { 
                it.expression.contains(query, ignoreCase = true) || 
                it.result.contains(query, ignoreCase = true) 
            }
        }
        if (onlyFavs) {
            list = list.filter { it.isFavorite }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks multi-selection IDs for deletion
    private val _selectedHistoryIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedHistoryIds: StateFlow<Set<Long>> = _selectedHistoryIds.asStateFlow()

    fun appendToken(token: String) {
        val current = _inputExpression.value
        
        // Logical formatting: prevent typing multiple operators consecutively
        val isNewOperator = isOperator(token)
        val isLastCharOperator = current.isNotEmpty() && isOperator(current.last().toString())

        if (isNewOperator && isLastCharOperator) {
            // Replace previous operation, except for unary negative sign additions or brackets
            if (token == "-" && current.last().toString() != "-") {
                _inputExpression.value = current + token
            } else {
                _inputExpression.value = current.dropLast(1) + token
            }
        } else {
            _inputExpression.value = current + token
        }
        
        triggerRealTimePreview()
    }

    private fun isOperator(s: String): Boolean {
        return s == "+" || s == "-" || s == "×" || s == "÷" || s == "^" || s == "%"
    }

    fun handleClear() {
        _inputExpression.value = ""
        _resultText.value = ""
        _realTimePreview.value = ""
    }

    fun handleBackspace() {
        val current = _inputExpression.value
        if (current.isEmpty()) return

        // Smart Backspace matching function suffixes
        val functions = listOf("asin(", "acos(", "atan(", "sqrt(", "cbrt(", "sin(", "cos(", "tan(", "log(", "abs(", "ln(")
        var deleted = false
        for (func in functions) {
            if (current.endsWith(func)) {
                _inputExpression.value = current.substring(0, current.length - func.length)
                deleted = true
                break
            }
        }
        if (!deleted) {
            _inputExpression.value = current.dropLast(1)
        }
        
        triggerRealTimePreview()
    }

    private fun triggerRealTimePreview() {
        val current = _inputExpression.value
        if (current.isBlank()) {
            _realTimePreview.value = ""
            return
        }
        
        // Evaluate expression without saving
        val evaluator = MathEvaluator(isDegreeMode.value)
        when (val res = evaluator.evaluate(current)) {
            is EvaluationResult.Success -> {
                // Mute preview if it evaluates to exactly the same text to avoid clutter
                if (res.formatted != current) {
                    _realTimePreview.value = res.formatted
                } else {
                    _realTimePreview.value = ""
                }
            }
            is EvaluationResult.Error -> {
                _realTimePreview.value = "" // Silently hide error during preview
            }
        }
    }

    fun evaluateFinal() {
        val expr = _inputExpression.value
        if (expr.isBlank()) return

        val evaluator = MathEvaluator(isDegreeMode.value)
        when (val res = evaluator.evaluate(expr)) {
            is EvaluationResult.Success -> {
                _resultText.value = res.formatted
                _realTimePreview.value = ""
                
                // Add to Room history
                viewModelScope.launch(Dispatchers.IO) {
                    val isSci = expr.containsAny(listOf("sin", "cos", "tan", "log", "ln", "PI", "E", "^", "sqrt", "cbrt", "abs", "!"))
                    repository.insertHistory(
                        HistoryItem(
                            expression = expr,
                            result = res.formatted,
                            isScientific = isSci
                        )
                    )
                }
            }
            is EvaluationResult.Error -> {
                _resultText.value = res.message
                _realTimePreview.value = ""
            }
        }
    }

    private fun String.containsAny(list: List<String>): Boolean {
        for (item in list) {
            if (this.contains(item, ignoreCase = true)) return true
        }
        return false
    }

    // Toggle degree or radian evaluations
    fun toggleDegreeMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        triggerRealTimePreview()
    }

    fun toggleScientificPanel() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    // Memory Functions
    fun memoryAdd() {
        val finalVal = parseCurrentResultDouble()
        _memoryValue.value += finalVal
    }

    fun memorySubtract() {
        val finalVal = parseCurrentResultDouble()
        _memoryValue.value -= finalVal
    }

    fun memoryRecall() {
        val memStr = MathEvaluator().evaluate(_memoryValue.value.toString())
        if (memStr is EvaluationResult.Success) {
            _inputExpression.value += memStr.formatted
            triggerRealTimePreview()
        }
    }

    fun memoryClear() {
        _memoryValue.value = 0.0
    }

    private fun parseCurrentResultDouble(): Double {
        val currentText = _resultText.value
        if (currentText.isEmpty()) return 0.0
        return currentText.toDoubleOrNull() ?: 0.0
    }

    // Settings adjustments
    fun updateTheme(theme: AppTheme) {
        settingsManager.theme = theme
        _currentTheme.value = theme
    }

    fun updateBackground(style: BackgroundStyle) {
        settingsManager.background = style
        _currentBgStyle.value = style
    }

    fun toggleSound(enabled: Boolean) {
        settingsManager.isSoundEnabled = enabled
        _isSoundEnabled.value = enabled
    }

    fun updateVolume(vol: Float) {
        settingsManager.soundVolume = vol
        _soundVolume.value = vol
    }

    fun toggleAnimations(enabled: Boolean) {
        settingsManager.isAnimationEnabled = enabled
        _isAnimationEnabled.value = enabled
    }

    // History Search & Filter Actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _onlyFavoritesFilter.value = !_onlyFavoritesFilter.value
    }

    fun toggleFavoriteHistory(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFavoriteStatus(item.id, !item.isFavorite)
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistory(item)
        }
    }

    fun toggleHistorySelection(id: Long) {
        val current = _selectedHistoryIds.value
        if (current.contains(id)) {
            _selectedHistoryIds.value = current - id
        } else {
            _selectedHistoryIds.value = current + id
        }
    }

    fun clearSelections() {
        _selectedHistoryIds.value = emptySet()
    }

    fun deleteSelectedHistory() {
        val ids = _selectedHistoryIds.value.toList()
        if (ids.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.deleteMultipleHistory(ids)
                _selectedHistoryIds.value = emptySet()
            }
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
            _selectedHistoryIds.value = emptySet()
        }
    }

    fun pasteExpression(text: String) {
        _inputExpression.value = text
        triggerRealTimePreview()
    }
}
