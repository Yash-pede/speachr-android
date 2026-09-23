package com.yash.speachr.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral99

/**
 * A glassmorphic section container used across Settings and Onboarding.
 *
 * Reproduces the previous `SettingsGroupCard` styling.
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Coral40,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

/**
 * A section title with a trailing divider line. Previously the Onboarding `SectionHeader`.
 */
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

/**
 * A selectable pill used for languages and tones.
 *
 * Merges the previous Onboarding and Settings variants into a single, consistent style.
 */
@Composable
fun SelectablePill(
    text: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> Coral40
            enabled -> AppTheme.glassColors.surfaceSubtle
            else -> Neutral30.copy(alpha = 0.05f)
        },
        label = "pillBg"
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

/**
 * A row with an icon, title, subtitle and a trailing toggle. Previously `SettingsScreen.SettingToggleRow`.
 */
@Composable
fun SettingToggleRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = title,
            tint = Neutral10,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Neutral10)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Neutral30)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggleChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Neutral99, checkedTrackColor = Coral40)
        )
    }
}

/**
 * A small rounded action chip with an icon and label. Previously `HistoryScreen.ActionButton`.
 */
@Composable
fun ActionChip(
    @DrawableRes icon: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
