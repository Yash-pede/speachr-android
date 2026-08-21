package com.yash.speachr.ui.screens.onboarding.sections

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Coral80
import com.yash.speachr.ui.theme.Gold40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral17
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral90
import com.yash.speachr.ui.theme.Neutral99
import com.yash.speachr.ui.theme.Taupe40
import kotlinx.coroutines.delay

@Composable
fun LoginOnboarding(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    // Animations for background floating orbs
    val coralX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "coralX"
    )
    val coralY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "coralY"
    )

    // Entry animation states
    var startEntryAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startEntryAnim = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (startEntryAnim) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (startEntryAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "cardAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99),
        contentAlignment = Alignment.Center
    ) {
        // --- Animated Background Orbs ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Coral Orb
            drawCircle(
                color = Coral40.copy(alpha = 0.25f),
                radius = width * 0.5f,
                center = Offset(
                    x = width * (0.2f + coralX * 0.4f),
                    y = height * (0.2f + coralY * 0.4f)
                )
            )
            // Gold Orb
            drawCircle(
                color = Gold40.copy(alpha = 0.20f),
                radius = width * 0.45f,
                center = Offset(
                    x = width * (0.8f - coralX * 0.4f),
                    y = height * (0.8f - coralY * 0.4f)
                )
            )
            // Taupe Orb
            drawCircle(
                color = Taupe40.copy(alpha = 0.15f),
                radius = width * 0.3f,
                center = Offset(
                    x = width * (0.5f + coralX * 0.2f),
                    y = height * (0.5f - coralY * 0.2f)
                )
            )
        }

        // --- Main Frosted Glass Card ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .scale(cardScale)
                .clip(RoundedCornerShape(36.dp))
                .background(AppTheme.glassColors.surface)
                .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(36.dp))
//                .blur(20.dp) // Native blur fallback for glass effect
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                // --- Custom Animated App Logo ---
                SpeachrLogo(modifier = Modifier.size(100.dp))

                // --- Text Content ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Speachr",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Neutral10
                    )
                    Text(
                        text = "Where words come to life.\nSign in to start your journey.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral30,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Google Login Button ---
                GoogleLoginButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Footer ---
                Text(
                    text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral30.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Custom Google "G" Logo drawn via Canvas
// ------------------------------------------------------------------------------------------------
@Composable
private fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val strokeWidth = canvasSize * 0.1f
        val radius = canvasSize / 2 - strokeWidth / 2

        // Colors
        val blue = Color(0xFF4285F4)
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)

        // 1. Red (Top right curve)
        drawArc(
            color = red,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(canvasSize - strokeWidth * 2, canvasSize - strokeWidth * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // 2. Yellow (Top left curve)
        drawArc(
            color = yellow,
            startAngle = 90f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(canvasSize - strokeWidth * 2, canvasSize - strokeWidth * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // 3. Green (Bottom left curve)
        drawArc(
            color = green,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(canvasSize - strokeWidth * 2, canvasSize - strokeWidth * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // 4. Blue (Bottom right curve)
        drawArc(
            color = blue,
            startAngle = 225f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(canvasSize - strokeWidth * 2, canvasSize - strokeWidth * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // 5. Blue Crossbar
        val crossbarPath = Path().apply {
            // Drawing a thick line across the center
            val centerY = canvasSize / 2
            moveTo(centerY - radius * 0.2f, centerY)
            lineTo(centerY + radius * 0.8f, centerY)
        }
        drawPath(
            path = crossbarPath,
            color = blue,
            style = Stroke(width = strokeWidth * 0.9f, cap = StrokeCap.Round)
        )
    }
}

// ------------------------------------------------------------------------------------------------
// The Google Login Button
// ------------------------------------------------------------------------------------------------
@Composable
private fun GoogleLoginButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral99)
            .border(1.dp, Neutral30.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            GoogleLogoIcon(modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Continue with Google",
                color = Neutral10,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Custom Animated Speachr Logo (Soundwaves inside a rounded coral background)
// ------------------------------------------------------------------------------------------------
@Composable
private fun SpeachrLogo(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "logo_anim")

    val animat1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val animat2 by transition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            tween(500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val animat3 by transition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val animat4 by transition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bar4"
    )

    val scales = listOf(animat1, animat2, animat3, animat4)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Coral40, Coral80)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)) {
            val barCount = 4
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / (barCount * 2 - 1)
            val maxBarHeight = canvasHeight

            scales.forEachIndexed { index, scale ->
                val barHeight = maxBarHeight * scale
                val x = barWidth * (index * 2)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(x, (canvasHeight - barHeight) / 2),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        barWidth / 2,
                        barWidth / 2
                    )
                )
            }
        }
    }
}