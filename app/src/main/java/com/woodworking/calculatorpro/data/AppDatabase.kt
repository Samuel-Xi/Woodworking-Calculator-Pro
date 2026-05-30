package com.woodworking.calculatorpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        private const val DB_NAME = "woodworking.db"

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                // Safe default: the DB only stores user-created history, so losing
                // it on a schema change is preferable to crashing.
                .fallbackToDestructiveMigration()
                .build()
    }
}
