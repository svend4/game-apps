package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.ThemePreset
import com.example.ui.viewmodel.CircusViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// 3D sphere data class
data class DreamSphere(
    var id: Long,
    var x: Float, // relative lateral coordinate (-250 to 250)
    var y: Float, // height (0 is floating flat on road)
    var z: Float, // distance (0 is closest, 1000 is horizon)
    var radius: Float = 26f,
    val color: Color,
    val noteIndex: Int,
    val speed: Float,
    var rotationAngle: Float = 0f,
    var isCaught: Boolean = false,
    var isMissed: Boolean = false,
    var fadeAlpha: Float = 1f
)

// Star particle class for surreal spacespace look
data class Star3D(
    var x: Float,
    var y: Float,
    var z: Float,
    val speed: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionGravityFunnel(
    viewModel: CircusViewModel,
    theme: ThemePreset,
    highScore: Int
) {
    val coroutineScope = rememberCoroutineScope()
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var maxStreak by remember { mutableStateOf(0) }
    
    // Funnel X location (-200..200)
    var funnelX by remember { mutableStateOf(0f) }
    
    // Game lists
    val spheres = remember { mutableStateListOf<DreamSphere>() }
    val stars = remember { mutableStateListOf<Star3D>() }
    
    // Canvas dimensions helper
    var canvasWidth by remember { mutableStateOf(1000f) }
    var canvasHeight by remember { mutableStateOf(1200f) }

    // Spawn and update game ticks
    LaunchedEffect(Unit) {
        // Initialize starfield
        repeat(60) {
            stars.add(
                Star3D(
                    x = (Random.nextFloat() * 1200f - 600f),
                    y = (Random.nextFloat() * 1000f - 500f),
                    z = Random.nextFloat() * 1000f,
                    speed = 4f + Random.nextFloat() * 8f
                )
            )
        }

        var sphereIdCounter = 0L

        // Game loop
        while (true) {
            delay(16) // ~60 fps

            // 1. Move stars safely with snapshot list copy
            stars.toList().forEach { star ->
                star.z -= star.speed
                if (star.z < 10f) {
                    star.z = 1000f
                    star.x = (Random.nextFloat() * 1200f - 600f)
                    star.y = (Random.nextFloat() * 1000f - 500f)
                }
            }

            // 2. Move existing spheres safely with snapshot list copy to prevent concurrent modification exceptions
            val currentSpheres = spheres.toList()
            val toRemove = mutableListOf<DreamSphere>()

            currentSpheres.forEach { sphere ->
                if (sphere.isCaught) {
                    // transition outwards nicely
                    sphere.y -= 8f
                    sphere.radius += 2f
                    sphere.fadeAlpha -= 0.1f
                    if (sphere.fadeAlpha <= 0f) {
                        toRemove.add(sphere)
                    }
                    return@forEach
                }

                if (sphere.isMissed) {
                    // fall and dissolve
                    sphere.y += 10f
                    sphere.radius *= 0.9f
                    sphere.fadeAlpha -= 0.08f
                    if (sphere.fadeAlpha <= 0f) {
                        toRemove.add(sphere)
                    }
                    return@forEach
                }

                // Normal roll
                sphere.z -= sphere.speed
                sphere.rotationAngle += 0.08f

                // Floating amplitude
                sphere.y = -sin(sphere.z * 0.02f) * 15f

                // Check boundary collision near z = 0 (bottom range)
                if (sphere.z <= 10f) {
                    val distance = kotlin.math.abs(sphere.x - funnelX)
                    if (distance < 55f) {
                        // HIT!
                        sphere.isCaught = true
                        score += 10 + streak
                        streak++
                        if (streak > maxStreak) maxStreak = streak
                        
                        // Play gorgeous pentatonic musical chord tone
                        viewModel.soundGenerator.playNoteIndex(sphere.noteIndex, 400L)
                    } else {
                        // MISS
                        sphere.isMissed = true
                        streak = 0 // break harmony chain gracefully
                    }
                }
            }

            if (toRemove.isNotEmpty()) {
                spheres.removeAll(toRemove)
            }

            // 3. Spawning spheres logic
            if (spheres.count { !it.isCaught && !it.isMissed } < 5) {
                // Probabilistic spawn
                if (Random.nextInt(100) < 4) {
                    val colors = theme.bubbleColors
                    val noteIdx = Random.nextInt(colors.size)
                    spheres.add(
                        DreamSphere(
                            id = sphereIdCounter++,
                            x = Random.nextFloat() * 400f - 200f,
                            y = 0f,
                            z = 1000f,
                            color = colors[noteIdx],
                            noteIndex = noteIdx,
                            speed = 6f + Random.nextFloat() * 6f
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
                        text = "Ловцы Блуждающих Сфер",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveHarmonyRecord("Ловец Сфер", score, maxStreak)
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        modifier = Modifier.testTag("funnel_back_button")
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
                            // Restart
                            viewModel.saveHarmonyRecord("Ловец Сфер", score, maxStreak)
                            score = 0
                            streak = 0
                            maxStreak = 0
                            spheres.clear()
                            viewModel.soundGenerator.playTone(392f, 150L)
                        },
                        modifier = Modifier.testTag("funnel_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart attraction",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent, // Drawn on theme background, handled in parent main activity
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.radialGradient(listOf(Color.Transparent, Color(0x35000000))))
        ) {
            // Main Surreal Gamy Screen Container
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scoreboards / Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Score
                    Column {
                        Text(
                            text = "ОЧКИ ГАРМОНИИ",
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

                    // Middle: Harmony Multiplier Visualizer
                    AnimatedVisibility(
                        visible = streak > 0,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            color = theme.secondaryAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, theme.secondaryAccent),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Chain",
                                    tint = theme.secondaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Цепь: x$streak",
                                    fontWeight = FontWeight.Bold,
                                    color = theme.secondaryAccent,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Right: High score
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

                // Guide instruction text
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
                        text = "Сдвигайте парящий приемник внизу пальцем влево или вправо по экрану, чтобы ловить катящиеся по 3D-дорожке сферы снов.",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The surreal 3D Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0x20000000))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val rawNewX = funnelX + dragAmount.x
                                funnelX = rawNewX.coerceIn(-canvasWidth * 0.4f, canvasWidth * 0.4f)
                            }
                        }
                        .testTag("funnel_drag_area")
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        canvasWidth = size.width
                        canvasHeight = size.height

                        val centerX = size.width / 2f
                        val horizonY = size.height * 0.35f
                        val focalLength = 350f

                        // 1. Draw Starfield in 3D projection safely on snapshot copy
                        stars.toList().forEach { star ->
                            val scale = focalLength / (focalLength + star.z)
                            val starX = centerX + star.x * scale
                            val starY = horizonY + star.y * scale
                            val sizeValue = 2.5f * scale + 1.5f

                            if (starX in 0f..size.width && starY in 0f..size.height) {
                                drawCircle(
                                    color = Color.White.copy(alpha = scale.coerceIn(0.1f, 1f)),
                                    radius = sizeValue,
                                    center = Offset(starX, starY)
                                )
                            }
                        }

                        // 2. Draw surreal 3D road/ramp going to horizon
                        val path = Path()
                        val horizonW = 20f
                        val bottomW = 420f

                        val leftHorizonX = centerX - horizonW
                        val rightHorizonX = centerX + horizonW
                        val leftBottomX = centerX - bottomW
                        val rightBottomX = centerX + bottomW

                        path.moveTo(leftHorizonX, horizonY)
                        path.lineTo(rightHorizonX, horizonY)
                        path.lineTo(rightBottomX, size.height)
                        path.lineTo(leftBottomX, size.height)
                        path.close()

                        // Neon grid filling
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(theme.primaryAccent.copy(alpha = 0.05f), theme.secondaryAccent.copy(alpha = 0.15f))
                            )
                        )

                        // Outline of the 3D grid
                        drawPath(
                            path = path,
                            color = theme.secondaryAccent.copy(alpha = 0.4f),
                            style = Stroke(width = 3f)
                        )

                        // Perspective horizontal rungs/stripes on the ramp:
                        val zStripes = floatArrayOf(100f, 250f, 400f, 600f, 850f)
                        zStripes.forEach { zVal ->
                            val s = focalLength / (focalLength + zVal)
                            val y = horizonY + (size.height - horizonY) * s
                            val w = bottomW * s
                            drawLine(
                                color = theme.primaryAccent.copy(alpha = 0.35f * s),
                                start = Offset(centerX - w, y),
                                end = Offset(centerX + w, y),
                                strokeWidth = 2f
                            )
                        }

                        // Perspective vertical dividing lines
                        val roadLanes = floatArrayOf(-150f, -50f, 50f, 150f)
                        roadLanes.forEach { xl ->
                            val topScale = focalLength / (focalLength + 1000f)
                            val bottomScale = focalLength / (focalLength + 0f)
                            drawLine(
                                color = theme.secondaryAccent.copy(alpha = 0.2f),
                                start = Offset(centerX + xl * topScale, horizonY),
                                end = Offset(centerX + xl * bottomScale, size.height),
                                strokeWidth = 1.5f
                            )
                        }

                        // 3. Draw Dream Spheres in 3D perspective safely on snapshot copy
                        spheres.toList().forEach { sphere ->
                            val scale = focalLength / (focalLength + sphere.z)
                            // Perspective projection
                            val projectedX = centerX + sphere.x * scale
                            // Scale the road depth nicely
                            val projectedY = horizonY + (size.height - horizonY) * scale + sphere.y * scale
                            val radiusInCanvas = sphere.radius * scale + 5f

                            if (projectedX in -200f..(size.width + 200f) && projectedY in horizonY..(size.height + 200f)) {
                                val baseColor = sphere.color.copy(alpha = sphere.fadeAlpha)

                                // Draw glowing neon aura/shadow
                                drawCircle(
                                    color = baseColor.copy(alpha = 0.25f * sphere.fadeAlpha),
                                    radius = radiusInCanvas * 1.6f,
                                    center = Offset(projectedX, projectedY)
                                )

                                // Main 3D Sphere body
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White.copy(alpha = sphere.fadeAlpha), baseColor, Color.Black.copy(alpha = 0.7f * sphere.fadeAlpha)),
                                        center = Offset(projectedX - radiusInCanvas * 0.3f, projectedY - radiusInCanvas * 0.3f),
                                        radius = radiusInCanvas * 1.2f
                                    ),
                                    radius = radiusInCanvas,
                                    center = Offset(projectedX, projectedY)
                                )

                                // Rotating metallic sheen lines to emphasize 3D motion!
                                val rotationFactorX = cos(sphere.rotationAngle) * radiusInCanvas * 0.6f
                                val rotationFactorY = sin(sphere.rotationAngle) * radiusInCanvas * 0.6f
                                drawLine(
                                    color = Color.White.copy(alpha = 0.45f * sphere.fadeAlpha),
                                    start = Offset(projectedX - rotationFactorX, projectedY - rotationFactorY),
                                    end = Offset(projectedX + rotationFactorX, projectedY + rotationFactorY),
                                    strokeWidth = 2.5f
                                )
                            }
                        }

                        // 4. Draw Surreal Floating Gravity Funnel (Player receiver)
                        // It stays in 3D near z=0 (very front of the road)
                        val funnelScale = focalLength / (focalLength + 0f) // scale is 1.0 at front
                        val fnX = centerX + funnelX * funnelScale
                        val fnY = size.height - 75f
                        val fnWidth = 140f
                        val fnHeight = 65f

                        // Draw receiver neon shadow/glow
                        drawCircle(
                            color = theme.primaryAccent.copy(alpha = 0.3f),
                            radius = 65f,
                            center = Offset(fnX, fnY)
                        )

                        // Draw perspective oval rings (creating a 3D magnetic portal)
                        // Outer cylinder
                        drawRoundRect(
                            color = theme.primaryAccent,
                            topLeft = Offset(fnX - fnWidth / 2, fnY - fnHeight / 2),
                            size = Size(fnWidth, fnHeight),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 5f)
                        )

                        // Glowing inner portal rings
                        for (i in 1..3) {
                            val ringShrink = i * 14f
                            val ringAlpha = 1f - (i * 0.25f)
                            drawOval(
                                color = theme.secondaryAccent.copy(alpha = ringAlpha),
                                topLeft = Offset(fnX - (fnWidth / 2) + ringShrink, fnY - (fnHeight / 2) + (ringShrink * 0.2f)),
                                size = Size(fnWidth - ringShrink * 2f, fnHeight - ringShrink * 0.4f),
                                style = Stroke(width = 3.5f)
                            )
                        }

                        // Draw side stabilizer wings using path
                        val wingPath = Path()
                        wingPath.moveTo(fnX - fnWidth / 2, fnY)
                        wingPath.lineTo(fnX - fnWidth / 2 - 30f, fnY + 10f)
                        wingPath.lineTo(fnX - fnWidth / 2 - 10f, fnY - 20f)
                        wingPath.close()

                        drawPath(
                            path = wingPath,
                            color = theme.primaryAccent,
                            style = Stroke(width = 3f)
                        )

                        val rightWingPath = Path()
                        rightWingPath.moveTo(fnX + fnWidth / 2, fnY)
                        rightWingPath.lineTo(fnX + fnWidth / 2 + 30f, fnY + 10f)
                        rightWingPath.lineTo(fnX + fnWidth / 2 + 10f, fnY - 20f)
                        rightWingPath.close()

                        drawPath(
                            path = rightWingPath,
                            color = theme.primaryAccent,
                            style = Stroke(width = 3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
