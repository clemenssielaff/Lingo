package com.clemenssielaff.lingo

import android.app.Application
import com.clemenssielaff.lingo.data.AppDatabase
import com.clemenssielaff.lingo.data.WordRepository

class LingoApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { WordRepository(database.wordDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LingoApplication
            private set
    }
}
