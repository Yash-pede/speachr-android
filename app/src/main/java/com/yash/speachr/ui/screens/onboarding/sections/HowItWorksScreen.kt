package com.yash.speachr.ui.screens.onboarding.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.R
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Coral80
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral17
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral99
import kotlinx.coroutines.launch

@Composable
fun HowItWorksScreen(
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

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
                targetValue = (pagerState.currentPage + 1) / 2f,
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

            // --- Pager Content (Swipeable Area) ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                pageSpacing = 24.dp
            ) { page ->
                when (page) {
                    0 -> AppLogoPlaceholder()
                    1 -> KeyboardImagePlaceholder()
                }
            }

            // --- Dynamic Text Area ---
            val titles = listOf("Meet the Speachr Bubble", "Works in every app")
            val bodies = listOf(
                "A floating voice assistant that appears wherever you type.",
                "Tap the bubble to dictate perfectly formatted text, instantly."
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

            // --- Next Button ---
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1f,
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
                    .clickable(interactionSource = interactionSource, indication = null) {
                        if (pagerState.currentPage == 1) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pagerState.currentPage == 1) "Get Started" else "Next",
                    color = Neutral99,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Placeholders for your images. Replace the Canvas/Box inside with:
// Image(painter = painterResource(id = R.drawable.your_logo), contentDescription = null)
// ------------------------------------------------------------------------------------------------

@Composable
private fun AppLogoPlaceholder() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(AppTheme.glassColors.surface)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(Coral40, Coral80)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.main_logo),
                contentDescription = "Speachr logo"
            )
        }
    }
}

// app/src/main/java/com/yash/spechr/feature/onboarding/OnboardingScreen.kt

@Composable
private fun KeyboardImagePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.5f)
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.keyboard),
                contentDescription = "Keyboard",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}