package com.yash.speachr.ui.screens.home

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yash.speachr.R
import com.yash.speachr.core.database.DictationEntity
import com.yash.speachr.core.permissions.PermissionViewModel
import com.yash.speachr.ui.components.AppPermission
import com.yash.speachr.ui.screens.home.viewmodel.HomeStats
import com.yash.speachr.ui.screens.home.viewmodel.HomeViewModel
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Coral80
import com.yash.speachr.ui.theme.Gold40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral17
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral99
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Success40 = Color(0xFF4CAF50)

@Composable
fun HomeScreen(
    onSeeAllHistory: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    permissionViewModel: PermissionViewModel = koinViewModel()
) {
    val stats by viewModel.todayStats.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val micGranted by permissionViewModel.micGranted.collectAsStateWithLifecycle()
    val overlayGranted by permissionViewModel.overlayGranted.collectAsStateWithLifecycle()
    val batteryIgnored by permissionViewModel.batteryIgnored.collectAsStateWithLifecycle()
    val accessibilityGranted by permissionViewModel.accessibilityGranted.collectAsStateWithLifecycle()

    // Deliberately NOT persisted: dismissing only hides the nudge for this session, so a fresh
    // app launch (or returning to the tab) surfaces it again instead of losing it forever.
    var permissionCardDismissed by rememberSaveable { mutableStateOf(false) }

    // Recompute "today" each time the tab is opened, so an app left running past midnight
    // doesn't keep showing yesterday's numbers.
    LaunchedEffect(Unit) { viewModel.refresh() }

    // Permissions can be revoked while the app is backgrounded; re-read them on return.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionViewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Required permissions are enforced by the startup gate, so whatever is listed here is
    // normally an optional extra — this card is a nudge, not a blocker.
    val missingPermissions = buildList {
        if (!micGranted) add(AppPermission.MICROPHONE)
        if (!overlayGranted) add(AppPermission.OVERLAY)
        if (!accessibilityGranted) add(AppPermission.ACCESSIBILITY)
        if (!batteryIgnored) add(AppPermission.BATTERY)
    }
    val onlyOptionalMissing = missingPermissions.isNotEmpty() &&
        missingPermissions.all { it.optional }
    val showPermissionCard =
        missingPermissions.isNotEmpty() && !(onlyOptionalMissing && permissionCardDismissed)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { HomeHeader() }

        if (showPermissionCard) {
            item {
                PermissionsCard(
                    missing = missingPermissions,
                    onOpen = { permission ->
                        when (permission) {
                            AppPermission.MICROPHONE -> permissionViewModel.openMicSettings(context)
                            AppPermission.OVERLAY -> permissionViewModel.openOverlaySettings(context)
                            AppPermission.BATTERY -> permissionViewModel.openBatterySettings(context)
                            AppPermission.ACCESSIBILITY -> permissionViewModel.openAccessibilitySettings(context)
                        }
                    },
                    // Dismissing is only offered when nothing required is missing.
                    onDismiss = if (onlyOptionalMissing) {
                        { permissionCardDismissed = true }
                    } else null
                )
            }
        }

        item { PrivacyCard() }

        item { SectionLabel("Today") }

        item { TodayStatsRow(stats) }

        item {
            RecentHeader(
                hasItems = stats.recentDictations.isNotEmpty(),
                onSeeAll = onSeeAllHistory
            )
        }

        if (stats.recentDictations.isEmpty()) {
            item { EmptyActivityState() }
        } else {
            items(stats.recentDictations, key = { it.id }) { item ->
                TranscriptionCard(item)
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

// ------------------------------------------------------------------------------------------------
// Header
// ------------------------------------------------------------------------------------------------

@Composable
private fun HomeHeader() {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral30
            )
            Text(
                text = "Yash",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Neutral10
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Coral40, Coral80))),
            contentAlignment = Alignment.Center
        ) {
            Text("Y", color = Neutral99, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Privacy — the reassurance the screen was previously missing
// ------------------------------------------------------------------------------------------------

@Composable
private fun PrivacyCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Success40.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lock_24px),
                        contentDescription = null,
                        tint = Success40,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Private by design",
                        fontWeight = FontWeight.Bold,
                        color = Neutral10,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Your voice stays yours",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral30
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Audio is sent only to be turned into text, then discarded. " +
                    "Nothing is saved on our servers — your history lives on this device, " +
                    "and clearing it here removes it for good.",
                style = MaterialTheme.typography.bodySmall,
                color = Neutral30,
                lineHeight = 18.sp
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Stats
// ------------------------------------------------------------------------------------------------

@Composable
private fun TodayStatsRow(stats: HomeStats) {
    val animatedWords by animateIntAsState(
        targetValue = stats.totalWordsToday,
        animationSpec = tween(600),
        label = "words"
    )
    val animatedSessions by animateIntAsState(
        targetValue = stats.sessionsToday,
        animationSpec = tween(600),
        label = "sessions"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = animatedWords.toString(),
            label = "Words",
            color = Coral40
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${stats.timeSavedMinutesToday}m",
            label = "Saved",
            color = Gold40
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = animatedSessions.toString(),
            label = "Sessions",
            color = Success40
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = Neutral10
    )
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral30
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Recent activity
// ------------------------------------------------------------------------------------------------

@Composable
private fun RecentHeader(hasItems: Boolean, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionLabel("Recent Activity")
        if (hasItems) {
            Text(
                text = "See all",
                color = Coral40,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSeeAll() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyActivityState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Coral40.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.mic_24px),
                contentDescription = null,
                tint = Coral40,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No dictations yet",
            fontWeight = FontWeight.Bold,
            color = Neutral10,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Open any app, tap into a text field, then tap the Speachr bubble " +
                "and start talking. Your words appear right where you're typing.",
            style = MaterialTheme.typography.bodySmall,
            color = Neutral30,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun TranscriptionCard(item: DictationEntity) {
    val timeAgo = remember(item.timestamp) {
        val diff = System.currentTimeMillis() - item.timestamp
        when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(item.timestamp))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Coral40.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.mic_24px),
                contentDescription = null,
                tint = Coral40,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${item.wordCount} words", fontWeight = FontWeight.Bold, color = Neutral10)
                Text(timeAgo, style = MaterialTheme.typography.bodySmall, color = Neutral30)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral30,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Permissions nudge
// ------------------------------------------------------------------------------------------------

@Composable
private fun PermissionsCard(
    missing: List<AppPermission>,
    onOpen: (AppPermission) -> Unit,
    onDismiss: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            // Neutral surface + coral accent border: reads as important without the muddy
            // pink fill that made the text harder to scan.
            .background(AppTheme.glassColors.surface)
            .border(1.5.dp, Coral40.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Coral40.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.settings_24px),
                    contentDescription = null,
                    tint = Coral40,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "A few permissions are off",
                    fontWeight = FontWeight.Bold,
                    color = Neutral10,
                    fontSize = 15.sp
                )
                Text(
                    text = "Tap one to open its settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral30
                )
            }
            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.close_24px),
                        contentDescription = "Dismiss",
                        tint = Neutral30,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        missing.forEach { permission ->
            PermissionRow(
                permission = permission,
                onClick = { onOpen(permission) }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    permission: AppPermission,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral17.copy(alpha = 0.03f))
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(permission.iconRes),
            contentDescription = null,
            tint = Coral40,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Weighted so the badge measures first and gets its full intrinsic width
                // (otherwise it was squeezed and clipped to "Optio").
                Text(
                    text = permission.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Neutral10,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                if (permission.optional) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Gold40.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Optional",
                            color = Gold40,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            // Keep the chip on one line — it was wrapping to "Optio/nal".
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = permission.rationale,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral30,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.chevron_right_24px),
            contentDescription = null,
            tint = Neutral30.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 1100)
@Composable
private fun HomeScreenPreview() {
    com.yash.speachr.ui.theme.SpeachrTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Neutral99)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HomeHeader()
            PermissionsCard(
                missing = listOf(AppPermission.BATTERY),
                onOpen = {},
                onDismiss = {}
            )
            PrivacyCard()
            SectionLabel("Today")
            TodayStatsRow(HomeStats(totalWordsToday = 248, timeSavedMinutesToday = 2, sessionsToday = 4))
            RecentHeader(hasItems = false, onSeeAll = {})
            EmptyActivityState()
        }
    }
}
