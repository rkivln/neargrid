package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "neighbor_posts")
data class NeighborPost(
    @PrimaryKey val id: String,
    val type: String, // NEED, OFFER, MEETUP
    val title: String,
    val description: String,
    val category: String, // TOOLS, HOUSEHOLD, HELP, STUDY, SPORTS, COMMUNITY
    val authorName: String,
    val authorInitials: String,
    val authorCellId: String,
    val cellName: String,
    val lat: Double,
    val lng: Double,
    val approxDistanceBand: String, // e.g. "within 250 m"
    val urgency: String, // NORMAL, URGENT, TIME_SENSITIVE
    val expiresInHours: Int,
    val status: String = "ACTIVE", // ACTIVE, MATCHED, COMPLETED
    val matchedItemId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_issues")
data class CommunityIssue(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val locationSnippet: String,
    val cellId: String,
    val category: String, // LIGHTING, ROADS, SANITATION, PARKS, SAFETY
    val status: String, // REPORTED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED
    val reporterName: String,
    val upvotesCount: Int = 1,
    val hasUserUpvoted: Boolean = false,
    val assignedVolunteer: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_notices")
data class CommunityNotice(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val cellId: String,
    val authorityName: String,
    val isPinned: Boolean = false,
    val isUrgent: Boolean = false,
    val dateLabel: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val participantName: String,
    val relatedPostTitle: String,
    val relatedPostType: String,
    val lastMessage: String,
    val lastMessageTime: Long = System.currentTimeMillis(),
    val distanceBand: String,
    val isResolved: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderName: String,
    val isFromCurrentUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPickupLocation: Boolean = false,
    val meetingPoint: String? = null
)

@Entity(tableName = "grid_cells")
data class GridCell(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val activeNeighborsCount: Int,
    val openNeedsCount: Int,
    val openOffersCount: Int,
    val activeIssuesCount: Int,
    val centerLat: Double,
    val centerLng: Double,
    val description: String
)

data class IntentMatchResult(
    val need: NeighborPost,
    val matchingOffer: NeighborPost,
    val compatibilityPercent: Int,
    val matchReason: String
)
