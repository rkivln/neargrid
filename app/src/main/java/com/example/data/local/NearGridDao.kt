package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.data.model.Conversation
import com.example.data.model.GridCell
import com.example.data.model.NeighborPost
import kotlinx.coroutines.flow.Flow

@Dao
interface NeighborPostDao {
    @Query("SELECT * FROM neighbor_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<NeighborPost>>

    @Query("SELECT * FROM neighbor_posts WHERE type = :type ORDER BY createdAt DESC")
    fun getPostsByType(type: String): Flow<List<NeighborPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<NeighborPost>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: NeighborPost)

    @Update
    suspend fun updatePost(post: NeighborPost)

    @Query("UPDATE neighbor_posts SET status = :status WHERE id = :id")
    suspend fun updatePostStatus(id: String, status: String)

    @Query("DELETE FROM neighbor_posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("DELETE FROM neighbor_posts")
    suspend fun deleteAllPosts()
}

@Dao
interface CommunityIssueDao {
    @Query("SELECT * FROM community_issues ORDER BY createdAt DESC")
    fun getAllIssues(): Flow<List<CommunityIssue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<CommunityIssue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: CommunityIssue)

    @Query("UPDATE community_issues SET status = :status, assignedVolunteer = :volunteer WHERE id = :id")
    suspend fun updateIssueStatus(id: String, status: String, volunteer: String?)

    @Query("UPDATE community_issues SET upvotesCount = :upvotes, hasUserUpvoted = :hasUpvoted WHERE id = :id")
    suspend fun updateUpvote(id: String, upvotes: Int, hasUpvoted: Boolean)

    @Query("DELETE FROM community_issues")
    suspend fun deleteAllIssues()
}

@Dao
interface CommunityNoticeDao {
    @Query("SELECT * FROM community_notices ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotices(): Flow<List<CommunityNotice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<CommunityNotice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: CommunityNotice)

    @Query("DELETE FROM community_notices")
    suspend fun deleteAllNotices()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<Conversation>)

    @Query("UPDATE conversations SET lastMessage = :message, lastMessageTime = :time WHERE id = :id")
    suspend fun updateLastMessage(id: String, message: String, time: Long)

    @Query("UPDATE conversations SET isResolved = :isResolved WHERE id = :id")
    suspend fun setResolved(id: String, isResolved: Boolean)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface GridCellDao {
    @Query("SELECT * FROM grid_cells")
    fun getAllCells(): Flow<List<GridCell>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCells(cells: List<GridCell>)

    @Query("SELECT * FROM grid_cells WHERE id = :id LIMIT 1")
    suspend fun getCellById(id: String): GridCell?

    @Query("DELETE FROM grid_cells")
    suspend fun deleteAllCells()
}
