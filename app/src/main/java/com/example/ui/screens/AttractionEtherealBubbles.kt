package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemePreset
import com.example.ui.viewmodel.CircusViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class EtherealBubble(
    val id: Long,
    var x: Float, // current X
    var y: Float, // current Y
    var vx: Float, // velocity X
    var vy: Float, // velocity Y
    var radius: Float,
    val color: Color,
    val noteIndex: Int,
    var z: Float = 0f, // depth perspective (floating closer and further)
    var wobble: Float = 0f,
    var isPopped: Boolean = false,
    var popProgress: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionEtherealBubbles(
    viewModel: CircusViewModel,
    theme: ThemePreset,
    highScore: Int
) {
    val coroutineScope = rememberCoroutineScope()
    var score by remember { mutableStateOf(0) }
    var splitCount by remember { mutableStateOf(0) }

    val bubbles = remember { mutableStateListOf<EtherealBubble>() }
    var bubbleIdCounter by remember { mutableLongStateOf(0L) }

    // Canvas sizes
    var canvasWidth by remember { mutableStateOf(800f) }
    var canvasHeight by remember { mutableStateOf(1000f) }

    // Spawn & Physics Loop
    LaunchedEffect(Unit) {
        // Starters
        repeat(7) {
            val colors = theme.bubbleColors
            val noteIdx = Random.nextInt(colors.size)
            bubbles.add(
                EtherealBubble(
                    id = bubbleIdCounter++,
                    x = 100f + Random.nextFloat() * 500f,
                    y = 300f + Random.nextFloat() * 400f,
                    vx = (Random.nextFloat() * 3f - 1.5f),
                    vy = -(1.5f + Random.nextFloat() * 2f),
                    radius = 45f + Random.nextFloat() * 35f,
                    color = colors[noteIdx],
                    noteIndex = noteIdx,
                    z = Random.nextFloat() * 200f
                )
            )
        }

        // Action Loop
        while (true) {
            delay(16) // ~60fps

            // Move and bounce bubbles safely using snapshot list to prevent concurrent modifications
            val currentBubbles = bubbles.toList()
            val toRemove = mutableListOf<EtherealBubble>()
            
            currentBubbles.forEach { bubble ->
                if (bubble.isPopped) {
                    bubble.popProgress += 0.12f
                    if (bubble.popProgress >= 1f) {
                        toRemove.add(bubble)
                    }
                } else {
                    // Normal floating physics in atmospheric space
                    bubble.x += bubble.vx
                    bubble.y += bubble.vy
                    bubble.wobble += 0.07f

                    // Drifting z depth
                    bubble.z = (bubble.z + sin(bubble.wobble * 0.2f) * 1f).coerceIn(0f, 300f)

                    // Slow rise
                    if (bubble.vy > -1.2f) bubble.vy -= 0.02f

                    // Screen boundaries bounce custom check
                    if (bubble.x - bubble.radius < 0f) {
                        bubble.x = bubble.radius
                        bubble.vx = -bubble.vx * 0.9f
                    }
                    if (bubble.x + bubble.radius > canvasWidth) {
                        bubble.x = canvasWidth - bubble.radius
                        bubble.vx = -bubble.vx * 0.9f
                    }

                    // If bubble floats off screen top, respawn at bottom nicely
                    if (bubble.y + bubble.radius < -100f) {
                        bubble.y = canvasHeight + 100f
                        bubble.x = 100f + Random.nextFloat() * (canvasWidth - 200f)
                        bubble.vy = -(1f + Random.nextFloat() * 2.5f)
                        bubble.vx = (Random.nextFloat() * 2f - 1f)
                    }
                }
            }

            if (toRemove.isNotEmpty()) {
                bubbles.removeAll(toRemove)
            }

            // Perform collision merge checking safely
            val afterMoveBubbles = bubbles.toList()
            for (i in 0 until afterMoveBubbles.size) {
                for (j in (i + 1) until afterMoveBubbles.size) {
                    val b1 = afterMoveBubbles.getOrNull(i)
                    val b2 = afterMoveBubbles.getOrNull(j)
                    if (b1 != null && b2 != null && !b1.isPopped && !b2.isPopped) {
                        val dx = b1.x - b2.x
                        val dy = b1.y - b2.y
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        val rSum = b1.radius + b2.radius

                        if (dist < rSum * 0.75f) {
                            // Merge! Keep b1 as the mega bubble, delete b2.
                            b1.radius = (b1.radius + b2.radius * 0.5f).coerceAtMost(130f)
                            b1.vx = (b1.vx + b2.vx) * 0.5f
                            b1.vy = (b1.vy + b2.vy) * 0.5f
                            
                            // Pop b2 without visual debris
                            b2.isPopped = true
                            b2.popProgress = 1f // instant remove

                            score += 15

                            // Play deep, glowing organ-like harmony Chord Synthesizer note
                            viewModel.soundGenerator.playNoteIndex((b1.noteIndex + 3).coerceAtMost(9), 500L)
                        }
                    }
                }
            }

            // Continuous spawn from pipe
            if (bubbles.count { !it.isPopped } < 9) {
                if (Random.nextInt(100) < 3) {
                    val colors = theme.bubbleColors
                    val noteIdx = Random.nextInt(colors.size)
                    bubbles.add(
                        EtherealBubble(
                            id = bubbleIdCounter++,
                            x = canvasWidth / 2f + (Random.nextFloat() * 80f - 40f),
                            y = canvasHeight + 30f,
                            vx = (Random.nextFloat() * 4f - 2f),
                            vy = -(2f + Random.nextFloat() * 3f),
                            radius = 40f + Random.nextFloat() * 30f,
                            color = colors[noteIdx],
                            noteIndex = noteIdx,
                            z = Random.nextFloat() * 200f
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Эфирные Мыльные Пузыри",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveHarmonyRecord("Сферы Эфира", score, splitCount)
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        modifier = Modifier.testTag("bubbles_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to dashboard",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveHarmonyRecord("Сферы Эфира", score, splitCount)
                            score = 0
                            splitCount = 0
                            bubbles.clear()
                            viewModel.soundGenerator.playTone(440.00f, 150L)
                        },
                        modifier = Modifier.testTag("bubbles_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset bubbles",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scoreboards / Multipliers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ОЧКИ ЭФИРА",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = theme.primaryAccent
                        )
                    }

                    // Splits count
                    Surface(
                        color = theme.secondaryAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BubbleChart,
                                contentDescription = "Bubble icon",
                                tint = theme.secondaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Делений: $splitCount",
                                fontWeight = FontWeight.SemiBold,
                                color = theme.secondaryAccent,
                                style = MaterialTheme.typography.bodyMedium
                              )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "РЕКОРД",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${kotlin.math.max(highScore, score)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = theme.starColor
                        )
                    }
                }

                // Instruction bar
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = theme.surfaceColor.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Касайтесь пузырей, чтобы умиротворенно лопать их, ИЛИ проведите пальцем (свайп), чтобы делить пузырь на два меньших шарика разной тональности! Близкие пузыри плавно сливаются в гиганты.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The physics viewport Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0x1F000000))
                        .pointerInput(theme) {
                            detectTapGestures { tapOffset ->
                                // Check if tapped inside any active bubble safely on snapshot copy
                                bubbles.toList().forEach { b ->
                                    if (!b.isPopped) {
                                        val dx = tapOffset.x - b.x
                                        val dy = tapOffset.y - b.y
                                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                        if (dist < b.radius + 15f) {
                                            // POPPED!
                                            b.isPopped = true
                                            score += 10
                                            viewModel.soundGenerator.playNoteIndex(b.noteIndex, 300L)
                                        }
                                    }
                                }
                            }
                        }
                        .pointerInput(theme) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val ptr = change.position
                                // Drag-cutting detection (Swipe to cut the bubble) safely with snapshot list
                                val toAdd = mutableListOf<EtherealBubble>()
                                bubbles.toList().forEach { b ->
                                    if (!b.isPopped && b.radius > 32f) {
                                        val dx = ptr.x - b.x
                                        val dy = ptr.y - b.y
                                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                                        if (dist < b.radius + 15f) {
                                            // SPLIT! Make it popped and spawn 2 smaller ones
                                            b.isPopped = true
                                            b.popProgress = 1f // instantly replace

                                            score += 5
                                            splitCount++

                                            // Spawn 2 smaller bubbles in half size flying left & right
                                            val radNew = b.radius * 0.6f
                                            val colors = theme.bubbleColors
                                            
                                            toAdd.add(
                                                EtherealBubble(
                                                    id = bubbleIdCounter++,
                                                    x = b.x - radNew,
                                                    y = b.y,
                                                    vx = -2.5f - Random.nextFloat() * 2f,
                                                    vy = b.vy * 0.9f,
                                                    radius = radNew,
                                                    color = colors[(b.noteIndex + 1) % colors.size],
                                                    noteIndex = (b.noteIndex + 1) % 10,
                                                    z = b.z
                                                )
                                            )

                                            toAdd.add(
                                                EtherealBubble(
                                                    id = bubbleIdCounter++,
                                                    x = b.x + radNew,
                                                    y = b.y,
                                                    vx = 2.5f + Random.nextFloat() * 2f,
                                                    vy = b.vy * 0.9f,
                                                    radius = radNew,
                                                    color = colors[(b.noteIndex + 2) % colors.size],
                                                    noteIndex = (b.noteIndex + 2) % 10,
                                                    z = b.z
                                                )
                                            )

                                            // Trigger neat double splitting harmony pitch sound
                                            viewModel.soundGenerator.playNoteIndex(b.noteIndex, 200L)
                                        }
                                    }
                                }
                                if (toAdd.isNotEmpty()) {
                                    bubbles.addAll(toAdd)
                                }
                            }
                        }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("bubbles_viewport")
                    ) {
                        canvasWidth = size.width
                        canvasHeight = size.height

                        val centerX = size.width / 2f
                        val focalLength = 300f
                        val cameraD = 350f

                        // 1. Draw bubble generator pipeline nozzle at bottom center
                        // Styled as a surreal metallic golden-glowing physical pipe
                        val pipeW = 100f
                        val pipeH = 50f
                        val pipeX = centerX - pipeW / 2
                        val pipeY = size.height - pipeH

                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(theme.secondaryAccent.copy(alpha = 0.5f), Color.White.copy(alpha = 0.9f), theme.secondaryAccent.copy(alpha = 0.5f))
                            ),
                            topLeft = Offset(pipeX, pipeY),
                            size = Size(pipeW, pipeH),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        // Pipe mouth glowing aura
                        drawOval(
                            color = theme.primaryAccent,
                            topLeft = Offset(centerX - 35f, pipeY - 10f),
                            size = Size(70f, 20f),
                            style = Stroke(width = 4f)
                        )

                        // 2. Render bubbles safely on snapshot list copy
                        bubbles.toList().forEach { b ->
                            val scale = focalLength / (focalLength + cameraD + b.z)
                            val drawRadius = b.radius * scale
                            val px = b.x
                            val py = b.y

                            // Wobble displacement
                            val wobbleX = sin(b.wobble) * 6f * scale
                            val wobbleY = cos(b.wobble) * 4f * scale

                            if (b.isPopped) {
                                // Draw expanding starry debris particles on pop!
                                val popProgress = b.popProgress
                                for (p in 0..7) {
                                    val angle = (p.toFloat() / 8f) * (2f * Math.PI)
                                    val dist = drawRadius * (1f + popProgress * 1.5f)
                                    val pSize = 4f * (1f - popProgress)
                                    drawCircle(
                                        color = b.color.copy(alpha = 1f - popProgress),
                                        radius = pSize,
                                        center = Offset(
                                            (px + dist * cos(angle)).toFloat(),
                                            (py + dist * sin(angle)).toFloat()
                                        )
                                    )
                                }
                            } else {
                                // Draw glowing fluid bubble outline
                                // Highlighted oil glossy iridescent borders
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            b.color.copy(alpha = 0.05f),
                                            b.color.copy(alpha = 0.45f),
                                            Color.White.copy(alpha = 0.8f)
                                        ),
                                        center = Offset(px + wobbleX, py + wobbleY),
                                        radius = drawRadius
                                    ),
                                    radius = drawRadius,
                                    center = Offset(px + wobbleX, py + wobbleY)
                                )

                                // Specular bright reflection highlight (creates glassy/soap look!)
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.65f),
                                    radius = drawRadius * 0.22f,
                                    center = Offset(px + wobbleX - drawRadius * 0.45f, py + wobbleY - drawRadius * 0.45f)
                                )

                                // Soft neon aura rings wrapping them
                                drawCircle(
                                    color = theme.secondaryAccent.copy(alpha = 0.12f),
                                    radius = drawRadius * 1.2f,
                                    center = Offset(px + wobbleX, py + wobbleY),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
