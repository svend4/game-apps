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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.ThemePreset
import com.example.ui.viewmodel.CircusViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class CarouselStar(
    val id: Int,
    val baseX: Float,
    val baseY: Float,
    val baseZ: Float,
    var rotatedX: Float = 0f,
    var rotatedY: Float = 0f,
    var rotatedZ: Float = 0f,
    val color: Color,
    val colorIndex: Int,
    var isSelected: Boolean = false,
    var pulse: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttractionCelestialCarousel(
    viewModel: CircusViewModel,
    theme: ThemePreset,
    highScore: Int
) {
    val coroutineScope = rememberCoroutineScope()
    
    var score by remember { mutableStateOf(0) }
    var scaleCount by remember { mutableStateOf(0) }
    
    // Rotation angles
    var angleY by remember { mutableStateOf(0f) }
    var angleX by remember { mutableStateOf(0f) }
    
    // Star nodes
    val carouselStars = remember { mutableStateListOf<CarouselStar>() }
    
    // Selection state
    val selectedStarIds = remember { mutableStateListOf<Int>() }
    
    // Canvas dimensions helper
    var canvasWidth by remember { mutableStateOf(800f) }
    var canvasHeight by remember { mutableStateOf(800f) }

    // Initialize stars in a surreal spherical/cylinder cage carousel in 3D
    fun resetCarouselStars() {
        carouselStars.clear()
        selectedStarIds.clear()
        
        val colors = theme.bubbleColors
        var id = 0
        
        // 3 tiers vertically
        for (tier in -1..1) {
            val tierY = tier * 180f
            val countInTier = 6
            for (i in 0 until countInTier) {
                val circleAngle = (i.toFloat() / countInTier) * (2.0 * Math.PI)
                val radius = 220f
                val baseXVal = (radius * cos(circleAngle)).toFloat()
                val baseZVal = (radius * sin(circleAngle)).toFloat()
                
                // Slightly randomize color
                val colorIdx = Random.nextInt(colors.size)
                
                carouselStars.add(
                    CarouselStar(
                        id = id++,
                        baseX = baseXVal,
                        baseY = tierY + Random.nextFloat() * 40f - 20f,
                        baseZ = baseZVal,
                        color = colors[colorIdx],
                        colorIndex = colorIdx
                    )
                )
            }
        }
    }

    // Initialize once & on theme change
    LaunchedEffect(theme) {
        resetCarouselStars()
    }

    // Auto-rotation tick
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            angleY += 0.005f // Gentle auto drift
            
            // Pulse selection animations safely on snapshot copy
            carouselStars.toList().forEach { star ->
                if (star.isSelected) {
                    star.pulse += 0.15f
                } else {
                    star.pulse = 0f
                }
            }
        }
    }

    // Compute updated rotated coordinates for projection
    fun updateRotations(w: Float, h: Float) {
        val cosY = cos(angleY)
        val sinY = sin(angleY)
        val cosX = cos(angleX)
        val sinX = sin(angleX)

        carouselStars.forEach { star ->
            // Rotate around vertical Y axis
            val rx1 = star.baseX * cosY - star.baseZ * sinY
            val rz1 = star.baseX * sinY + star.baseZ * cosY
            
            // Rotate around lateral X axis
            val ry2 = star.baseY * cosX - rz1 * sinX
            val rz2 = star.baseY * sinX + rz1 * cosX

            star.rotatedX = rx1
            star.rotatedY = ry2
            star.rotatedZ = rz2
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Карусель Созвездий",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveHarmonyRecord("Карусель Звезд", score, scaleCount)
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        modifier = Modifier.testTag("carousel_back_button")
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
                            viewModel.saveHarmonyRecord("Карусель Звезд", score, scaleCount)
                            score = 0
                            scaleCount = 0
                            resetCarouselStars()
                            viewModel.soundGenerator.playTone(523.25f, 150L)
                        },
                        modifier = Modifier.testTag("carousel_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset carousel",
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
                // Score, HighScore, Multipliers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ОЧКИ СЛИЯНИЯ",
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

                    // Constellations formed count
                    Surface(
                        color = theme.secondaryAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark",
                                tint = theme.secondaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Созвездий: $scaleCount",
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

                // Instruction panel
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
                        text = "Вращайте карусель свайпом. Касайтесь звёзд одинакового цвета, чтобы связать их в созвездие. Соедините хотя бы 3 звезды одного цвета!",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The surreal 3D Canvas Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0x1A000000))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                // Drag changes angles
                                angleY += dragAmount.x * 0.006f
                                angleX = (angleX - dragAmount.y * 0.006f).coerceIn(-0.6f, 0.6f)
                            }
                        }
                        .pointerInput(theme) {
                            detectTapGestures { offset ->
                                // Try to hit test on projected 3D stars!
                                val centerX = canvasWidth / 2f
                                val centerY = canvasHeight / 2f
                                val focalLength = 400f
                                val d = 450f // camera distance

                                var hitStar: CarouselStar? = null
                                var minDistance = Float.MAX_VALUE

                                carouselStars.forEach { star ->
                                    val scale = focalLength / (focalLength + d + star.rotatedZ)
                                    val projectedX = centerX + star.rotatedX * scale
                                    val projectedY = centerY + star.rotatedY * scale

                                    // Hit target radius: 35dp
                                    val dx = offset.x - projectedX
                                    val dy = offset.y - projectedY
                                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                                    if (dist < 55f && dist < minDistance) {
                                        minDistance = dist
                                        hitStar = star
                                    }
                                }

                                hitStar?.let { clicked ->
                                    coroutineScope.launch {
                                        viewModel.soundGenerator.playNoteIndex(clicked.colorIndex, 250L)
                                    }

                                    if (selectedStarIds.contains(clicked.id)) {
                                        // deselect
                                        clicked.isSelected = false
                                        selectedStarIds.remove(clicked.id)
                                    } else {
                                        // Is it same class?
                                        if (selectedStarIds.isEmpty() || 
                                            carouselStars.find { it.id == selectedStarIds.first() }?.colorIndex == clicked.colorIndex
                                        ) {
                                            clicked.isSelected = true
                                            selectedStarIds.add(clicked.id)

                                            // Check cascade connection completion!
                                            val currentPoolCount = carouselStars.count { it.colorIndex == clicked.colorIndex }
                                            val currentSelectedCount = selectedStarIds.size

                                            // If we selected 3 or more of this category: offer instant fusion cascade! Or if we selected all of them!
                                            if (currentSelectedCount >= 3 && currentSelectedCount == currentPoolCount || currentSelectedCount >= 4) {
                                                // Constellation fusion SUCCESS!
                                                coroutineScope.launch {
                                                    // Beautiful arpeggio cascade
                                                    viewModel.soundGenerator.playNoteIndex(clicked.colorIndex, 150L)
                                                    delay(100L)
                                                    viewModel.soundGenerator.playNoteIndex((clicked.colorIndex + 2) % 10, 150L)
                                                    delay(100L)
                                                    viewModel.soundGenerator.playNoteIndex((clicked.colorIndex + 4) % 10, 300L)
                                                }

                                                score += currentSelectedCount * 25
                                                scaleCount++

                                                // Animate out / reset matching ones!
                                                val starListCopy = carouselStars.toList()
                                                selectedStarIds.forEach { matchedId ->
                                                    carouselStars.find { it.id == matchedId }?.let { original ->
                                                        // Replace with a new star with a different color/position
                                                        val idx = carouselStars.indexOf(original)
                                                        if (idx != -1) {
                                                            val colors = theme.bubbleColors
                                                            val colorIdx = Random.nextInt(colors.size)
                                                            carouselStars[idx] = CarouselStar(
                                                                id = original.id,
                                                                baseX = original.baseX,
                                                                baseY = original.baseY,
                                                                baseZ = original.baseZ,
                                                                color = colors[colorIdx],
                                                                colorIndex = colorIdx
                                                            )
                                                        }
                                                    }
                                                }
                                                selectedStarIds.clear()
                                            }
                                        } else {
                                            // Clicked different color - clear previous set & select new one
                                            selectedStarIds.forEach { id ->
                                                carouselStars.find { it.id == id }?.isSelected = false
                                            }
                                            selectedStarIds.clear()

                                            clicked.isSelected = true
                                            selectedStarIds.add(clicked.id)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("carousel_viewport")
                    ) {
                        canvasWidth = size.width
                        canvasHeight = size.height

                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val focalLength = 400f
                        val d = 450f // camera distance

                        // Compute current 3D rotations
                        updateRotations(size.width, size.height)

                        // 1. Draw glowing carousel vertical spindle/core in 3D perspective
                        // We draw a cylindrical neon rod in the center from Y=-300 to Y=300
                        val coreScaleTop = focalLength / (focalLength + d + 0f)
                        val coreScaleBottom = focalLength / (focalLength + d + 0f)

                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(theme.primaryAccent.copy(alpha = 0.1f), theme.secondaryAccent.copy(alpha = 0.5f), theme.primaryAccent.copy(alpha = 0.1f))
                            ),
                            start = Offset(centerX, centerY - 250f * coreScaleTop),
                            end = Offset(centerX, centerY + 250f * coreScaleBottom),
                            strokeWidth = 12f
                        )

                        // Subtle orbital ring rings
                        for (ringIdx in 1..2) {
                            val ringRadius = 220f
                            val ringSegments = 24
                            val rPath = Path()
                            for (seg in 0..ringSegments) {
                                val sAngle = (seg.toFloat() / ringSegments) * (2.0 * Math.PI)
                                val rx = (ringRadius * cos(sAngle)).toFloat()
                                val rz = (ringRadius * sin(sAngle)).toFloat()
                                val ry = (ringIdx - 1.51f) * 200f // vertical level

                                // Rotate ring point around Y and X angles
                                val rxY = rx * cos(angleY) - rz * sin(angleY)
                                val rzY = rx * sin(angleY) + rz * cos(angleY)
                                val ryX = ry * cos(angleX) - rzY * sin(angleX)
                                val rzX = ry * sin(angleX) + rzY * cos(angleX)

                                val scale = focalLength / (focalLength + d + rzX)
                                val px = centerX + rxY * scale
                                val py = centerY + ryX * scale

                                if (seg == 0) rPath.moveTo(px, py) else rPath.lineTo(px, py)
                            }
                            drawPath(
                                path = rPath,
                                color = theme.secondaryAccent.copy(alpha = 0.15f),
                                style = Stroke(width = 2.5f)
                            )
                        }

                        // 2. Draw active constellation lines between SELECTED STARS
                        if (selectedStarIds.size > 1) {
                            for (xI in 0 until selectedStarIds.lastIndex) {
                                val starA = carouselStars.find { it.id == selectedStarIds[xI] }
                                val starB = carouselStars.find { it.id == selectedStarIds[xI + 1] }

                                if (starA != null && starB != null) {
                                    val scaleA = focalLength / (focalLength + d + starA.rotatedZ)
                                    val pxA = centerX + starA.rotatedX * scaleA
                                    val pyA = centerY + starA.rotatedY * scaleA

                                    val scaleB = focalLength / (focalLength + d + starB.rotatedZ)
                                    val pxB = centerX + starB.rotatedX * scaleB
                                    val pyB = centerY + starB.rotatedY * scaleB

                                    // Glowing neon laser beam
                                    drawLine(
                                        color = theme.primaryAccent.copy(alpha = 0.5f),
                                        start = Offset(pxA, pyA),
                                        end = Offset(pxB, pyB),
                                        strokeWidth = 9f
                                    )
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(pxA, pyA),
                                        end = Offset(pxB, pyB),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }

                        // 3. Draw Stars sorting by Z depth (painter's algorithm!)
                        val sortedStars = carouselStars.toList().sortedByDescending { it.rotatedZ }

                        sortedStars.forEach { star ->
                            val scale = focalLength / (focalLength + d + star.rotatedZ)
                            val px = centerX + star.rotatedX * scale
                            val py = centerY + star.rotatedY * scale

                            // Scaled size of the geometric star node
                            val starRadiusBase = 18f
                            val pulseMultiplier = 1.0f + 0.3f * sin(star.pulse)
                            val pRadius = starRadiusBase * scale * pulseMultiplier

                            // Glow backplate
                            drawCircle(
                                color = star.color.copy(alpha = 0.25f),
                                radius = pRadius * 2.2f,
                                center = Offset(px, py)
                            )

                            // Drawn shape: Draw a neat glowing diamond (diamond outline / cross lines)
                            val outerColor = if (star.isSelected) Color.White else star.color
                            val innerColor = if (star.isSelected) theme.primaryAccent else Color.White

                            val diamondPath = Path()
                            diamondPath.moveTo(px, py - pRadius * 1.5f) // top
                            diamondPath.lineTo(px + pRadius * 1.2f, py) // right
                            diamondPath.lineTo(px, py + pRadius * 1.5f) // bottom
                            diamondPath.lineTo(px - pRadius * 1.2f, py) // left
                            diamondPath.close()

                            drawPath(
                                path = diamondPath,
                                color = outerColor,
                                style = Stroke(width = 4f)
                            )

                            // Inner core shape
                            drawCircle(
                                color = innerColor,
                                radius = pRadius * 0.45f,
                                center = Offset(px, py)
                            )

                            // Tiny sparkling cross hairs:
                            drawLine(
                                color = outerColor.copy(alpha = 0.8f),
                                start = Offset(px, py - pRadius * 2.2f),
                                end = Offset(px, py + pRadius * 2.2f),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = outerColor.copy(alpha = 0.8f),
                                start = Offset(px - pRadius * 2.2f, py),
                                end = Offset(px + pRadius * 2.2f, py),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
