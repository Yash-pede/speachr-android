package com.yash.speachr.ui.screens.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yash.speachr.core.database.DictationDao
import com.yash.speachr.core.database.DictationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class HomeViewModel(private val dictationDao: DictationDao) : ViewModel() {

    private val todayStart: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    val todayStats: StateFlow<HomeStats> = dictationDao.getDictationsSince(todayStart)
        .map { dictations ->
            val totalWords = dictations.sumOf { it.wordCount }
            // Assuming time saved is proportional to word count or duration. 
            // Let's say 120 words per minute saved.
            val timeSavedMinutes = totalWords / 120 
            HomeStats(
                totalWordsToday = totalWords,
                timeSavedMinutesToday = timeSavedMinutes,
                recentDictations = dictations.take(5)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeStats()
        )
}

data class HomeStats(
    val totalWordsToday: Int = 0,
    val timeSavedMinutesToday: Int = 0,
    val recentDictations: List<DictationEntity> = emptyList()
)
