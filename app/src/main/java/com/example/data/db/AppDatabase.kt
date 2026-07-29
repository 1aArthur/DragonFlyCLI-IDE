package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.db.dao.*
import com.example.data.db.entities.*

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        CommandHistoryEntity::class,
        BookmarkEntity::class,
        WorkflowEntity::class,
        ApiConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun commandDao(): CommandDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun apiConfigDao(): ApiConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dragonfly_cli_ide.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
