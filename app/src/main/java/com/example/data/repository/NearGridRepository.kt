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
        category: String
    ) = withContext(Dispatchers.IO) {
        val issue = CommunityIssue(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            locationSnippet = locationSnippet,
            cellId = cellId,
            category = category,
            status = "REPORTED",
            reporterName = "You (Local Resident)",
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

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingPosts = postDao.getAllPosts().first()
        if (existingPosts.isNotEmpty()) return@withContext

        // 1. Seed Grid Cells
        val cells = listOf(
            GridCell(
                id = "cell_oakwood_center",
                name = "Oakwood Grove",
                code = "#H3-8826",
                activeNeighborsCount = 42,
                openNeedsCount = 6,
                openOffersCount = 9,
                activeIssuesCount = 2,
                centerLat = 37.7749,
                centerLng = -122.4194,
                description = "Tree-lined central avenue, quiet cul-de-sacs, and community corner market."
            ),
            GridCell(
                id = "cell_elmwood_north",
                name = "Elmwood Avenue",
                code = "#H3-8827",
                activeNeighborsCount = 31,
                openNeedsCount = 4,
                openOffersCount = 7,
                activeIssuesCount = 3,
                centerLat = 37.7780,
                centerLng = -122.4160,
                description = "North corridor connecting local bakery, residential blocks, and walking loop."
            ),
            GridCell(
                id = "cell_pinecrest_park",
                name = "Pinecrest Commons",
                code = "#H3-8828",
                activeNeighborsCount = 55,
                openNeedsCount = 8,
                openOffersCount = 11,
                activeIssuesCount = 1,
                centerLat = 37.7720,
                centerLng = -122.4230,
                description = "Recreation center, community gardens, sports pitch, and safe exchange zone."
            ),
            GridCell(
                id = "cell_sunset_ridge",
                name = "Sunset Ridge",
                code = "#H3-8829",
                activeNeighborsCount = 24,
                openNeedsCount = 3,
                openOffersCount = 5,
                activeIssuesCount = 2,
                centerLat = 37.7795,
                centerLng = -122.4255,
                description = "Elevated street cluster with shared composting station and panoramic views."
            )
        )
        cellDao.insertCells(cells)

        // 2. Seed Needs (Requests)
        val posts = listOf(
            NeighborPost(
                id = "need_1",
                type = "NEED",
                title = "16ft extension ladder to clean gutters",
                description = "Need to clear out maple leaves from my second-story gutters this Saturday. Just need it for ~2 hours.",
                category = "TOOLS",
                authorName = "Marcus Chen",
                authorInitials = "MC",
                authorCellId = "cell_oakwood_center",
                cellName = "Oakwood Grove",
                lat = 37.7746,
                lng = -122.4190,
                approxDistanceBand = "within 280 m",
                urgency = "TIME_SENSITIVE",
                expiresInHours = 18,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 7200000
            ),
            NeighborPost(
                id = "offer_1",
                type = "OFFER",
                title = "16ft aluminum extension ladder & stabilizer",
                description = "Happy to lend my heavy-duty Werner ladder with standoff stabilizer. Can leave it on my covered front porch.",
                category = "TOOLS",
                authorName = "Robert Miller",
                authorInitials = "RM",
                authorCellId = "cell_oakwood_center",
                cellName = "Oakwood Grove",
                lat = 37.7751,
                lng = -122.4185,
                approxDistanceBand = "within 350 m",
                urgency = "NORMAL",
                expiresInHours = 72,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 14400000
            ),
            NeighborPost(
                id = "need_2",
                type = "NEED",
                title = "Need high school Calculus study partner or tutor",
                description = "Looking for a neighbor who understands limits and derivatives for 1 hour weekend review sessions.",
                category = "STUDY",
                authorName = "Priya Sharma",
                authorInitials = "PS",
                authorCellId = "cell_elmwood_north",
                cellName = "Elmwood Avenue",
                lat = 37.7782,
                lng = -122.4165,
                approxDistanceBand = "within 600 m",
                urgency = "NORMAL",
                expiresInHours = 48,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 21600000
            ),
            NeighborPost(
                id = "offer_2",
                type = "OFFER",
                title = "Free math tutoring & study help for high schoolers",
                description = "Retired engineer, happy to help high school neighbors with Algebra, Calculus, or Physics over coffee or at the park pavilion.",
                category = "STUDY",
                authorName = "Anita Roy",
                authorInitials = "AR",
                authorCellId = "cell_elmwood_north",
                cellName = "Elmwood Avenue",
                lat = 37.7779,
                lng = -122.4158,
                approxDistanceBand = "within 520 m",
                urgency = "NORMAL",
                expiresInHours = 96,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 28800000
            ),
            NeighborPost(
                id = "need_3",
                type = "NEED",
                title = "Hammer drill & masonry bit for shelving",
                description = "Mounting bookshelves into brick wall. Need a rotary drill for 30 minutes tonight.",
                category = "TOOLS",
                authorName = "Elena Rostova",
                authorInitials = "ER",
                authorCellId = "cell_oakwood_center",
                cellName = "Oakwood Grove",
                lat = 37.7742,
                lng = -122.4201,
                approxDistanceBand = "within 190 m",
                urgency = "URGENT",
                expiresInHours = 6,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 3600000
            ),
            NeighborPost(
                id = "offer_3",
                type = "OFFER",
                title = "DeWalt hammer drill & masonry bit set",
                description = "Ready to lend drill with full metric & imperial masonry set. Available all evening.",
                category = "TOOLS",
                authorName = "Tariq Al-Mansoor",
                authorInitials = "TA",
                authorCellId = "cell_oakwood_center",
                cellName = "Oakwood Grove",
                lat = 37.7745,
                lng = -122.4197,
                approxDistanceBand = "within 210 m",
                urgency = "NORMAL",
                expiresInHours = 48,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 86400000
            ),
            NeighborPost(
                id = "meetup_1",
                type = "MEETUP",
                title = "Casual Cricket & Catch — Pinecrest Pitch",
                description = "Looking for 4-6 more neighbors for friendly tape-ball cricket this Saturday at 4 PM! All skill levels welcome, bats provided.",
                category = "SPORTS",
                authorName = "David Kumar",
                authorInitials = "DK",
                authorCellId = "cell_pinecrest_park",
                cellName = "Pinecrest Commons",
                lat = 37.7725,
                lng = -122.4235,
                approxDistanceBand = "within 750 m",
                urgency = "NORMAL",
                expiresInHours = 36,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 10800000
            ),
            NeighborPost(
                id = "offer_4",
                type = "OFFER",
                title = "6 Organic heirloom tomato seedlings in pots",
                description = "Over-seeded Cherokee Purple and Sweet 100 cherry tomato plants. Free to any green-thumb neighbor!",
                category = "HOUSEHOLD",
                authorName = "Clara Davis",
                authorInitials = "CD",
                authorCellId = "cell_sunset_ridge",
                cellName = "Sunset Ridge",
                lat = 37.7798,
                lng = -122.4250,
                approxDistanceBand = "within 850 m",
                urgency = "NORMAL",
                expiresInHours = 48,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() - 43200000
            )
        )
        postDao.insertPosts(posts)

        // 3. Seed Community Issues
        val issues = listOf(
            CommunityIssue(
                id = "issue_1",
                title = "Flickering street lamp outside 42 Elmwood",
                description = "The sodium vapor street light has been strobing intermittently every night, making the sidewalk dark for evening walkers.",
                locationSnippet = "Corner of Elmwood Ave & 3rd Cross",
                cellId = "cell_elmwood_north",
                category = "LIGHTING",
                status = "IN_PROGRESS",
                reporterName = "Samantha Wu",
                upvotesCount = 19,
                hasUserUpvoted = true,
                assignedVolunteer = "Ward Maintenance Crew #4",
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ),
            CommunityIssue(
                id = "issue_2",
                title = "Large pothole at Pinecrest bicycle entry",
                description = "Deep asphalt crack near the park curb. Several cyclists had near falls during evening commute.",
                locationSnippet = "Pinecrest South Gate Bike Path",
                cellId = "cell_pinecrest_park",
                category = "ROADS",
                status = "ACKNOWLEDGED",
                reporterName = "Leo Martinez",
                upvotesCount = 14,
                hasUserUpvoted = false,
                assignedVolunteer = "Pending Public Works Review",
                createdAt = System.currentTimeMillis() - 86400000
            ),
            CommunityIssue(
                id = "issue_3",
                title = "Loose handrail on wooden creek pedestrian bridge",
                description = "Right-side handrail is loose near post #3. Needs tightening with heavy screws for toddler safety.",
                locationSnippet = "Oakwood Grove Creek Walkway",
                cellId = "cell_oakwood_center",
                category = "SAFETY",
                status = "REPORTED",
                reporterName = "Marcus Chen",
                upvotesCount = 8,
                hasUserUpvoted = false,
                assignedVolunteer = null,
                createdAt = System.currentTimeMillis() - 36000000
            ),
            CommunityIssue(
                id = "issue_4",
                title = "Storm branches cleared from sidewalk",
                description = "Volunteers cleared two heavy fallen eucalyptus limbs from the sidewalk following Tuesday wind.",
                locationSnippet = "Elmwood Park border",
                cellId = "cell_elmwood_north",
                category = "SANITATION",
                status = "RESOLVED",
                reporterName = "Neighborhood Volunteer Team",
                upvotesCount = 27,
                hasUserUpvoted = true,
                assignedVolunteer = "Green Volunteers & Robert Miller",
                createdAt = System.currentTimeMillis() - 86400000 * 4
            )
        )
        issueDao.insertIssues(issues)

        // 4. Seed Community Notices
        val notices = listOf(
            CommunityNotice(
                id = "notice_1",
                title = "Oakwood Water Main Valve Inspection",
                content = "Scheduled low water pressure window on Thursday between 10:00 AM and 12:30 PM for valve diagnostics. Please store drinking water in advance.",
                cellId = "cell_oakwood_center",
                authorityName = "Suburban Water Authority",
                isPinned = true,
                isUrgent = true,
                dateLabel = "Thursday, 10 AM - 12:30 PM",
                createdAt = System.currentTimeMillis() - 7200000
            ),
            CommunityNotice(
                id = "notice_2",
                title = "Safe Exchange Zone at Pinecrest Community Hall",
                content = "Neighbors borrowing or returning items are encouraged to use the well-lit, covered Community Hall lobby, monitored 24/7.",
                cellId = "cell_pinecrest_park",
                authorityName = "NearGrid Safety Council",
                isPinned = true,
                isUrgent = false,
                dateLabel = "Ongoing Community Guide",
                createdAt = System.currentTimeMillis() - 86400000 * 5
            ),
            CommunityNotice(
                id = "notice_3",
                title = "Annual Spring Seed Swap & Tool Lending Day",
                content = "Bring extra cuttings, garden tools, and seeds to share this Saturday at Pinecrest Pavilion from 9 AM to 1 PM.",
                cellId = "cell_pinecrest_park",
                authorityName = "Oakwood Community Gardeners",
                isPinned = false,
                isUrgent = false,
                dateLabel = "This Saturday, 9 AM",
                createdAt = System.currentTimeMillis() - 86400000
            )
        )
        noticeDao.insertNotices(notices)

        // 5. Seed Conversation & Messages
        val conv = Conversation(
            id = "conv_ladder",
            participantName = "Robert Miller",
            relatedPostTitle = "16ft extension ladder to clean gutters",
            relatedPostType = "NEED",
            lastMessage = "Sounds good! I'll leave the ladder by my garage door at 3 PM.",
            lastMessageTime = System.currentTimeMillis() - 1800000,
            distanceBand = "within 350 m",
            isResolved = false
        )
        conversationDao.insertConversation(conv)

        val messages = listOf(
            ChatMessage(
                id = "msg_1",
                conversationId = "conv_ladder",
                senderName = "Robert Miller",
                isFromCurrentUser = false,
                text = "Hi! I saw your request for the 16ft ladder. I'm right on Oakwood Lane (~350m away) and have one ready with stabilizer bars.",
                timestamp = System.currentTimeMillis() - 7200000,
                isPickupLocation = false
            ),
            ChatMessage(
                id = "msg_2",
                conversationId = "conv_ladder",
                senderName = "You",
                isFromCurrentUser = true,
                text = "That is so wonderful! Thank you so much Robert. Can I pick it up around 3 PM today?",
                timestamp = System.currentTimeMillis() - 3600000,
                isPickupLocation = false
            ),
            ChatMessage(
                id = "msg_3",
                conversationId = "conv_ladder",
                senderName = "Robert Miller",
                isFromCurrentUser = false,
                text = "Sounds good! I'll leave the ladder by my garage door at 3 PM. Safe exchange point is confirmed.",
                timestamp = System.currentTimeMillis() - 1800000,
                isPickupLocation = true,
                meetingPoint = "Oakwood Lane #14 Side Entrance"
            )
        )
        chatDao.insertMessages(messages)
    }
}
