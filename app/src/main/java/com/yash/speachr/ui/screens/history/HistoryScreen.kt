package com.yash.speachr.ui.screens.history

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.R
import com.yash.speachr.ui.theme.AppTheme
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Gold40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral60
import com.yash.speachr.ui.theme.Neutral99
import kotlin.random.Random

@Composable
fun HistoryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var expandedItemId by remember { mutableStateOf<String?>(null) }

    // --- Massive Dummy Data List (40 items) ---
    val allHistory = remember {
        val apps = listOf(
            Triple(
                "WhatsApp",
                Color(0xFF25D366),
                "Hey, I'm running about 10 minutes late to the meeting. Traffic is crazy right now."
            ),
            Triple(
                "Gmail",
                Color(0xFFEA4335),
                "Hi Team, just a quick update on the Q3 roadmap. We are on track to deliver the beta by next Friday."
            ),
            Triple(
                "Slack",
                Color(0xFF4A154B),
                "Can someone take a look at the PR when they have a moment? It's blocking the merge."
            ),
            Triple(
                "Notion",
                Color(0xFF000000),
                "Meeting notes: Discussed the new onboarding flow and the floating bubble animation specs."
            ),
            Triple(
                "Twitter",
                Color(0xFF1DA1F2),
                "Just shipped a new update to the app! So excited to hear what you guys think."
            )
        )
        (1..40).map { i ->
            val app = apps[i % apps.size]
            HistoryItem(
                id = i.toString(),
                appName = app.first,
                appColor = app.second,
                timestamp = "Day ${i}, ${Random.nextInt(1, 12)}:${
                    Random.nextInt(
                        10,
                        59
                    )
                } ${if (Random.nextBoolean()) "AM" else "PM"}",
                text = app.third + " (Instance $i) - Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt."
            )
        }
    }

    val filters = listOf("All", "WhatsApp", "Gmail", "Slack", "Notion", "Twitter")

    val filteredHistory = allHistory.filter { item ->
        (selectedFilter == "All" || item.appName == selectedFilter) &&
                (searchQuery.isEmpty() || item.text.contains(
                    searchQuery,
                    ignoreCase = true
                ) || item.appName.contains(searchQuery, ignoreCase = true))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Neutral99)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Neutral10
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${allHistory.size} items",
                    color = Neutral30,
                    fontWeight = FontWeight.Medium
                )
            }

            // --- Smart Folders (Coming Soon) ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listOf("Work", "Personal", "Starred")) { folder ->
                    ComingSoonFolderChip(folder)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Custom Glassmorphic Search Bar ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.glassColors.surface)
                    .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.search_24px),
                        contentDescription = "Search",
                        tint = Neutral30,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search transcriptions...",
                                color = Neutral30.copy(alpha = 0.6f)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(color = Neutral10, fontSize = 16.sp),
                            cursorBrush = SolidColor(Coral40),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✕",
                            color = Neutral30,
                            modifier = Modifier.clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- App Filter Pills ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterPill(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Main List ---
            if (filteredHistory.isEmpty()) {
                EmptySearchState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            isExpanded = expandedItemId == item.id,
                            onClick = {
                                expandedItemId = if (expandedItemId == item.id) null else item.id
                            }
                        )
                    }
                    // Bottom spacer so last card isn't hidden by nav bar
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Sub-Components
// ------------------------------------------------------------------------------------------------

data class HistoryItem(
    val id: String,
    val appName: String,
    val appColor: Color,
    val timestamp: String,
    val text: String
)

@Composable
private fun ComingSoonFolderChip(folderName: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.folder_managed_24px),
                contentDescription = "Folder",
                tint = Gold40,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(folderName, color = Neutral10, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Gold40.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("Soon", color = Gold40, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Coral40 else AppTheme.glassColors.surface
    val textColor = if (isSelected) Neutral99 else Neutral10
    val borderColor = if (isSelected) Color.Transparent else AppTheme.glassColors.border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItem,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.glassColors.surface)
            .border(
                width = 1.dp,
                color = if (isExpanded) Coral40.copy(alpha = 0.5f) else AppTheme.glassColors.border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.appColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(item.appColor)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.appName, fontWeight = FontWeight.Bold, color = Neutral10)
                Text(item.timestamp, style = MaterialTheme.typography.bodySmall, color = Neutral30)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral10,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        // Expandable Actions
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(icon = R.drawable.content_copy_24px, label = "Copy", color = Coral40)
                Spacer(modifier = Modifier.width(12.dp))
                ActionButton(
                    icon = R.drawable.share_windows_24px,
                    label = "Share",
                    color = Neutral30
                )
                Spacer(modifier = Modifier.width(12.dp))
                ActionButton(
                    icon = R.drawable.delete_24px,
                    label = "Delete",
                    color = Color(0xFFBA1A1A)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    @DrawableRes icon: Int,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable { /* TODO: Handle action */ }
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

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.glassColors.surface)
                    .border(1.dp, AppTheme.glassColors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.search_24px),
                    contentDescription = "No results",
                    tint = Neutral30,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No results found",
                fontWeight = FontWeight.Bold,
                color = Neutral10,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Try adjusting your search or filter.",
                color = Neutral30,
                textAlign = TextAlign.Center
            )
        }
    }
}