package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

data class ThemePreset(
    val name: String,
    val backgroundGradient: Brush,
    val bgSolid: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val surfaceColor: Color,
    val starColor: Color,
    val bubbleColors: List<Color>,
    val textColor: Color = Color.White,
    val description: String
)

object ThemeManager {
    fun getPreset(index: Int): ThemePreset {
        return when (index % 4) {
            0 -> ThemePreset(
                name = "Космические сумерки",
                backgroundGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0C20), Color(0xFF15102A), Color(0xFF281145))
                ),
                bgSolid = Color(0xFF15102A),
                primaryAccent = Color(0xFFFF2A85), // Neon Pink
                secondaryAccent = Color(0xFF00F0FF), // Neon Cyan
                surfaceColor = Color(0x2A5B27A2), // Semi-transparent Purple
                starColor = Color(0xFFFFD700), // Gold Star
                bubbleColors = listOf(Color(0xFFFF2A85), Color(0xFF00F0FF), Color(0xFF9D4EDD), Color(0xFF3A86C8)),
                description = "Глубокие фиолетовые тона и яркий неоновый неон."
            )
            1 -> ThemePreset(
                name = "Неоновый аттракцион",
                backgroundGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B0518), Color(0xFF2E091F), Color(0xFF4B121C))
                ),
                bgSolid = Color(0xFF2E091F),
                primaryAccent = Color(0xFFFF6B00), // Electric Orange
                secondaryAccent = Color(0xFF39FF14), // Neon Green
                surfaceColor = Color(0x2D7E192A), // Warm Red Tint
                starColor = Color(0xFFFFA500),
                bubbleColors = listOf(Color(0xFFFF6B00), Color(0xFF39FF14), Color(0xFFFFD700), Color(0xFFFF007F)),
                description = "Энергия праздничного карнавала и неоновые огни."
            )
            2 -> ThemePreset(
                name = "Изумрудный сон",
                backgroundGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF041C15), Color(0xFF0A2F1D), Color(0xFF114227))
                ),
                bgSolid = Color(0xFF0A2F1D),
                primaryAccent = Color(0xFF00FFCC), // Magic Mint
                secondaryAccent = Color(0xFFFFE347), // Mellow Yellow
                surfaceColor = Color(0x2B1A5C3A), // Light Teal Tint
                starColor = Color(0xFFFFFF99),
                bubbleColors = listOf(Color(0xFF00FFCC), Color(0xFFFFE347), Color(0xFF48CAE4), Color(0xFF80FFDB)),
                description = "Мягкая лесная мистика и сияние изумрудных светлячков."
            )
            else -> ThemePreset(
                name = "Глубокий индиго",
                backgroundGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0F24), Color(0xFF101935), Color(0xFF1B2A4A))
                ),
                bgSolid = Color(0xFF101935),
                primaryAccent = Color(0xFF7000FF), // Cyber Purple
                secondaryAccent = Color(0xFF00BFFF), // Deep Sky Blue
                surfaceColor = Color(0x282C4075), // Deep Indigo Tint
                starColor = Color(0xFFE2E2E2), // Silver
                bubbleColors = listOf(Color(0xFF7000FF), Color(0xFF00BFFF), Color(0xFFB5179E), Color(0xFF4CC9F0)),
                description = "Спокойствие открытого космоса и серебряные созвездия."
            )
        }
    }
}
