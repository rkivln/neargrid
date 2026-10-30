package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PostComment
import kotlinx.coroutines.flow.Flow

@Dao
interface PostCommentDao {
    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<PostComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostComment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<PostComment>)

    @Query("DELETE FROM post_comments WHERE postId = :postId")
    suspend fun clearCommentsForPost(postId: String)
}
