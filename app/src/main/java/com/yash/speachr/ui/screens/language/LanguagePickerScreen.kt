package com.yash.speachr.ui.screens.language

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yash.speachr.R
import com.yash.speachr.core.model.SpokenLanguage
import com.yash.speachr.core.model.WHISPER_LANGUAGES
import com.yash.speachr.ui.theme.Coral40
import com.yash.speachr.ui.theme.Neutral10
import com.yash.speachr.ui.theme.Neutral30
import com.yash.speachr.ui.theme.Neutral60
import com.yash.speachr.ui.theme.Neutral99
import com.yash.speachr.ui.theme.SpeachrTheme

/**
 * Full-screen language picker, shared by onboarding and Settings.
 *
 * Renders every Whisper-supported language with a search field, scroll-fade edges and a
 * confirm button. Selection is staged locally and only surfaced through [onConfirm], so
 * backing out with the system back gesture leaves the previous choice untouched.
 */
@Composable
fun LanguagePickerScreen(
    selectedLanguage: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var pendingSelection by rememberSaveable(selectedLanguage) {
        mutableStateOf(selectedLanguage)
    }

    // Hardware / gesture back should behave like the toolbar arrow.
    BackHandler(enabled = true) { onDismiss() }

    val filteredLanguages = remember(query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            WHISPER_LANGUAGES
        } else {
            WHISPER_LANGUAGES.filter { language ->
                language.name.contains(trimmed, ignoreCase = true) ||
                    language.nativeName.contains(trimmed, ignoreCase = true)
            }
        }
    }

    val listState = rememberLazyListState()

    // When the query changes, jump back to the top so results are immediately visible.
    LaunchedEffect(query) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral99)
            // safeDrawing (not systemBars) so the search field's keyboard pushes the list
            // instead of covering the Done button.
            .safeDrawingPadding()
    ) {
        LanguagePickerTopBar(onBack = onDismiss)

        LanguageSearchField(
            query = query,
            onQueryChange = { query = it },
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (filteredLanguages.isEmpty()) {
                EmptyLanguagesState(
                    query = query,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredLanguages,
                        key = { it.name }
                    ) { language ->
                        LanguageCard(
                            language = language,
                            isSelected = language.name == pendingSelection,
                            onClick = { pendingSelection = language.name }
                        )
                    }
                }
            }

            // Soft fades so rows dissolve into the background instead of being hard-clipped.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Neutral99, Neutral99.copy(alpha = 0f))
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Neutral99.copy(alpha = 0f), Neutral99)
                        )
                    )
            )
        }

        DoneButton(
            enabled = pendingSelection.isNotBlank(),
            onClick = { onConfirm(pendingSelection) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

// ------------------------------------------------------------------------------------------------
// Pieces
// ------------------------------------------------------------------------------------------------

@Composable
private fun LanguagePickerTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.chevron_right_24px),
                contentDescription = "Back",
                tint = Neutral10,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(180f)
            )
        }

        Text(
            text = "Select Language",
            color = Neutral10,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp)
        )
    }
}

@Composable
private fun LanguageSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Neutral99)
            .border(1.dp, Neutral30.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.search_24px),
            contentDescription = null,
            tint = Neutral60,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = "Search languages",
                    color = Neutral60,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = Neutral10,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(Coral40),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LanguageCard(
    language: SpokenLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        listOf(Coral40.copy(alpha = 0.12f), Coral40.copy(alpha = 0.03f))
                    )
                } else {
                    Brush.linearGradient(listOf(Color.White, Color(0xFFF7F2EF)))
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Coral40 else Neutral30.copy(alpha = 0.65f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Neutral10
                    )
                ) {
                    append(language.name)
                }
                if (language.nativeName.isNotBlank() && language.nativeName != language.name) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = Neutral30.copy(alpha = 0.55f)
                        )
                    ) {
                        append(" (${language.nativeName})")
                    }
                }
            },
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Spacer(modifier = Modifier.width(12.dp))
            CheckMark()
        }
    }
}

@Composable
private fun CheckMark() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.55f)
            lineTo(size.width * 0.4f, size.height * 0.78f)
            lineTo(size.width * 0.85f, size.height * 0.22f)
        }
        drawPath(
            path = path,
            color = Coral40,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
private fun EmptyLanguagesState(
    query: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No languages found",
            color = Neutral10,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Nothing matches \"$query\". Try a different spelling.",
            color = Neutral60,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DoneButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        label = "doneAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Coral40.copy(alpha = background))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Done",
            color = Neutral99,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun LanguagePickerScreenPreview() {
    SpeachrTheme {
        LanguagePickerScreen(
            selectedLanguage = "Breton",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
