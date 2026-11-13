package com.clemenssielaff.lingo.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WordRepository(private val wordDao: WordDao) {

    val allWords: Flow<List<Word>> = wordDao.getAllWords()
    val wordsForPractice: Flow<List<Word>> = wordDao.getWordsForPractice()
    val wordCount: Flow<Int> = wordDao.countWords()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (wordDao.count() == 0) {
                wordDao.insertAll(sampleWords)
            }
        }
    }

    suspend fun addWord(term: String, translation: String, example: String, language: String) {
        wordDao.insert(
            Word(
                term = term.trim(),
                translation = translation.trim(),
                example = example.trim(),
                language = language.ifBlank { "en" },
            )
        )
    }

    suspend fun markCorrect(word: Word) {
        wordDao.update(word.copy(timesCorrect = word.timesCorrect + 1))
    }

    suspend fun markWrong(word: Word) {
        wordDao.update(word.copy(timesWrong = word.timesWrong + 1))
    }

    suspend fun delete(word: Word) {
        wordDao.delete(word)
    }

    companion object {
        val sampleWords = listOf(
            Word(term = "apple", translation = "der Apfel", example = "An apple a day keeps the doctor away.", language = "en"),
            Word(term = "house", translation = "das Haus", example = "Their house is near the park.", language = "en"),
            Word(term = "to learn", translation = "lernen", example = "I learn English every day.", language = "en"),
            Word(term = "dog", translation = "der Hund", example = "The dog barks at night.", language = "en"),
            Word(term = "book", translation = "das Buch", example = "This book is very interesting.", language = "en"),
            Word(term = "water", translation = "das Wasser", example = "Please drink more water.", language = "en"),
            Word(term = "friend", translation = "der Freund / die Freundin", example = "She is my best friend.", language = "en"),
            Word(term = "beautiful", translation = "schön", example = "What a beautiful day!", language = "en"),
            Word(term = "to travel", translation = "reisen", example = "We travel to Spain in summer.", language = "en"),
            Word(term = "breakfast", translation = "das Frühstück", example = "I eat breakfast at eight.", language = "en"),
        )
    }
}
