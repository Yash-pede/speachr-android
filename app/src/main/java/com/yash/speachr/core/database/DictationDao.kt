package com.yash.speachr.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DictationDao {
    @Insert
    suspend fun insert(dictation: DictationEntity)

    @Query("SELECT * FROM dictations ORDER BY timestamp DESC")
    fun getAllDictations(): Flow<List<DictationEntity>>

    @Query("SELECT * FROM dictations WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getDictationsSince(since: Long): Flow<List<DictationEntity>>

    @Query("DELETE FROM dictations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM dictations")
    suspend fun deleteAll()
}
