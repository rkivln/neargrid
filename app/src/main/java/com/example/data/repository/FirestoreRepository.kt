package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.Conversation
import com.example.data.model.GeohashMessage
import com.example.data.model.NeighborPost
import com.example.data.model.PostComment
import com.example.data.model.UserProfile
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class FirestoreRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(getSafeFirestoreInstance(context))

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            android.util.Log.w("FirestoreRepository", "FirebaseAuth.getInstance() failed: ${e.message}")
            null
        }

    companion object {
        private fun getSafeFirestoreInstance(context: Context): FirebaseFirestore {
            // Ensure FirebaseApp is active before requesting Firestore
            val appContext = context.applicationContext
            if (com.google.firebase.FirebaseApp.getApps(appContext).isEmpty()) {
                try {
                    com.google.firebase.FirebaseApp.initializeApp(appContext)
                } catch (e: Exception) {
                    try {
                        val options = com.google.firebase.FirebaseOptions.Builder()
                            .setApplicationId("1:253737365034:android:858656ab161612d795dd53")
                            .setProjectId("gen-lang-client-0509703417")
                            .setApiKey("AIzaSyBSc8bWTsIYVL8Sj7kicx9zSZLOAcLD1LA")
                            .setGcmSenderId("253737365034")
                            .setStorageBucket("gen-lang-client-0509703417.firebasestorage.app")
                            .build()
                        com.google.firebase.FirebaseApp.initializeApp(appContext, options)
                    } catch (ignored: Exception) {}
                }
            }

            val databaseId = try {
                appContext.getString(R.string.firestore_database_id)
            } catch (e: Exception) {
                ""
            }

            if (databaseId.isNotBlank() && databaseId != "(default)") {
                try {
                    return FirebaseFirestore.getInstance(databaseId)
                } catch (e: Exception) {
                    android.util.Log.w("FirestoreRepository", "Named database ($databaseId) failed, falling back to default", e)
                }
            }

            return try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                android.util.Log.e("FirestoreRepository", "Default Firestore instance failed, attempting app fallback", e)
                val app = com.google.firebase.FirebaseApp.getApps(appContext).firstOrNull()
                    ?: try { com.google.firebase.FirebaseApp.initializeApp(appContext) } catch (ignored: Exception) { null }
                if (app != null) FirebaseFirestore.getInstance(app) else FirebaseFirestore.getInstance()
            }
        }
    }

    fun requireUserId(): String {
        return auth?.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null || userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                trySend(null)
                close()
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            val data = snapshot.data ?: run {
                trySend(null)
                return@addSnapshotListener
            }
            val profile = UserProfile(
                userId = userId,
                email = data["email"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "",
                handle = data["handle"] as? String ?: "",
                bio = data["bio"] as? String ?: "",
                cellId = data["cellId"] as? String ?: "cell_oakwood_center",
                cellName = data["cellName"] as? String ?: "Oakwood Grove",
                avatarPresetIndex = (data["avatarPresetIndex"] as? Number)?.toInt() ?: 0,
                avatarUri = data["avatarUri"] as? String,
                privacyRadiusMeters = (data["privacyRadiusMeters"] as? Number)?.toInt() ?: 500,
                fuzzyCoords = data["fuzzyCoords"] as? Boolean ?: true,
                incognito = data["incognito"] as? Boolean ?: false,
                allowDMs = data["allowDMs"] as? Boolean ?: true,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
            )
            trySend(profile)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "email" to profile.email,
            "displayName" to profile.displayName,
            "handle" to profile.handle,
            "bio" to profile.bio,
            "cellId" to profile.cellId,
            "cellName" to profile.cellName,
            "avatarPresetIndex" to profile.avatarPresetIndex,
            "privacyRadiusMeters" to profile.privacyRadiusMeters,
            "fuzzyCoords" to profile.fuzzyCoords,
            "incognito" to profile.incognito,
            "allowDMs" to profile.allowDMs,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (profile.avatarUri != null) {
            payload["avatarUri"] = profile.avatarUri
        }
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Posts (Needs / Offers / Meetups) ---

    fun observePosts(): Flow<List<NeighborPost>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "posts"
        val listener = db.collection(path)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }
                val currentUid = auth?.currentUser?.uid ?: ""
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val upvoters = (data["upvoterIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        NeighborPost(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            type = data["type"] as? String ?: "NEED",
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            category = data["category"] as? String ?: "TOOLS",
                            authorName = data["authorName"] as? String ?: "",
                            authorInitials = data["authorInitials"] as? String ?: "N",
                            authorCellId = data["authorCellId"] as? String ?: "cell_oakwood_center",
                            cellName = data["cellName"] as? String ?: "Oakwood Grove",
                            lat = (data["lat"] as? Number)?.toDouble() ?: 37.7749,
                            lng = (data["lng"] as? Number)?.toDouble() ?: -122.4194,
                            approxDistanceBand = data["approxDistanceBand"] as? String ?: "within 300 m",
                            urgency = data["urgency"] as? String ?: "NORMAL",
                            expiresInHours = (data["expiresInHours"] as? Number)?.toInt() ?: 48,
                            status = data["status"] as? String ?: "ACTIVE",
                            mediaUris = (data["mediaUris"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            mediaTypes = (data["mediaTypes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            upvotesCount = (data["upvotesCount"] as? Number)?.toInt() ?: upvoters.size,
                            hasUserUpvoted = upvoters.contains(currentUid),
                            upvoterIds = upvoters,
                            createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: NeighborPost): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docId = if (post.id.isNotBlank()) post.id else UUID.randomUUID().toString()
        val docRef = db.collection("posts").document(docId)
        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to uid,
            "type" to post.type,
            "title" to post.title,
            "description" to post.description,
            "category" to post.category,
            "authorName" to post.authorName,
            "authorInitials" to post.authorInitials,
            "authorCellId" to post.authorCellId,
            "cellName" to post.cellName,
            "lat" to post.lat,
            "lng" to post.lng,
            "approxDistanceBand" to post.approxDistanceBand,
            "urgency" to post.urgency,
            "expiresInHours" to post.expiresInHours,
            "status" to "ACTIVE",
            "mediaUris" to post.mediaUris,
            "mediaTypes" to post.mediaTypes,
            "upvotesCount" to 0,
            "upvoterIds" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun togglePostUpvote(postId: String, currentUpvotes: Int, currentlyUpvoted: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            if (currentlyUpvoted) {
                docRef.update(
                    mapOf(
                        "upvoterIds" to FieldValue.arrayRemove(uid),
                        "upvotesCount" to FieldValue.increment(-1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "upvoterIds" to FieldValue.arrayUnion(uid),
                        "upvotesCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updatePostStatus(postId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Community Issues ---

    fun observeIssues(): Flow<List<CommunityIssue>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "issues"
        val listener = db.collection(path)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }
                val currentUid = auth?.currentUser?.uid ?: ""
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val upvoters = (data["upvoterIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        CommunityIssue(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            locationSnippet = data["locationSnippet"] as? String ?: "",
                            cellId = data["cellId"] as? String ?: "cell_oakwood_center",
                            category = data["category"] as? String ?: "LIGHTING",
                            status = data["status"] as? String ?: "REPORTED",
                            reporterName = data["reporterName"] as? String ?: "",
                            reporterId = data["reporterId"] as? String ?: "",
                            upvotesCount = (data["upvotesCount"] as? Number)?.toInt() ?: upvoters.size,
                            hasUserUpvoted = upvoters.contains(currentUid),
                            upvoterIds = upvoters,
                            assignedVolunteer = data["assignedVolunteer"] as? String,
                            mediaUris = (data["mediaUris"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            mediaTypes = (data["mediaTypes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items.sortedByDescending { it.createdAt })
            }
        awaitClose { listener.remove() }
    }

    suspend fun createIssue(issue: CommunityIssue): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docId = if (issue.id.isNotBlank()) issue.id else UUID.randomUUID().toString()
        val docRef = db.collection("issues").document(docId)
        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "title" to issue.title,
            "description" to issue.description,
            "locationSnippet" to issue.locationSnippet,
            "cellId" to issue.cellId,
            "category" to issue.category,
            "status" to "REPORTED",
            "reporterName" to issue.reporterName,
            "reporterId" to uid,
            "upvotesCount" to 1,
            "upvoterIds" to listOf(uid),
            "mediaUris" to issue.mediaUris,
            "mediaTypes" to issue.mediaTypes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleIssueUpvote(issueId: String, currentUpvotes: Int, currentlyUpvoted: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = db.collection("issues").document(issueId)
        val newUpvotes = if (currentlyUpvoted) (currentUpvotes - 1).coerceAtLeast(0) else currentUpvotes + 1
        val updates = hashMapOf<String, Any>(
            "upvotesCount" to newUpvotes,
            "upvoterIds" to if (currentlyUpvoted) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateIssueStatus(issueId: String, newStatus: String, volunteer: String?): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val docRef = db.collection("issues").document(issueId)
        val updates = hashMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (volunteer != null) {
            updates["assignedVolunteer"] = volunteer
        }
        try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Conversations & Direct Coordination ---

    fun observeConversations(): Flow<List<Conversation>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "conversations"
        val listener = db.collection(path)
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val pIds = (data["participantIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val pNames = (data["participantNames"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val otherName = pNames.firstOrNull { it != (auth?.currentUser?.displayName ?: "") }
                            ?: pNames.firstOrNull() ?: "Neighbor"

                        Conversation(
                            id = doc.id,
                            participantIds = pIds,
                            participantNames = pNames,
                            participantName = otherName,
                            relatedPostTitle = data["relatedPostTitle"] as? String ?: "",
                            relatedPostType = data["relatedPostType"] as? String ?: "NEED",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            lastMessageTime = (data["lastMessageTime"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                            distanceBand = data["distanceBand"] as? String ?: "within 300 m",
                            isResolved = data["isResolved"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items.sortedByDescending { it.lastMessageTime })
            }
        awaitClose { listener.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null || conversationId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "conversations/$conversationId/messages"
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }
                val currentUid = auth?.currentUser?.uid ?: ""
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val senderId = data["senderId"] as? String ?: ""
                        ChatMessage(
                            id = doc.id,
                            conversationId = conversationId,
                            senderId = senderId,
                            senderName = data["senderName"] as? String ?: "Neighbor",
                            isFromCurrentUser = senderId == currentUid,
                            text = data["text"] as? String ?: "",
                            timestamp = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                            isPickupLocation = data["isPickupLocation"] as? Boolean ?: false,
                            meetingPoint = data["meetingPoint"] as? String
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items.sortedBy { it.timestamp })
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        isPickupLocation: Boolean = false,
        meetingPoint: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val displayName = auth?.currentUser?.displayName ?: "Neighbor"
        val msgDoc = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document(UUID.randomUUID().toString())

        val msgPayload = hashMapOf<String, Any>(
            "id" to msgDoc.id,
            "conversationId" to conversationId,
            "senderId" to uid,
            "senderName" to displayName,
            "text" to text,
            "isPickupLocation" to isPickupLocation,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (meetingPoint != null) {
            msgPayload["meetingPoint"] = meetingPoint
        }

        try {
            msgDoc.set(msgPayload).await()
            // Update parent conversation
            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgDoc.path)
            Result.failure(e)
        }
    }

    suspend fun startConversationForPost(post: NeighborPost): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val currentUserName = auth?.currentUser?.displayName ?: "Neighbor"
        val convId = "conv_${post.id}_${uid.take(6)}"
        val convRef = db.collection("conversations").document(convId)

        val convPayload = hashMapOf<String, Any>(
            "id" to convId,
            "participantIds" to listOf(uid, post.userId.ifBlank { "neighbor_author" }),
            "participantNames" to listOf(currentUserName, post.authorName),
            "relatedPostId" to post.id,
            "relatedPostTitle" to post.title,
            "relatedPostType" to post.type,
            "lastMessage" to "Connecting on \"${post.title}\"",
            "lastMessageTime" to FieldValue.serverTimestamp(),
            "distanceBand" to post.approxDistanceBand,
            "isResolved" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            convRef.set(convPayload).await()
            Result.success(convId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, convRef.path)
            Result.failure(e)
        }
    }

    // --- Geohash Messages (Broadcasts by Geohash Area) ---

    fun observeGeohashMessages(geohash: String? = null): Flow<List<GeohashMessage>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "geohash_messages"
        val query = if (!geohash.isNullOrBlank()) {
            db.collection(path).whereEqualTo("geohash", geohash)
        } else {
            db.collection(path)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                trySend(emptyList())
                close()
                return@addSnapshotListener
            }
            val currentUid = auth?.currentUser?.uid ?: ""
            val items = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val senderId = data["userId"] as? String ?: ""
                    val ts = (data["timestamp"] as? Timestamp)?.toDate()?.time
                        ?: (data["createdAt"] as? Timestamp)?.toDate()?.time
                        ?: System.currentTimeMillis()

                    GeohashMessage(
                        id = doc.id,
                        userId = senderId,
                        authorName = data["authorName"] as? String ?: "Neighbor",
                        authorInitials = data["authorInitials"] as? String ?: "N",
                        text = data["text"] as? String ?: "",
                        geohash = data["geohash"] as? String ?: "",
                        latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                        timestamp = ts,
                        localityName = data["localityName"] as? String ?: "",
                        isFromCurrentUser = senderId == currentUid
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            trySend(items.sortedByDescending { it.timestamp })
        }
        awaitClose { listener.remove() }
    }

    suspend fun postGeohashMessage(
        text: String,
        latitude: Double,
        longitude: Double,
        geohash: String,
        authorName: String = "",
        localityName: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val displayName = authorName.ifBlank { auth?.currentUser?.displayName ?: "Neighbor" }
        val initials = displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "N" }

        val docId = UUID.randomUUID().toString()
        val docRef = db.collection("geohash_messages").document(docId)

        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to uid,
            "authorName" to displayName,
            "authorInitials" to initials,
            "text" to text.trim(),
            "geohash" to geohash.trim().lowercase(),
            "latitude" to latitude,
            "longitude" to longitude,
            "localityName" to localityName,
            "timestamp" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Threaded Post Comments ---

    fun observePostComments(postId: String): Flow<List<PostComment>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null || postId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "posts/$postId/comments"
        val listener = db.collection(path)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        PostComment(
                            id = doc.id,
                            postId = postId,
                            authorId = data["authorId"] as? String ?: "",
                            authorName = data["authorName"] as? String ?: "Neighbor",
                            authorInitials = data["authorInitials"] as? String ?: "N",
                            text = data["text"] as? String ?: "",
                            parentId = data["parentId"] as? String,
                            timestamp = (data["timestamp"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items.sortedBy { it.timestamp })
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPostComment(
        postId: String,
        text: String,
        authorName: String = "",
        parentId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val displayName = authorName.ifBlank { auth?.currentUser?.displayName ?: "Neighbor" }
        val initials = displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "N" }

        val docId = UUID.randomUUID().toString()
        val docRef = db.collection("posts").document(postId).collection("comments").document(docId)

        val payload = hashMapOf<String, Any?>(
            "id" to docId,
            "postId" to postId,
            "authorId" to uid,
            "authorName" to displayName,
            "authorInitials" to initials,
            "text" to text.trim(),
            "parentId" to parentId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload.filterValues { it != null } as Map<String, Any>).await()
            Result.success(docId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
