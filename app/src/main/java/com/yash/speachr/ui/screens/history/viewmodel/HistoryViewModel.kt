package com.yash.speachr.ui.screens.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yash.speachr.core.database.DictationDao
import com.yash.speachr.core.database.DictationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val dictationDao: DictationDao) : ViewModel() {

    val allHistory: StateFlow<List<DictationEntity>> = dictationDao.getAllDictations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteDictation(id: Long) {
        viewModelScope.launch {
            dictationDao.deleteById(id)
        }
    }
}
