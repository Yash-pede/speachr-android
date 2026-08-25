package com.yash.speachr.ui.screens.settings

import android.content.Context
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yash.speachr.R
import com.yash.speachr.core.billing.SubscriptionViewModel
import com.yash.speachr.core.permissions.PermissionViewModel
import com.yash.speachr.ui.theme.*
import org.koin.androidx.compose.koinViewModel

enum class ToneStrategy { AUTO, GLOBAL }
enum class ManualTone { CASUAL, PROFESSIONAL, NEUTRAL }

@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit,
    permissionViewModel: PermissionViewModel = koinViewModel(),
    subscriptionViewModel: SubscriptionViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val userSettingsSharedPerfs = remember {
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
    }

    // Permission States
    val micGranted by permissionViewModel.micGranted.collectAsStateWithLifecycle()
    val overlayGranted by permissionViewModel.overlayGranted.collectAsStateWithLifecycle()
    val batteryIgnored by permissionViewModel.batteryIgnored.collectAsStateWithLifecycle()
    val accessibilityGranted by permissionViewModel.accessibilityGranted.collectAsStateWithLifecycle()

    // Subscription State
    val isPro = subscriptionViewModel.isPro

    // Floating Bubble Settings
    var bubbleSize by remember {
        mutableStateOf(userSettingsSharedPerfs.getFloat("bubble_size", 1.0f))
    }
    var bubbleAlpha by remember {
        mutableStateOf(userSettingsSharedPerfs.getFloat("bubble_alpha", 1.0f))
    }

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
                permissionViewModel.checkPermissions()
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

        // --- Voice & Tone Section ---
        VoiceToneSettings(
            toneStrategy = toneStrategy,
            onToneStrategyChange = { toneStrategy = it },
            manualTone = manualTone,
            onManualToneChange = { manualTone = it },
            autoPunctuation = autoPunctuation,
            onAutoPunctuationChange = { autoPunctuation = it }
        )

        // --- Permissions Section ---
        SystemPermissionsSettings(
            micGranted = micGranted,
            overlayGranted = overlayGranted,
            batteryIgnored = batteryIgnored,
            accessibilityGranted = accessibilityGranted,
            onPermissionClick = { permissionType ->
                when (permissionType) {
                    PermissionType.MIC -> permissionViewModel.openMicSettings(context)
                    PermissionType.OVERLAY -> permissionViewModel.openOverlaySettings(context)
                    PermissionType.BATTERY -> permissionViewModel.openBatterySettings(context)
                    PermissionType.ACCESSIBILITY -> permissionViewModel.openAccessibilitySettings(context)
                }
            }
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
    SettingsGroupCard(title = "Floating Bubble") {
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
private fun VoiceToneSettings(
    toneStrategy: ToneStrategy,
    onToneStrategyChange: (ToneStrategy) -> Unit,
    manualTone: ManualTone,
    onManualToneChange: (ManualTone) -> Unit,
    autoPunctuation: Boolean,
    onAutoPunctuationChange: (Boolean) -> Unit
) {
    SettingsGroupCard(title = "Voice & Tone") {
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

enum class PermissionType { MIC, OVERLAY, BATTERY, ACCESSIBILITY }

@Composable
private fun SystemPermissionsSettings(
    micGranted: Boolean,
    overlayGranted: Boolean,
    batteryIgnored: Boolean,
    accessibilityGranted: Boolean,
    onPermissionClick: (PermissionType) -> Unit
) {
    SettingsGroupCard(title = "System Permissions") {
        SettingLinkRow(
            icon = R.drawable.mic_24px,
            title = "Microphone",
            status = if (micGranted) "Granted" else "Missing",
            statusColor = if (micGranted) Color(0xFF4CAF50) else Coral40,
            onClick = { onPermissionClick(PermissionType.MIC) }
        )
        SettingLinkRow(
            icon = R.drawable.settings_voice_24px,
            title = "Display Over Apps",
            status = if (overlayGranted) "Granted" else "Missing",
            statusColor = if (overlayGranted) Color(0xFF4CAF50) else Coral40,
            onClick = { onPermissionClick(PermissionType.OVERLAY) }
        )
        SettingLinkRow(
            icon = R.drawable.battery_full_24px,
            title = "Battery Optimization",
            status = if (batteryIgnored) "Optimized" else "Required",
            statusColor = if (batteryIgnored) Color(0xFF4CAF50) else Gold40,
            onClick = { onPermissionClick(PermissionType.BATTERY) }
        )
        SettingLinkRow(
            icon = R.drawable.grain_24px,
            title = "Accessibility Service",
            status = if (accessibilityGranted) "Active" else "Required",
            statusColor = if (accessibilityGranted) Color(0xFF4CAF50) else Coral40,
            onClick = { onPermissionClick(PermissionType.ACCESSIBILITY) }
        )
    }
}

@Composable
private fun DataPrivacySettings(
    autoDeleteHistory: Boolean,
    onAutoDeleteHistoryChange: (Boolean) -> Unit
) {
    SettingsGroupCard(title = "Data & Privacy") {
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
private fun SettingsGroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Coral40, modifier = Modifier.padding(bottom = 16.dp))
        content()
    }
}

@Composable
private fun SettingToggleRow(@DrawableRes icon: Int, title: String, subtitle: String, isChecked: Boolean, onToggleChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(icon), contentDescription = title, tint = Neutral10, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Neutral10)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Neutral30)
        }
        Switch(checked = isChecked, onCheckedChange = onToggleChange, colors = SwitchDefaults.colors(checkedThumbColor = Neutral99, checkedTrackColor = Coral40))
    }
}

@Composable
private fun SettingLinkRow(@DrawableRes icon: Int, title: String, status: String, statusColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(icon), contentDescription = title, tint = Neutral10, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.SemiBold, color = Neutral10, modifier = Modifier.weight(1f))
        Text(status, color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(painter = painterResource(R.drawable.chevron_right_24px), contentDescription = "Open", tint = Neutral30.copy(alpha = 0.5f))
    }
}

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

@Composable
private fun SelectablePill(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor = if (isSelected) Coral40 else Neutral99.copy(alpha = 0.5f)
    val textColor = if (isSelected) Neutral99 else Neutral10
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, if (isSelected) Color.Transparent else Neutral30.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp)
    }
}
