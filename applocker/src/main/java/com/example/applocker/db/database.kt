package com.example.applocker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.applocker.db.dao.AppLockDao
import com.example.applocker.db.entities.AppLockEntity

const val DATABASE_NAME = "core_app_locker_db"

@Database(
    entities = [AppLockEntity::class],
    exportSchema = false,
    version = 1
)
abstract class DatabaseHelper : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: DatabaseHelper? = null
        fun create(context: Context) {
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        fun get(): DatabaseHelper {
            return instance!!
        }

        private fun buildDatabase(context: Context): DatabaseHelper {
            return Room.databaseBuilder(context, DatabaseHelper::class.java, DATABASE_NAME).build()
        }
    }

    abstract fun appLockDao(): AppLockDao
}