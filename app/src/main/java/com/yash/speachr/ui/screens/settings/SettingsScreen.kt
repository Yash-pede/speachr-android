package com.yash.speachr.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yash.speachr.R
import com.yash.speachr.core.billing.SubscriptionViewModel
import com.yash.speachr.core.model.BubblePauseStore
import com.yash.speachr.core.model.ManualTone
import com.yash.speachr.core.model.TargetLanguageStore
import com.yash.speachr.core.model.ToneStrategy
import com.yash.speachr.services.FloatingService
import com.yash.speachr.ui.components.SectionCard
import com.yash.speachr.ui.components.SelectablePill
import com.yash.speachr.ui.components.SettingToggleRow
import com.yash.speachr.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import org.koin.androidx.compose.koinViewModel


@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    subscriptionViewModel: SubscriptionViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val userSettingsSharedPerfs = remember {
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    }

    // Subscription State
    val isPro = subscriptionViewModel.isPro

    // Floating Bubble Settings
    var bubbleSize by remember {
        mutableStateOf(userSettingsSharedPerfs.getFloat("bubble_size", 1.0f))
    }
    var bubbleAlpha by remember {
        mutableStateOf(userSettingsSharedPerfs.getFloat("bubble_alpha", 1.0f))
    }

    // Output language lives in a shared store so it stays in sync with the picker screen.
    val language by TargetLanguageStore.language.collectAsStateWithLifecycle()

    // Voice & Tone Settings
    var toneStrategy by remember {
        mutableStateOf(
            ToneStrategy.valueOf(
                userSettingsSharedPerfs.getString("tone", ToneStrategy.AUTO.name) ?: ToneStrategy.AUTO.name
            )
        )
    }
    var manualTone by remember {
        mutableStateOf(
            ManualTone.valueOf(
                userSettingsSharedPerfs.getString("manualtone", ManualTone.PROFESSIONAL.name) ?: ManualTone.PROFESSIONAL.name
            )
        )
    }
    var autoPunctuation by remember {
        mutableStateOf(userSettingsSharedPerfs.getBoolean("auto_punctuation", true))
    }
    var autoDeleteHistory by remember {
        mutableStateOf(userSettingsSharedPerfs.getBoolean("auto_delete_history", false))
    }

    // Sync settings to SharedPreferences
    LaunchedEffect(bubbleSize, bubbleAlpha, toneStrategy, manualTone, autoPunctuation, autoDeleteHistory) {
        userSettingsSharedPerfs.edit {
            putFloat("bubble_size", bubbleSize)
            putFloat("bubble_alpha", bubbleAlpha)
            putString("tone", toneStrategy.name)
            putString("manualtone", manualTone.name)
            putBoolean("auto_punctuation", autoPunctuation)
            putBoolean("auto_delete_history", autoDeleteHistory)
        }
    }

    // Re-check permissions when returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                subscriptionViewModel.updateCustomerInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Neutral10
        )

        // --- Premium Section ---
        if (!isPro) {
            PremiumUpgradeCard(onUpgradeClick = onNavigateToPaywall)
        }

        // --- Floating Bubble Section ---
        FloatingBubbleSettings(
            bubbleSize = bubbleSize,
            onSizeChange = { bubbleSize = it },
            bubbleAlpha = bubbleAlpha,
            onAlphaChange = { bubbleAlpha = it }
        )

        // --- Pause Bubble Section ---
        BubblePauseSettings()

        // --- Language Section ---
        LanguageSettingRow(
            selectedLanguage = language,
            onClick = onNavigateToLanguage
        )

        // --- Voice & Tone Section ---
        VoiceToneSettings(
            toneStrategy = toneStrategy,
            onToneStrategyChange = { toneStrategy = it },
            manualTone = manualTone,
            onManualToneChange = { manualTone = it },
            autoPunctuation = autoPunctuation,
            onAutoPunctuationChange = { autoPunctuation = it }
        )

        // --- Privacy Section ---
        DataPrivacySettings(
            autoDeleteHistory = autoDeleteHistory,
            onAutoDeleteHistoryChange = { autoDeleteHistory = it }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ------------------------------------------------------------------------------------------------
// Modular Components
// ------------------------------------------------------------------------------------------------

@Composable
private fun PremiumUpgradeCard(onUpgradeClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = listOf(Coral40, Coral80)))
            .clickable { onUpgradeClick() }
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.bolt_24px),
                contentDescription = "Premium",
                tint = Neutral99,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Speachr Pro", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Neutral99)
                Text("Unlimited dictation & advanced models", fontSize = 14.sp, color = Neutral99.copy(alpha = 0.8f))
            }
            Icon(painter = painterResource(R.drawable.chevron_right_24px), contentDescription = "Go", tint = Neutral99)
        }
    }
}

@Composable
private fun FloatingBubbleSettings(
    bubbleSize: Float,
    onSizeChange: (Float) -> Unit,
    bubbleAlpha: Float,
    onAlphaChange: (Float) -> Unit
) {
    SectionCard(title = "Floating Bubble") {
        // Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Neutral17.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((56 * bubbleSize).dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Coral40, Coral80)), alpha = bubbleAlpha)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Size: ${String.format("%.0f", bubbleSize * 100)}%", fontWeight = FontWeight.SemiBold)
        Slider(
            value = bubbleSize,
            onValueChange = onSizeChange,
            valueRange = 0.8f..1.5f,
            colors = SliderDefaults.colors(thumbColor = Coral40, activeTrackColor = Coral40)
        )

        Text("Transparency: ${String.format("%.0f", bubbleAlpha * 100)}%", fontWeight = FontWeight.SemiBold)
        Slider(
            value = bubbleAlpha,
            onValueChange = onAlphaChange,
            valueRange = 0.4f..1.0f,
            colors = SliderDefaults.colors(thumbColor = Coral40, activeTrackColor = Coral40)
        )
    }
}

@Composable
private fun BubblePauseSettings() {
    val context = LocalContext.current
    val pausedUntil by BubblePauseStore.pausedUntil.collectAsStateWithLifecycle()

    // Live countdown while a timed pause runs; also lifts expired pauses so every reader
    // (this UI, the accessibility service) agrees on the state.
    var remainingMs by remember { mutableLongStateOf(BubblePauseStore.remainingMillis() ?: 0L) }
    LaunchedEffect(pausedUntil) {
        while (true) {
            remainingMs = BubblePauseStore.remainingMillis() ?: 0L
            // A positive timestamp (unlike INDEFINITE, which is negative) means a timed pause
            // that has just lapsed — clear it so the bubble can come back.
            if (pausedUntil > 0L && remainingMs == 0L) {
                BubblePauseStore.resume(context)
            }
            delay(15.seconds)
        }
    }

    val isPaused = when {
        pausedUntil == BubblePauseStore.INDEFINITE -> true
        pausedUntil > 0L -> remainingMs > 0L
        else -> false
    }

    val statusLine = when {
        pausedUntil == BubblePauseStore.INDEFINITE ->
            "Paused — nothing will re-enable it automatically"
        pausedUntil > 0L && remainingMs > 0L ->
            "Paused · back in ${formatRemaining(remainingMs)}"
        else -> "Active — the bubble appears when you tap a text field"
    }

    SectionCard(title = "Pause Bubble") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.motion_mode_24px),
                contentDescription = null,
                tint = if (isPaused) Neutral30 else Coral40,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Background dictation", fontWeight = FontWeight.SemiBold, color = Neutral10)
                Text(statusLine, style = MaterialTheme.typography.bodySmall, color = Neutral30)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isPaused) {
            // Re-enable affordance — the only way out of an indefinite pause.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Coral40)
                    .clickable { BubblePauseStore.resume(context) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Turn bubble back on",
                    color = Neutral99,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        } else {
            Text(
                "Pausing completely stops the bubble and its background service. " +
                    "Your permissions and history are kept.",
                style = MaterialTheme.typography.bodySmall,
                color = Neutral30
            )
            Spacer(modifier = Modifier.height(12.dp))

            PauseOption(
                title = "For 1 hour",
                subtitle = "Turns itself back on",
                onClick = { pauseBubble(context, 1.hours) }
            )
            PauseOption(
                title = "For 8 hours",
                subtitle = "Turns itself back on",
                onClick = { pauseBubble(context, 8.hours) }
            )
            PauseOption(
                title = "Until tomorrow",
                subtitle = "Turns itself back on",
                onClick = { pauseBubble(context, 24.hours) }
            )
            PauseOption(
                title = "Until I turn it back on",
                subtitle = "Stays off — nothing will re-enable it for you",
                highlight = true,
                onClick = { pauseBubble(context, null) }
            )
        }
    }
}

/**
 * A tappable pause-duration row. The subtitle spells out whether the pause ends by itself,
 * which the old single-word "For now" chip left ambiguous.
 */
@Composable
private fun PauseOption(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (highlight) Coral40.copy(alpha = 0.06f) else Neutral17.copy(alpha = 0.03f)
            )
            .border(
                width = 1.dp,
                color = if (highlight) Coral40.copy(alpha = 0.35f) else AppTheme.glassColors.border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Neutral10, fontSize = 15.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Neutral30)
        }
        Icon(
            painter = painterResource(R.drawable.pause_24px),
            contentDescription = null,
            tint = if (highlight) Coral40 else Neutral30.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Stops the running foreground service, then records the pause. Pass `null` to pause until the
 * user re-enables it manually.
 */
private fun pauseBubble(context: Context, duration: Duration?) {
    context.stopService(Intent(context, FloatingService::class.java))
    BubblePauseStore.pause(context, duration?.inWholeMilliseconds)
}

private fun formatRemaining(millis: Long): String {
    val totalMinutes = (millis + 59_999) / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "under a minute"
    }
}

@Composable
private fun VoiceToneSettings(
    toneStrategy: ToneStrategy,
    onToneStrategyChange: (ToneStrategy) -> Unit,
    manualTone: ManualTone,
    onManualToneChange: (ManualTone) -> Unit,
    autoPunctuation: Boolean,
    onAutoPunctuationChange: (Boolean) -> Unit
) {
    SectionCard(title = "Voice & Tone") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StrategyCard(
                title = "Smart Auto-Detect",
                description = "Matches the tone of the app you're using.",
                isSelected = toneStrategy == ToneStrategy.AUTO,
                onClick = { onToneStrategyChange(ToneStrategy.AUTO) }
            )
            StrategyCard(
                title = "Global Default",
                description = "Use the same tone everywhere.",
                isSelected = toneStrategy == ToneStrategy.GLOBAL,
                onClick = { onToneStrategyChange(ToneStrategy.GLOBAL) }
            )
        }

        AnimatedVisibility(
            visible = toneStrategy == ToneStrategy.GLOBAL,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Choose your default tone:", style = MaterialTheme.typography.bodyMedium, color = Neutral30)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ManualTone.entries.forEach { tone ->
                        SelectablePill(
                            text = tone.name.lowercase().replaceFirstChar { it.uppercase() },
                            isSelected = manualTone == tone,
                            onClick = { onManualToneChange(tone) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SettingToggleRow(
            icon = R.drawable.graphic_eq_24px,
            title = "Auto-Punctuation",
            subtitle = "Automatically add commas and periods",
            isChecked = autoPunctuation,
            onToggleChange = onAutoPunctuationChange
        )
    }
}

@Composable
private fun LanguageSettingRow(
    selectedLanguage: String,
    onClick: () -> Unit
) {
    SectionCard(title = "Output Language") {
        Text(
            text = "Speachr will transcribe and translate your voice to this language.",
            style = MaterialTheme.typography.bodySmall,
            color = Neutral30,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Neutral17.copy(alpha = 0.04f))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLanguage,
                fontWeight = FontWeight.Bold,
                color = Neutral10,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.chevron_right_24px),
                contentDescription = "Change language",
                tint = Neutral30.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DataPrivacySettings(
    autoDeleteHistory: Boolean,
    onAutoDeleteHistoryChange: (Boolean) -> Unit
) {
    SectionCard(title = "Data & Privacy") {
        SettingToggleRow(
            icon = R.drawable.warning_24px,
            title = "Auto-Delete History",
            subtitle = "Erase transcriptions older than 30 days",
            isChecked = autoDeleteHistory,
            onToggleChange = onAutoDeleteHistoryChange
        )
    }
}

// ------------------------------------------------------------------------------------------------
// Reusable UI Components
// ------------------------------------------------------------------------------------------------

@Composable
private fun StrategyCard(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_anim")
    val pulseAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.6f, animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral99.copy(alpha = 0.5f))
            .border(width = 2.dp, color = if (isSelected) Coral40.copy(alpha = pulseAlpha) else Neutral30.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = Neutral10)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Neutral30)
            }
            RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Coral40))
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun PauseOptionsPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Neutral99)
            .padding(24.dp)
    ) {
        SectionCard(title = "Pause Bubble") {
            PauseOption(title = "For 1 hour", subtitle = "Turns itself back on", onClick = {})
            PauseOption(title = "For 8 hours", subtitle = "Turns itself back on", onClick = {})
            PauseOption(title = "Until tomorrow", subtitle = "Turns itself back on", onClick = {})
            PauseOption(
                title = "Until I turn it back on",
                subtitle = "Stays off — nothing will re-enable it for you",
                highlight = true,
                onClick = {}
            )
        }
    }
}

