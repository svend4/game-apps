package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SurrealSoundGenerator
import com.example.data.database.AppDatabase
import com.example.data.model.HarmonyRecord
import com.example.data.repository.HarmonyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface Screen {
    data object Dashboard : Screen
    data object AttractionFunnel : Screen
    data object AttractionCarousel : Screen
    data object AttractionBubbles : Screen
}

data class UserProgress(
    val currentThemeIndex: Int = 0,
    val activeHarmonyScore: Int = 0,
    val totalPlaySessions: Int = 0
)

val THEMES = listOf(
    "Космические сумерки" to "Twilight Violet",
    "Неоновый аттракцион" to "Neon Circus",
    "Изумрудный сон" to "Emerald Gate",
    "Глубокий индиго" to "Stargazer Indigo"
)

class CircusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HarmonyRepository(db.harmonyDao())

    val soundGenerator = SurrealSoundGenerator()

    // Screen state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Harmony Records flow from Room
    val harmonyRecords: StateFlow<List<HarmonyRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Best scores
    val funnelHighScore: StateFlow<Int?> = repository.getHighScoreForAttraction("Ловец Сфер")
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val carouselHighScore: StateFlow<Int?> = repository.getHighScoreForAttraction("Карусель Звезд")
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val bubblesHighScore: StateFlow<Int?> = repository.getHighScoreForAttraction("Сферы Эфира")
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    // Current player state
    private val _userProgress = MutableStateFlow(UserProgress())
    val userProgress: StateFlow<UserProgress> = _userProgress.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        // Play welcome sound of circus gate
        soundGenerator.playTone(440.0f, 150L)
        viewModelScope.launch {
            soundGenerator.playTone(523.25f, 250L)
        }
    }

    fun changeTheme() {
        _userProgress.update {
            it.copy(currentThemeIndex = (it.currentThemeIndex + 1) % THEMES.size)
        }
        soundGenerator.playNoteIndex(3, 150L)
    }

    fun saveHarmonyRecord(attractionName: String, score: Int, noteChainLength: Int) {
        if (score == 0) return
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
            val themeName = THEMES[_userProgress.value.currentThemeIndex].first
            val record = HarmonyRecord(
                attractionName = attractionName,
                score = score,
                dateString = dateStr,
                noteChainLength = noteChainLength,
                themePalette = themeName
            )
            repository.insertRecord(record)
            
            // Increment play session counter
            _userProgress.update {
                it.copy(totalPlaySessions = it.totalPlaySessions + 1)
            }
        }
    }

    fun clearRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
            soundGenerator.playTone(220.0f, 400L)
        }
    }
}
