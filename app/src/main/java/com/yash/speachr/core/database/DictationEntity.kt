package com.yash.speachr.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dictations")
data class DictationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long,
    val wordCount: Int,
    val durationSeconds: Long
)
