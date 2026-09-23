package com.yash.speachr.ui.screens.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yash.speachr.core.database.DictationDao
import com.yash.speachr.core.database.DictationEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class HomeViewModel(private val dictationDao: DictationDao) : ViewModel() {

    /** Re-emitted by [refresh] so the "today" window is recomputed when the screen is opened. */
    private val refreshTrigger = MutableStateFlow(Unit)

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayStats: StateFlow<HomeStats> = refreshTrigger
        .flatMapLatest { dictationDao.getDictationsSince(startOfToday()) }
        .map { dictations ->
            val totalWords = dictations.sumOf { it.wordCount }
            HomeStats(
                totalWordsToday = totalWords,
                timeSavedMinutesToday = totalWords / WORDS_PER_MINUTE_TYPED,
                sessionsToday = dictations.size,
                recentDictations = dictations.take(RECENT_LIMIT)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeStats()
        )

    /**
     * Recomputes "today". Without this the start-of-day boundary is captured once at
     * construction, so an app left open overnight would keep showing yesterday's numbers.
     */
    fun refresh() {
        refreshTrigger.value = Unit
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        /** Rough typing speed a user is spared by dictating instead. */
        const val WORDS_PER_MINUTE_TYPED = 120
        const val RECENT_LIMIT = 5
    }
}

data class HomeStats(
    val totalWordsToday: Int = 0,
    val timeSavedMinutesToday: Int = 0,
    val sessionsToday: Int = 0,
    val recentDictations: List<DictationEntity> = emptyList()
)
