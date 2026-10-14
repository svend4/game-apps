package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.ui.viewmodel.THEMES
import com.example.ui.theme.ThemePreset
import com.example.ui.viewmodel.CircusViewModel
import com.example.ui.viewmodel.Screen
import com.example.data.model.HarmonyRecord

@Composable
fun CircusDashboard(
    viewModel: CircusViewModel,
    theme: ThemePreset,
    records: List<HarmonyRecord>,
    funnelBest: Int,
    carouselBest: Int,
    bubblesBest: Int,
    themeIndex: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_root"),
        contentPadding = PaddingValues(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Spacing for edge-to-edge status bar
        item {
            Spacer(modifier = Modifier.height(56.dp))
        }

        // 2. Beautiful Surreal Headings with Neon Text Accents
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Surface(
                    color = theme.primaryAccent.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cyclone,
                            contentDescription = "Spindle",
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ИНТЕРАКТИВНОЕ ПРОСТРАНСТВО",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primaryAccent
                        )
                    }
                }

                Text(
                    text = "СЮРРЕАЛИСТИЧЕСКИЙ",
                    style = MaterialTheme.typography.headlineSmall,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ЦИРК СНОВ",
                    style = MaterialTheme.typography.displayMedium.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(theme.primaryAccent, theme.secondaryAccent)
                        )
                    ),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Аттракционы гармонии, где всякое действо рождает созвучия",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp)
                )
            }
        }

        // 3. Theme Controller Card switcher (Cycle themes dynamically!)
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .testTag("theme_switcher_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = theme.surfaceColor.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, theme.secondaryAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "АКТИВНОЕ ИЗМЕРЕНИЕ",
                            style = MaterialTheme.typography.labelSmall,
                            color = theme.secondaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = theme.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.changeTheme() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("cycle_theme_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Change Theme",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Сменить")
                        }
                    }
                }
            }
        }

        // 4. Attractions Heading Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp).padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ВОРОТА АТТРАКЦИОНОВ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Gate 1: Gravity Funnel (Ловцы Сфер)
        item {
            AttractionGateCard(
                title = "Ловцы Блуждающих Сфер",
                subtitle = "Улавливайте неоновые падающие шары в трехмерной перспективе под нежные колокольчики.",
                icon = Icons.Default.VerticalAlignBottom,
                bestScore = funnelBest,
                accentColor = theme.primaryAccent,
                tag = "gate_funnel",
                onClick = { viewModel.navigateTo(Screen.AttractionFunnel) }
            )
        }

        // 6. Gate 2: Celestial Carousel (Карусель Звезд)
        item {
            AttractionGateCard(
                title = "Карусель Созвездий",
                subtitle = "Вращайте трехмерную звездную призму в космическом пространстве и соединяйте созвездия.",
                icon = Icons.Default.AllInclusive,
                bestScore = carouselBest,
                accentColor = theme.secondaryAccent,
                tag = "gate_carousel",
                onClick = { viewModel.navigateTo(Screen.AttractionCarousel) }
            )
        }

        // 7. Gate 3: Ethereal Bubbles (Сферы Эфира)
        item {
            AttractionGateCard(
                title = "Эфирные Мыльные Пузыри",
                subtitle = "Погрузите разум в невесомость, деля летящие пузыри свайпом или сливая их воедино.",
                icon = Icons.Default.BubbleChart,
                bestScore = bubblesBest,
                accentColor = theme.starColor,
                tag = "gate_bubbles",
                onClick = { viewModel.navigateTo(Screen.AttractionBubbles) }
            )
        }

        // 8. Dream Journal Record Logs Header (Room persistence showcase)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ЖУРНАЛ СНОВИДЕНИЙ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                if (records.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearRecords() },
                        colors = ButtonDefaults.textButtonColors(contentColor = theme.primaryAccent),
                        modifier = Modifier.testTag("clear_records_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Очистить", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 9. Dream list items scroll
        if (records.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.surfaceColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.InterpreterMode,
                            contentDescription = "Dream catcher",
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Пока еще нет записей снов",
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Пройдите аттракционы, чтобы запечатлеть здесь свои лучшие созвездия слияния!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(records) { record ->
                DreamRecordListItem(record = record, theme = theme)
            }
        }
    }
}

@Composable
fun AttractionGateCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bestScore: Int,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x35000000)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon capsule
            Surface(
                color = accentColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp)
                )

                // High score indicator
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Star",
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Лучший сон: $bestScore очков",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Arrow forward indicator
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Enter gate",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DreamRecordListItem(
    record: HarmonyRecord,
    theme: ThemePreset
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1F000000)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.attractionName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = record.themePalette,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            // Record points column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.score}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = theme.secondaryAccent
                )
                Text(
                    text = "цепь: x${record.noteChainLength}",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.primaryAccent.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
