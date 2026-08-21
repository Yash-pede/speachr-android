package com.yash.speachr.ui.screens.onboarding.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.yash.speachr.ui.theme.Neutral99
import kotlinx.coroutines.launch

@Composable
fun PermissionsOnboardingScreen(
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Top Progress Bar ---
            val progress by animateFloatAsState(
                targetValue = (pagerState.currentPage + 1) / 4f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                label = "progress"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Neutral30.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Coral40)
                )
            }

            // --- Pager Content ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                pageSpacing = 24.dp
            ) { page ->
                val titles = listOf(
                    "Microphone Access",
                    "Display Over Apps",
                    "Battery Optimization",
                    "Accessibility Service"
                )
                val bodies = listOf(
                    "Speachr needs to hear your voice to transcribe it perfectly. Your audio is never stored.",
                    "Allows the Speachr Bubble to float seamlessly over your keyboard and other apps.",
                    "Prevents your phone from pausing Speachr in the background for uninterrupted dictation.",
                    "Reads text fields and inserts your dictation automatically, perfectly formatted every time."
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Custom Drawn Canvas Icons
                    when (page) {
                        0 -> MicGraphic()
                        1 -> WindowGraphic()
                        2 -> BatteryGraphic()
                        3 -> AccessibilityGraphic()
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Optional Badge
                    if (page == 2) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Gold40.copy(alpha = 0.2f))
                                .border(1.dp, Gold40, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "OPTIONAL",
                                color = Gold40,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Dynamic Text
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            fadeIn(tween(400)) togetherWith fadeOut(tween(150))
                        },
                        label = "text_anim"
                    ) { targetPage ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = titles[targetPage],
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Neutral10,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = bodies[targetPage],
                                style = MaterialTheme.typography.bodyLarge,
                                color = Neutral30,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Bottom Action Area ---
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "buttonScale"
            )

            val isLastPage = pagerState.currentPage == 3
            val isOptionalPage = pagerState.currentPage == 2

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(buttonScale)
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Neutral17)
                    .clickable(interactionSource = interactionSource, indication = null) {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLastPage) "Finish Setup" else "Allow",
                    color = Neutral99,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Skip button for Optional page
            if (isOptionalPage) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Skip for now",
                    color = Neutral30,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Custom Canvas Graphics for Permissions
// ------------------------------------------------------------------------------------------------

@Composable
private fun MicGraphic() {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(AppTheme.glassColors.surface)
            .border(2.dp, Coral40, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            val micWidth = size.width * 0.4f
            val micHeight = size.height * 0.5f

            // Mic body
            drawRoundRect(
                color = Coral40,
                topLeft = Offset((size.width - micWidth) / 2, size.height * 0.1f),
                size = Size(micWidth, micHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(micWidth / 2, micWidth / 2)
            )
            // Mic stand
            drawLine(
                color = Coral40,
                start = Offset(size.width * 0.3f, size.height * 0.75f),
                end = Offset(size.width * 0.7f, size.height * 0.75f),
                strokeWidth = 4f
            )
            // Mic arc
            drawArc(
                color = Coral40,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(size.width * 0.15f, size.height * 0.35f),
                size = Size(size.width * 0.7f, size.height * 0.7f),
                style = Stroke(width = 4f)
            )
        }

        // Pulse rings
        Canvas(modifier = Modifier
            .fillMaxSize()
            .scale(pulse)) {
            drawCircle(
                color = Coral40.copy(alpha = 0.2f),
                radius = size.minDimension / 2
            )
        }
    }
}

@Composable
private fun WindowGraphic() {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(AppTheme.glassColors.surface)
            .border(2.dp, Coral40, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(70.dp)) {
            // Background Window
            drawRoundRect(
                color = Neutral30.copy(alpha = 0.5f),
                topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                size = Size(size.width * 0.6f, size.height * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            // Foreground Window
            drawRoundRect(
                color = Coral40,
                topLeft = Offset(size.width * 0.4f, size.height * 0.4f),
                size = Size(size.width * 0.5f, size.height * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
    }
}

@Composable
private fun BatteryGraphic() {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(AppTheme.glassColors.surface)
            .border(2.dp, Gold40, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(70.dp)) {
            // Battery body
            drawRoundRect(
                color = Neutral10,
                topLeft = Offset(size.width * 0.1f, size.height * 0.2f),
                size = Size(size.width * 0.7f, size.height * 0.6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(width = 4f)
            )
            // Battery terminal
            drawRoundRect(
                color = Neutral10,
                topLeft = Offset(size.width * 0.8f, size.height * 0.35f),
                size = Size(size.width * 0.1f, size.height * 0.3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            // Battery fill
            drawRoundRect(
                color = Gold40,
                topLeft = Offset(size.width * 0.15f, size.height * 0.25f),
                size = Size(size.width * 0.55f, size.height * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
private fun AccessibilityGraphic() {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(AppTheme.glassColors.surface)
            .border(2.dp, Coral40, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(60.dp)) {
            // Head
            drawCircle(
                color = Coral40,
                radius = size.width * 0.12f,
                center = Offset(size.width * 0.5f, size.height * 0.15f)
            )
            // Body & Arms (Path)
            val bodyPath = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.3f) // Neck
                lineTo(size.width * 0.5f, size.height * 0.7f)  // Spine

                // Left Arm
                moveTo(size.width * 0.2f, size.height * 0.35f)
                lineTo(size.width * 0.8f, size.height * 0.35f)

                // Left Leg
                moveTo(size.width * 0.5f, size.height * 0.7f)
                lineTo(size.width * 0.2f, size.height * 0.9f)

                // Right Leg
                moveTo(size.width * 0.5f, size.height * 0.7f)
                lineTo(size.width * 0.8f, size.height * 0.9f)
            }
            drawPath(
                path = bodyPath,
                color = Coral40,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
    }
}