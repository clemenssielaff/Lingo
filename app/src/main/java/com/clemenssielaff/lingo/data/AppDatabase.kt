package com.clemenssielaff.lingo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Word::class, Meta::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE words ADD COLUMN isSample INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE words SET isSample = 1")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS meta (id TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)"
                )
                db.execSQL("INSERT OR REPLACE INTO meta (id, value) VALUES ('seedVersion', '2')")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lingo.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
