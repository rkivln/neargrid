package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.data.model.Conversation
import com.example.data.model.GridCell
import com.example.data.model.NeighborPost
import com.example.data.model.PostComment

@Database(
    entities = [
        NeighborPost::class,
        CommunityIssue::class,
        CommunityNotice::class,
        Conversation::class,
        ChatMessage::class,
        GridCell::class,
        PostComment::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun neighborPostDao(): NeighborPostDao
    abstract fun communityIssueDao(): CommunityIssueDao
    abstract fun communityNoticeDao(): CommunityNoticeDao
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun gridCellDao(): GridCellDao
    abstract fun postCommentDao(): PostCommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun migrateDatabase(execSql: (String) -> Unit) {
            try {
                execSql(
                    "CREATE TABLE IF NOT EXISTS `post_comments` (" +
                        "`id` TEXT NOT NULL, " +
                        "`postId` TEXT NOT NULL, " +
                        "`authorId` TEXT NOT NULL, " +
                        "`authorName` TEXT NOT NULL, " +
                        "`authorInitials` TEXT NOT NULL, " +
                        "`text` TEXT NOT NULL, " +
                        "`parentId` TEXT, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            } catch (ignored: Exception) {}

            try {
                execSql("ALTER TABLE `neighbor_posts` ADD COLUMN `mediaUris` TEXT NOT NULL DEFAULT '[]'")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `neighbor_posts` ADD COLUMN `mediaTypes` TEXT NOT NULL DEFAULT '[]'")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `neighbor_posts` ADD COLUMN `upvotesCount` INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `neighbor_posts` ADD COLUMN `hasUserUpvoted` INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `neighbor_posts` ADD COLUMN `upvoterIds` TEXT NOT NULL DEFAULT '[]'")
            } catch (ignored: Exception) {}

            try {
                execSql("ALTER TABLE `community_issues` ADD COLUMN `mediaUris` TEXT NOT NULL DEFAULT '[]'")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `community_issues` ADD COLUMN `mediaTypes` TEXT NOT NULL DEFAULT '[]'")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `community_issues` ADD COLUMN `hasUserUpvoted` INTEGER NOT NULL DEFAULT 0")
            } catch (ignored: Exception) {}
            try {
                execSql("ALTER TABLE `community_issues` ADD COLUMN `upvoterIds` TEXT NOT NULL DEFAULT '[]'")
            } catch (ignored: Exception) {}
        }

        private fun createMigration(from: Int, to: Int): Migration {
            return object : Migration(from, to) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    migrateDatabase { sql -> db.execSQL(sql) }
                }

                override fun migrate(connection: SQLiteConnection) {
                    if (connection is androidx.room.driver.SupportSQLiteConnection) {
                        migrateDatabase { sql -> connection.db.execSQL(sql) }
                    } else {
                        migrateDatabase { sql -> connection.execSQL(sql) }
                    }
                }
            }
        }

        private val MIGRATION_1_2 = createMigration(1, 2)
        private val MIGRATION_2_3 = createMigration(2, 3)
        private val MIGRATION_3_4 = createMigration(3, 4)
        private val MIGRATION_4_5 = createMigration(4, 5)
        private val MIGRATION_5_6 = createMigration(5, 6)
        private val MIGRATION_1_6 = createMigration(1, 6)
        private val MIGRATION_2_6 = createMigration(2, 6)
        private val MIGRATION_3_6 = createMigration(3, 6)
        private val MIGRATION_4_6 = createMigration(4, 6)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    buildAndWarmupDatabase(context)
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Failed to open or migrate neargrid_database, deleting and recreating", e)
                    try {
                        context.applicationContext.deleteDatabase("neargrid_database")
                    } catch (ignored: Exception) {}
                    buildAndWarmupDatabase(context)
                }.also { INSTANCE = it }
            }
        }

        private fun buildAndWarmupDatabase(context: Context): AppDatabase {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "neargrid_database"
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_1_6,
                    MIGRATION_2_6,
                    MIGRATION_3_6,
                    MIGRATION_4_6
                )
                .fallbackToDestructiveMigration(true)
                .build()

            // Proactively warmup database connection inside try/catch so any migration
            // or schema mismatch exception is immediately caught and auto-recovered.
            try {
                instance.openHelper.writableDatabase
            } catch (e: Exception) {
                try {
                    instance.close()
                } catch (ignored: Exception) {}
                throw e
            }

            return instance
        }
    }
}
