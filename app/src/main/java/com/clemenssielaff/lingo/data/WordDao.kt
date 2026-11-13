package com.clemenssielaff.lingo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
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

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Insert
    suspend fun insert(word: Word): Long

    @Insert
    suspend fun insertAll(words: List<Word>)

    @Update
    suspend fun update(word: Word)

    @Delete
    suspend fun delete(word: Word)
}
