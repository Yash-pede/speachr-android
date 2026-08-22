package com.yash.speachr.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Coral80
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral17
import com.yash.speachr.ui.theme.Neutral99
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
) {
    var startAnim by remember { mutableStateOf(false) }

    // Trigger animations and navigate after completion
    LaunchedEffect(Unit) {
        delay(100) // Slight delay to let the UI settle
        startAnim = true
        delay(2200) // Let the animation play out
//        onNavigateNext()
    }

    // --- Animation States ---
    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logoAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "textAlpha"
    )
    val textOffset by animateFloatAsState(
        targetValue = if (startAnim) 0f else 30f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "textOffset"
    )

    // Infinite ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "rippleAlpha"
    )

    // Infinite soundwave animation
    val bar1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "bar1")
    val bar2 by infiniteTransition.animateFloat(1f, 0.4f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "bar2")
    val bar3 by infiniteTransition.animateFloat(0.5f, 0.9f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "bar3")
    val bar4 by infiniteTransition.animateFloat(0.8f, 0.2f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "bar4")
    val scales = listOf(bar1, bar2, bar3, bar4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Neutral17, Neutral10)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Logo Container ---
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Coral40, Coral80)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // --- Ripple Effect ---
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    drawCircle(
                        color = Coral40.copy(alpha = rippleAlpha),
                        radius = (canvasSize / 2) * rippleScale,
                        style = Stroke(width = 4f)
                    )
                }

                // --- Frosted Glass Inner Circle ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(AppTheme.glassColors.surface)
                        .border(1.dp, AppTheme.glassColors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // --- Animated Soundwave ---
                    Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        val barCount = 4
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val spacing = 8f
                        val totalSpacing = spacing * (barCount - 1)
                        val barWidth = (canvasWidth - totalSpacing) / barCount
                        val maxHeight = canvasHeight * 0.8f

                        scales.forEachIndexed { index, scaleValue ->
                            val barHeight = maxHeight * scaleValue
                            val x = (barWidth + spacing) * index
                            val y = (canvasHeight - barHeight) / 2f

                            drawRoundRect(
                                color = Neutral99,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- App Name ---
            Text(
                text = "Speachr",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Neutral99,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = textOffset.dp)
                    .graphicsLayer(alpha = textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- Tagline ---
            Text(
                text = "Speak naturally. Type perfectly.",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Neutral99.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = textOffset.dp)
                    .graphicsLayer(alpha = textAlpha)
            )
        }
    }
}