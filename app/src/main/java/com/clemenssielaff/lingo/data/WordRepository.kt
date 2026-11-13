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
            val seeded = wordDao.getMeta("seedVersion")?.toIntOrNull() ?: 0
            if (seeded < SEED_VERSION) {
                wordDao.deleteSampleWords()
                wordDao.insertAll(sampleWords)
                wordDao.setMeta(Meta("seedVersion", SEED_VERSION.toString()))
            }
        }
    }

    suspend fun addWord(term: String, translation: String, example: String, language: String) {
        wordDao.insert(
            Word(
                term = term.trim(),
                translation = translation.trim(),
                example = example.trim(),
                language = language.ifBlank { "ko" },
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
        const val SEED_VERSION = 2

        val sampleWords = listOf(
            Word(term = "안녕하세요 (annyeonghaseyo)", translation = "Hallo! / Guten Tag", example = "안녕하세요! 반갑습니다.", language = "ko", isSample = true),
            Word(term = "감사합니다 (gamsahamnida)", translation = "Danke", example = "정말 감사합니다.", language = "ko", isSample = true),
            Word(term = "사랑 (sarang)", translation = "die Liebe", example = "사랑해요.", language = "ko", isSample = true),
            Word(term = "집 (jip)", translation = "das Haus", example = "우리 집은 작아요.", language = "ko", isSample = true),
            Word(term = "밥 (bap)", translation = "das Essen / die Mahlzeit", example = "밥 먹었어요?", language = "ko", isSample = true),
            Word(term = "물 (mul)", translation = "das Wasser", example = "물 많이 마셔요.", language = "ko", isSample = true),
            Word(term = "친구 (chingu)", translation = "der Freund / die Freundin", example = "그는 내 친구야.", language = "ko", isSample = true),
            Word(term = "아름다운 (areumdaun)", translation = "schön", example = "오늘 날씨가 아름다워요.", language = "ko", isSample = true),
            Word(term = "학교 (hakgyo)", translation = "die Schule", example = "학교에 가요.", language = "ko", isSample = true),
            Word(term = "고양이 (goyangi)", translation = "die Katze", example = "고양이가 자요.", language = "ko", isSample = true),
        )
    }
}
