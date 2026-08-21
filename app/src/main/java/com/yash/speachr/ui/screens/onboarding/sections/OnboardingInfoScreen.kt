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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Coral80
import com.yash.speachr.ui.theme.Gold40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral17
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral99
import kotlinx.coroutines.delay

@Composable
fun OnboardingInfoScreen(
    onNextClick: () -> Unit
) {
    // Entry Animation State
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startAnim = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(800),
        label = "cardAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300),
        label = "textAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Top Visual Card ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .scale(cardScale)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Coral40, Coral80)
                        )
                    )
            ) {
                // Animated Voice Waveform inside the card
                VoiceWaveformAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            }

            // --- Text Content ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Speak naturally.\nLet Speachr handle the rest.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Neutral10,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Turn your voice into perfectly formatted text in every app, instantly.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Neutral30,
                    textAlign = TextAlign.Center
                )
            }

            // --- Page Indicator Dots ---
            if (startAnim) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(3) { index ->
                        val color = if (index == 0) Coral40 else Neutral30.copy(alpha = 0.3f)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == 0) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }

            // --- Next Button ---
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.95f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "buttonScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(buttonScale)
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Neutral17)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onNextClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Next",
                    color = Neutral99,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Custom Animated Voice Waveform
// ------------------------------------------------------------------------------------------------
@Composable
private fun VoiceWaveformAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")

    // Create 7 dynamic bars with different animation specs for an organic voice pulse
    val scales = (1..7).map { index ->
        val duration = 400 + (index * 80)
        val startDelay = index * 50L
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration,
                    delayMillis = startDelay.toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barCount = scales.size
        val spacing = 12f
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (canvasWidth - totalSpacing) / barCount
        val maxHeight = canvasHeight * 0.8f

        scales.forEachIndexed { index, scale ->
            val barHeight = maxHeight * scale.value
            val x = (barWidth + spacing) * index
            val y = (canvasHeight - barHeight) / 2f

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}