package com.clemenssielaff.lingo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val translation: String,
    val example: String = "",
    val language: String = "en",
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
)
