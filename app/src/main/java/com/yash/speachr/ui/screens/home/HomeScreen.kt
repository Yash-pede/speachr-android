package com.yash.speachr.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
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
                // Profile Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Coral40.copy(alpha = 0.2f))
                        .border(1.dp, Coral40, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Y", color = Coral40, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // --- Quick Stats Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "1,240",
                    label = "Words Today",
                    color = Coral40
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "15m",
                    label = "Time Saved",
                    color = Gold40
                )
            }
        }

        item {
            // --- Recent Activity Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontWeight = FontWeight.Bold,
                    color = Neutral10
                )
                Text(
                    text = "See all",
                    color = Coral40,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
        }

        // --- Recent Transcriptions List ---
        items(dummyTranscriptions) { item ->
            TranscriptionCard(item)
        }
    }
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
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral30
            )
        }
    }
}

data class TranscriptionItem(
    val appName: String,
    val appColor: Color,
    val preview: String,
    val timeAgo: String
)

private val dummyTranscriptions = listOf(
    TranscriptionItem("WhatsApp", Color(0xFF25D366), "Hey, I'm running about 10 minutes late to the meeting. Traffic is crazy right now.", "2m ago"),
    TranscriptionItem("Gmail", Color(0xFFEA4335), "Hi Team, just a quick update on the Q3 roadmap. We are on track to deliver the beta by next Friday.", "1h ago"),
    TranscriptionItem("Slack", Color(0xFF4A154B), "Can someone take a look at the PR when they have a moment? It's blocking the merge.", "Yesterday")
)

@Composable
private fun TranscriptionCard(item: TranscriptionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.glassColors.surface)
            .border(1.dp, AppTheme.glassColors.border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon Placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.appColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(item.appColor))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.appName, fontWeight = FontWeight.Bold, color = Neutral10)
                Text(item.timeAgo, style = MaterialTheme.typography.bodySmall, color = Neutral30)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral30,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
