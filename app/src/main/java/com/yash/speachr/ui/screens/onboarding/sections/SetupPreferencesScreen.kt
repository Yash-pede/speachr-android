package com.yash.speachr.ui.screens.onboarding.sections

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral17
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral99
import androidx.core.content.edit
import com.yash.speachr.core.model.ManualTone
import com.yash.speachr.core.model.ToneStrategy


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupPreferencesScreen(
    onFinish: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var toneStrategy by remember { mutableStateOf(ToneStrategy.AUTO) }
    var manualTone by remember { mutableStateOf(ManualTone.PROFESSIONAL) }
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    val userSettingsSharedPerfs =
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    // Entry animation
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnim = true
    }
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "entryScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .scale(scale)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header ---
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 24.dp),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                repeat(4) { index ->
//                    val isCompleted = index <= 3
//                    Box(
//                        modifier = Modifier
//                            .padding(horizontal = 4.dp)
//                            .height(6.dp)
//                            .weight(1f)
//                            .clip(CircleShape)
//                            .background(if (isCompleted) Coral40 else Neutral30.copy(alpha = 0.2f))
//                    )
//                }
//            }

            Text(
                text = "Personalize Speachr",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Neutral10,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Select your language and dictate how Speachr should sound in your favorite apps.",
                style = MaterialTheme.typography.bodyLarge,
                color = Neutral30,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Language Section ---
            SectionHeader(title = "Language")

            Spacer(modifier = Modifier.height(16.dp))

            // Available Languages
            val availableLanguages = listOf(
                "English",
                "हिन्दी (Hindi)",
                "Deutsch (German)",
                "Español (Spanish)",
                "Français (French)"
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                availableLanguages.forEach { lang ->
                    SelectablePill(
                        text = lang,
                        isSelected = selectedLanguage == lang,
                        enabled = true,
                        onClick = { selectedLanguage = lang }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Coming Soon Languages
            val comingSoonLanguages =
                listOf("日本語 (Japanese)", "中文 (Mandarin)", "العربية (Arabic)")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                comingSoonLanguages.forEach { lang ->
                    SelectablePill(
                        text = "$lang • Soon",
                        isSelected = false,
                        enabled = false,
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- Tone Strategy Section ---
            SectionHeader(title = "Voice & Style")

            Spacer(modifier = Modifier.height(16.dp))

            // Strategy Cards
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StrategyCard(
                    title = "Smart Auto-Detect",
                    description = "Speachr matches the tone of the app you're using (Casual on WhatsApp, Professional on Gmail).",
                    isSelected = toneStrategy == ToneStrategy.AUTO,
                    onClick = { toneStrategy = ToneStrategy.AUTO }
                )

                StrategyCard(
                    title = "Global Default",
                    description = "Use the same tone everywhere, regardless of the app you're typing in.",
                    isSelected = toneStrategy == ToneStrategy.GLOBAL,
                    onClick = { toneStrategy = ToneStrategy.GLOBAL }
                )
            }

            // Expandable Manual Tone Selection
            AnimatedVisibility(
                visible = toneStrategy == ToneStrategy.GLOBAL,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(
                        text = "Choose your default tone:",
                        style = MaterialTheme.typography.titleSmall,
                        color = Neutral10,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ManualTone.entries.forEach { tone ->
                            SelectablePill(
                                text = tone.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                isSelected = manualTone == tone,
                                enabled = true,
                                onClick = { manualTone = tone },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Finish Button ---
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
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onFinish()
                        userSettingsSharedPerfs.edit {
                            putString("language", selectedLanguage)
                                .putString("tone", toneStrategy.toString()).putString(
                                    "manualtone",
                                    manualTone.toString()
                                )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue",
                    color = Neutral99,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Neutral10
        )
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(Neutral30.copy(alpha = 0.2f))
        )
    }
}


// ------------------------------------------------------------------------------------------------
// Reusable Selectable Pill (For Languages & Tones)
// ------------------------------------------------------------------------------------------------
@Composable
fun SelectablePill(
    text: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> Coral40
            enabled -> AppTheme.glassColors.surfaceSubtle
            else -> Neutral30.copy(alpha = 0.05f)
        }, label = "pillBg"
    )

    val borderColor = when {
        isSelected -> Color.Transparent
        enabled -> AppTheme.glassColors.border
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Neutral99
        enabled -> Neutral10
        else -> Neutral30.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Reusable Strategy Card (For Auto vs Global)
// ------------------------------------------------------------------------------------------------
@Composable
fun StrategyCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_anim")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val borderAlpha by animateFloatAsState(
        if (isSelected) pulseAlpha else 1f,
        label = "borderAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.glassColors.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Coral40.copy(alpha = borderAlpha) else AppTheme.glassColors.border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Neutral10
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral30,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Custom Radio Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Coral40 else Color.Transparent)
                    .border(
                        2.dp,
                        if (isSelected) Color.Transparent else Neutral30.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Neutral99)
                    )
                }
            }
        }
    }
}
