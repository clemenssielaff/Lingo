package com.clemenssielaff.lingo.ui

import androidx.lifecycle.CreationExtras
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clemenssielaff.lingo.LingoApplication
import com.clemenssielaff.lingo.data.Word
import com.clemenssielaff.lingo.data.WordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LingoViewModel(private val repository: WordRepository) : ViewModel() {

    val words: StateFlow<List<Word>> = repository.allWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val practiceWords: StateFlow<List<Word>> = repository.wordsForPractice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val wordCount: StateFlow<Int> = repository.wordCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun addWord(term: String, translation: String, example: String, language: String) =
        viewModelScope.launch { repository.addWord(term, translation, example, language) }

    fun markCorrect(word: Word) = viewModelScope.launch { repository.markCorrect(word) }

    fun markWrong(word: Word) = viewModelScope.launch { repository.markWrong(word) }

    fun deleteWord(word: Word) = viewModelScope.launch { repository.delete(word) }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application =
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LingoApplication
                return LingoViewModel(application.repository) as T
            }
        }
    }
}
