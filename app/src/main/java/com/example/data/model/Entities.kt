package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val handle: String = "",
    val bio: String = "",
    val cellId: String = "cell_oakwood_center",
    val cellName: String = "Oakwood Grove",
    val avatarPresetIndex: Int = 0,
    val avatarUri: String? = null,
    val privacyRadiusMeters: Int = 500,
    val fuzzyCoords: Boolean = true,
    val incognito: Boolean = false,
    val allowDMs: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "neighbor_posts")
data class NeighborPost(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val type: String = "NEED", // NEED, OFFER, MEETUP
    val title: String = "",
    val description: String = "",
    val category: String = "TOOLS", // TOOLS, HOUSEHOLD, HELP, STUDY, SPORTS, COMMUNITY
    val authorName: String = "",
    val authorInitials: String = "N",
    val authorCellId: String = "cell_oakwood_center",
    val cellName: String = "Oakwood Grove",
    val lat: Double = 37.7749,
    val lng: Double = -122.4194,
    val approxDistanceBand: String = "within 300 m",
    val urgency: String = "NORMAL", // NORMAL, URGENT, TIME_SENSITIVE
    val expiresInHours: Int = 48,
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, CANCELLED
    val matchedItemId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_issues")
data class CommunityIssue(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val locationSnippet: String = "",
    val cellId: String = "cell_oakwood_center",
    val category: String = "LIGHTING", // LIGHTING, ROADS, SANITATION, PARKS, SAFETY, OTHER
    val status: String = "REPORTED", // REPORTED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED
    val reporterName: String = "",
    val reporterId: String = "",
    val upvotesCount: Int = 1,
    val hasUserUpvoted: Boolean = false,
    val upvoterIds: List<String> = emptyList(),
    val assignedVolunteer: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_notices")
data class CommunityNotice(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val content: String = "",
    val cellId: String = "cell_oakwood_center",
    val authorityName: String = "Community Council",
    val isPinned: Boolean = false,
    val isUrgent: Boolean = false,
    val dateLabel: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: List<String> = emptyList(),
    val participantName: String = "",
    val relatedPostId: String = "",
    val relatedPostTitle: String = "",
    val relatedPostType: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val distanceBand: String = "within 300 m",
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val isFromCurrentUser: Boolean = false,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPickupLocation: Boolean = false,
    val meetingPoint: String? = null
)

@Entity(tableName = "grid_cells")
data class GridCell(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val code: String = "",
    val activeNeighborsCount: Int = 0,
    val openNeedsCount: Int = 0,
    val openOffersCount: Int = 0,
    val activeIssuesCount: Int = 0,
    val centerLat: Double = 0.0,
    val centerLng: Double = 0.0,
    val description: String = ""
)

data class IntentMatchResult(
    val need: NeighborPost,
    val matchingOffer: NeighborPost,
    val compatibilityPercent: Int,
    val matchReason: String
)

@Entity(tableName = "geohash_messages")
data class GeohashMessage(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorInitials: String = "N",
    val text: String = "",
    val geohash: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val localityName: String = "",
    val isFromCurrentUser: Boolean = false
)
