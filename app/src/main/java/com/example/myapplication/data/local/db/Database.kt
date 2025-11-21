package com.example.myapplication.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoleplayScenarioEntity::class,
        PronunciationExampleEntity::class,
        ExerciseAttemptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BizEngDatabase : RoomDatabase() {
    abstract fun roleplayScenarioDao(): RoleplayScenarioDao
    abstract fun pronunciationDao(): PronunciationDao
    abstract fun attemptDao(): ExerciseAttemptDao

    companion object {
        fun create(context: Context): BizEngDatabase = Room.databaseBuilder(
            context.applicationContext,
            BizEngDatabase::class.java,
            "bizeng.db"
        ).fallbackToDestructiveMigration().build()
    }
}

