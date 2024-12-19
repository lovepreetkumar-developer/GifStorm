package com.techfathers.gifstorm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.techfathers.gifstorm.data.db.converters.AllTypeConverters
import com.techfathers.gifstorm.data.db.entities.ResultModel

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

@Database(
    entities = [ResultModel::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AllTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "GifStorm.db"
            ).build()
    }
}