package com.clemenssielaff.lingo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words ORDER BY id DESC")
    fun getAllWords(): Flow<List<Word>>

    @Query(
        "SELECT * FROM words ORDER BY " +
            "CASE WHEN timesWrong + timesCorrect = 0 THEN 1 " +
            "ELSE CAST(timesWrong AS REAL) / (timesWrong + timesCorrect) END DESC, id ASC"
    )
    fun getWordsForPractice(): Flow<List<Word>>

    @Query("SELECT COUNT(*) FROM words")
    fun countWords(): Flow<Int>

    @Insert
    suspend fun insert(word: Word): Long

    @Insert
    suspend fun insertAll(words: List<Word>)

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)

    @Query("DELETE FROM words WHERE isSample = 1")
    suspend fun deleteSampleWords()

    @Query("SELECT value FROM meta WHERE id = :key")
    suspend fun getMeta(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMeta(meta: Meta)
}
