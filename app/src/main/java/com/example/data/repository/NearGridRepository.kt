package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.CommunityNotice
import com.example.data.model.Conversation
import com.example.data.model.GridCell
import com.example.data.model.IntentMatchResult
import com.example.data.model.NeighborPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class NearGridRepository(private val database: AppDatabase) {

    private val postDao = database.neighborPostDao()
    private val issueDao = database.communityIssueDao()
    private val noticeDao = database.communityNoticeDao()
    private val conversationDao = database.conversationDao()
    private val chatDao = database.chatMessageDao()
    private val cellDao = database.gridCellDao()

    val allPosts: Flow<List<NeighborPost>> = postDao.getAllPosts()
    val allIssues: Flow<List<CommunityIssue>> = issueDao.getAllIssues()
    val allNotices: Flow<List<CommunityNotice>> = noticeDao.getAllNotices()
    val allConversations: Flow<List<Conversation>> = conversationDao.getAllConversations()
    val allCells: Flow<List<GridCell>> = cellDao.getAllCells()

    fun getChatMessages(conversationId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun createPost(
        type: String,
        title: String,
        description: String,
        category: String,
        authorName: String,
        cellName: String,
        cellId: String,
        urgency: String,
        expiresInHours: Int,
        distanceBand: String = "within 300 m"
    ) = withContext(Dispatchers.IO) {
        val initials = authorName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "ME" }

        val post = NeighborPost(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            description = description,
            category = category,
            authorName = authorName,
            authorInitials = initials,
            authorCellId = cellId,
            cellName = cellName,
            lat = 37.7749 + (Math.random() - 0.5) * 0.008,
            lng = -122.4194 + (Math.random() - 0.5) * 0.008,
            approxDistanceBand = distanceBand,
            urgency = urgency,
            expiresInHours = expiresInHours,
            status = "ACTIVE",
            createdAt = System.currentTimeMillis()
        )
        postDao.insertPost(post)
    }

    suspend fun createIssue(
        title: String,
        description: String,
        locationSnippet: String,
        cellId: String,
        category: String,
        reporterName: String = "You (Local Resident)"
    ) = withContext(Dispatchers.IO) {
        val issue = CommunityIssue(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            locationSnippet = locationSnippet,
            cellId = cellId,
            category = category,
            status = "REPORTED",
            reporterName = reporterName,
            upvotesCount = 1,
            hasUserUpvoted = true,
            createdAt = System.currentTimeMillis()
        )
        issueDao.insertIssue(issue)
    }

    suspend fun toggleUpvote(issueId: String, currentUpvotes: Int, currentlyUpvoted: Boolean) =
        withContext(Dispatchers.IO) {
            val newUpvotes = if (currentlyUpvoted) (currentUpvotes - 1).coerceAtLeast(0) else currentUpvotes + 1
            issueDao.updateUpvote(issueId, newUpvotes, !currentlyUpvoted)
        }

    suspend fun updateIssueStatus(issueId: String, newStatus: String, volunteer: String?) =
        withContext(Dispatchers.IO) {
            issueDao.updateIssueStatus(issueId, newStatus, volunteer)
        }

    suspend fun markPostStatus(postId: String, status: String) = withContext(Dispatchers.IO) {
        postDao.updatePostStatus(postId, status)
    }

    suspend fun sendMessage(conversationId: String, text: String, isPickupLocation: Boolean = false, meetingPoint: String? = null) =
        withContext(Dispatchers.IO) {
            val message = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderName = "You",
                isFromCurrentUser = true,
                text = text,
                timestamp = System.currentTimeMillis(),
                isPickupLocation = isPickupLocation,
                meetingPoint = meetingPoint
            )
            chatDao.insertMessage(message)
            conversationDao.updateLastMessage(conversationId, text, System.currentTimeMillis())
        }

    suspend fun startConversationForPost(post: NeighborPost): String = withContext(Dispatchers.IO) {
        val convId = "conv_${post.id}"
        val existing = conversationDao.getAllConversations().first().find { it.id == convId }
        if (existing == null) {
            val conv = Conversation(
                id = convId,
                participantName = post.authorName,
                relatedPostTitle = post.title,
                relatedPostType = post.type,
                lastMessage = "Started conversation regarding \"${post.title}\"",
                lastMessageTime = System.currentTimeMillis(),
                distanceBand = post.approxDistanceBand,
                isResolved = false
            )
            conversationDao.insertConversation(conv)
            val welcomeMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderName = post.authorName,
                isFromCurrentUser = false,
                text = "Hi neighbor! Thanks for connecting about \"${post.title}\". We can coordinate handoff or details here safely.",
                timestamp = System.currentTimeMillis() - 60000,
                isPickupLocation = true,
                meetingPoint = "Oakwood Community Park Public Pavilion"
            )
            chatDao.insertMessage(welcomeMsg)
        }
        convId
    }

    /**
     * Intent-Based Matching Engine
     * Cross-analyzes open Needs with active Offers to find high-compatibility local matches.
     */
    fun findTopIntentMatches(posts: List<NeighborPost>): List<IntentMatchResult> {
        val needs = posts.filter { it.type == "NEED" && it.status == "ACTIVE" }
        val offers = posts.filter { it.type == "OFFER" && it.status == "ACTIVE" }

        val matches = mutableListOf<IntentMatchResult>()

        for (need in needs) {
            for (offer in offers) {
                var score = 0
                val reasons = mutableListOf<String>()

                // Category match
                if (need.category.equals(offer.category, ignoreCase = true)) {
                    score += 50
                    reasons.add("Exact Category (${need.category})")
                }

                // Keyword overlap in title/description
                val needWords = (need.title + " " + need.description).lowercase().split(" ", ",", ".", "/")
                    .filter { it.length > 3 && it !in setOf("need", "looking", "have", "with", "from", "this", "that", "available") }
                val offerWords = (offer.title + " " + offer.description).lowercase().split(" ", ",", ".", "/")
                    .filter { it.length > 3 }

                val common = needWords.intersect(offerWords.toSet())
                if (common.isNotEmpty()) {
                    score += 35 + (common.size * 5)
                    reasons.add("Keywords: " + common.take(3).joinToString(", "))
                }

                // Proximity bonus
                if (need.cellName == offer.cellName) {
                    score += 15
                    reasons.add("Same street cluster (${need.cellName})")
                }

                if (score >= 65) {
                    val finalScore = score.coerceAtMost(98)
                    matches.add(
                        IntentMatchResult(
                            need = need,
                            matchingOffer = offer,
                            compatibilityPercent = finalScore,
                            matchReason = reasons.joinToString(" • ")
                        )
                    )
                }
            }
        }
        return matches.sortedByDescending { it.compatibilityPercent }
    }

    suspend fun resetToFreshState() = withContext(Dispatchers.IO) {
        postDao.deleteAllPosts()
        issueDao.deleteAllIssues()
        noticeDao.deleteAllNotices()
        conversationDao.deleteAllConversations()
        chatDao.deleteAllMessages()
        cellDao.deleteAllCells()
        seedFreshCells()
    }

    private suspend fun seedFreshCells() {
        val cells = listOf(
            GridCell(
                id = "cell_oakwood_center",
                name = "Oakwood Grove",
                code = "#H3-8826",
                activeNeighborsCount = 1,
                openNeedsCount = 0,
                openOffersCount = 0,
                activeIssuesCount = 0,
                centerLat = 37.7749,
                centerLng = -122.4194,
                description = "Tree-lined central avenue, quiet cul-de-sacs, and community corner market."
            ),
            GridCell(
                id = "cell_elmwood_north",
                name = "Elmwood Avenue",
                code = "#H3-8827",
                activeNeighborsCount = 1,
                openNeedsCount = 0,
                openOffersCount = 0,
                activeIssuesCount = 0,
                centerLat = 37.7780,
                centerLng = -122.4160,
                description = "North corridor connecting local bakery, residential blocks, and walking loop."
            ),
            GridCell(
                id = "cell_pinecrest_park",
                name = "Pinecrest Commons",
                code = "#H3-8828",
                activeNeighborsCount = 1,
                openNeedsCount = 0,
                openOffersCount = 0,
                activeIssuesCount = 0,
                centerLat = 37.7720,
                centerLng = -122.4230,
                description = "Recreation center, community gardens, sports pitch, and safe exchange zone."
            ),
            GridCell(
                id = "cell_sunset_ridge",
                name = "Sunset Ridge",
                code = "#H3-8829",
                activeNeighborsCount = 1,
                openNeedsCount = 0,
                openOffersCount = 0,
                activeIssuesCount = 0,
                centerLat = 37.7795,
                centerLng = -122.4255,
                description = "Elevated street cluster with shared composting station and panoramic views."
            )
        )
        cellDao.insertCells(cells)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingPosts = postDao.getAllPosts().first()
        val hasMockPosts = existingPosts.any { it.id.startsWith("need_") || it.id.startsWith("offer_") || it.id.startsWith("meetup_") }
        val existingConversations = conversationDao.getAllConversations().first()
        val hasMockConversations = existingConversations.any { it.id == "conv_ladder" }

        // If leftover mock data from previous version exists, wipe to fresh state
        if (hasMockPosts || hasMockConversations) {
            resetToFreshState()
            return@withContext
        }

        // Seed fresh spatial cells if database is empty
        val existingCells = cellDao.getAllCells().first()
        if (existingCells.isEmpty()) {
            seedFreshCells()
        }
    }
}
