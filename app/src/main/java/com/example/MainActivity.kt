package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AttractionCelestialCarousel
import com.example.ui.screens.AttractionEtherealBubbles
import com.example.ui.screens.AttractionGravityFunnel
import com.example.ui.screens.CircusDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeManager
import com.example.ui.viewmodel.CircusViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable clean transparent edge-to-edge system navigation & status bars
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                // Initialize the main game viewmodel
                val viewModel: CircusViewModel = viewModel()
                
                // Collect states reactively
                val currentScreen by viewModel.currentScreen.collectAsState()
                val userProgress by viewModel.userProgress.collectAsState()
                val records by viewModel.harmonyRecords.collectAsState()
                
                // Higher-tier score indicators
                val funnelBest by viewModel.funnelHighScore.collectAsState()
                val carouselBest by viewModel.carouselHighScore.collectAsState()
                val bubblesBest by viewModel.bubblesHighScore.collectAsState()

                // Active space theme presets loaded dynamically
                val theme = ThemeManager.getPreset(userProgress.currentThemeIndex)

                // Handle Android physical back gestures if inside a game attraction
                if (currentScreen != Screen.Dashboard) {
                    BackHandler {
                        // Save scores when going back
                        when (currentScreen) {
                            Screen.AttractionFunnel -> viewModel.saveHarmonyRecord("Ловец Сфер", 0, 0)
                            Screen.AttractionCarousel -> viewModel.saveHarmonyRecord("Карусель Звезд", 0, 0)
                            Screen.AttractionBubbles -> viewModel.saveHarmonyRecord("Сферы Эфира", 0, 0)
                            else -> {}
                        }
                        viewModel.navigateTo(Screen.Dashboard)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = theme.bgSolid
                ) {
                    // Bleed background gradient seamlessly
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(theme.backgroundGradient)
                            .safeDrawingPadding() // Absolute notch and curved display safety
                    ) {
                        // Animate screens transitions with gorgeous crossfades
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) togetherWith
                                        fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                            },
                            label = "screen_flow_anim"
                        ) { screenState ->
                            when (screenState) {
                                Screen.Dashboard -> {
                                    CircusDashboard(
                                        viewModel = viewModel,
                                        theme = theme,
                                        records = records,
                                        funnelBest = funnelBest ?: 0,
                                        carouselBest = carouselBest ?: 0,
                                        bubblesBest = bubblesBest ?: 0,
                                        themeIndex = userProgress.currentThemeIndex
                                    )
                                }
                                Screen.AttractionFunnel -> {
                                    AttractionGravityFunnel(
                                        viewModel = viewModel,
                                        theme = theme,
                                        highScore = funnelBest ?: 0
                                    )
                                }
                                Screen.AttractionCarousel -> {
                                    AttractionCelestialCarousel(
                                        viewModel = viewModel,
                                        theme = theme,
                                        highScore = carouselBest ?: 0
                                    )
                                }
                                Screen.AttractionBubbles -> {
                                    AttractionEtherealBubbles(
                                        viewModel = viewModel,
                                        theme = theme,
                                        highScore = bubblesBest ?: 0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
