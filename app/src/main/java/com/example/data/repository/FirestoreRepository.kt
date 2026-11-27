package com.example.data.repository

import android.content.Context
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityIssue
import com.example.data.model.Conversation
import com.example.data.model.GeohashMessage
import com.example.data.model.NeighborPost
import com.example.data.model.PostComment
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseBootstrap
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class FirestoreRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(getSafeFirestoreInstance(context))

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance(db.app)
        } catch (e: Exception) {
            android.util.Log.w("FirestoreRepository", "FirebaseAuth.getInstance(db.app) failed: ${e.message}")
            null
        }

    companion object {
        private val ALLOWED_LOCATION_PRECISIONS = setOf("EXACT", "APPROXIMATE", "NEIGHBORHOOD")

        private fun getSafeFirestoreInstance(context: Context): FirebaseFirestore {
            return FirebaseBootstrap.getFirestore(context)
                ?: throw IllegalStateException(
                    "Named Firestore database could not be initialized. Check R.string.firestore_database_id."
                )
        }

        internal fun sanitizeUserProfilePayload(
            uid: String,
            profile: UserProfile,
            includeCreatedAt: Boolean = false
        ): HashMap<String, Any> {
            val safeDisplayName = profile.displayName.trim().ifBlank { "Local Resident" }.take(100)
            val safeHandle = profile.handle.trim().take(60)
            val safeBio = profile.bio.trim().take(500)
            val safeCellId = profile.cellId.trim().ifBlank { "cell_local" }.take(80)
            val safeCellName = profile.cellName.trim().ifBlank { "Local Area" }.take(100)
            val safePrecision = if (profile.locationPrecision in ALLOWED_LOCATION_PRECISIONS) {
                profile.locationPrecision
            } else {
                "EXACT"
            }

            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to safeDisplayName,
                "handle" to safeHandle,
                "bio" to safeBio,
                "cellId" to safeCellId,
                "cellName" to safeCellName,
                "avatarPresetIndex" to profile.avatarPresetIndex,
                "privacyRadiusMeters" to profile.privacyRadiusMeters,
                "fuzzyCoords" to profile.fuzzyCoords,
                "incognito" to profile.incognito,
                "allowDMs" to profile.allowDMs,
                "locationPrecision" to safePrecision,
                "hideOnlineStatus" to profile.hideOnlineStatus,
                "hideExactDistance" to profile.hideExactDistance,
                "blockedUserIds" to profile.blockedUserIds.take(200),
                "isOnline" to profile.isOnline,
                "lastActiveAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            profile.avatarUri?.trim()?.takeIf { it.isNotEmpty() }?.let { uri ->
                payload["avatarUri"] = uri.take(1000)
            }
            if (profile.latitude != 0.0 && profile.longitude != 0.0) {
                payload["latitude"] = profile.latitude
                payload["longitude"] = profile.longitude
            }
            if (profile.geohash.isNotBlank()) {
                payload["geohash"] = profile.geohash.removePrefix("#").lowercase(Locale.ROOT).take(32)
            }
            if (includeCreatedAt) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            return payload
        }

        internal fun sanitizePrivateAccountPayload(
            uid: String,
            email: String,
            includeCreatedAt: Boolean = false
        ): HashMap<String, Any> {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "email" to email.trim().take(200),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (includeCreatedAt) {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            return payload
        }

        internal fun parseUserProfile(userId: String, data: Map<String, Any?>): UserProfile {
            return UserProfile(
                userId = userId,
                email = data["email"] as? String ?: "",
                displayName = data["displayName"] as? String ?: "Neighbor",
                handle = data["handle"] as? String ?: "",
                bio = data["bio"] as? String ?: "",
                cellId = data["cellId"] as? String ?: "cell_oakwood_center",
                cellName = data["cellName"] as? String ?: "Local Area",
                avatarPresetIndex = (data["avatarPresetIndex"] as? Number)?.toInt() ?: 0,
                avatarUri = data["avatarUri"] as? String,
                privacyRadiusMeters = (data["privacyRadiusMeters"] as? Number)?.toInt() ?: 500,
                fuzzyCoords = data["fuzzyCoords"] as? Boolean ?: true,
                incognito = data["incognito"] as? Boolean ?: false,
                allowDMs = data["allowDMs"] as? Boolean ?: true,
                locationPrecision = data["locationPrecision"] as? String ?: "EXACT",
                hideOnlineStatus = data["hideOnlineStatus"] as? Boolean ?: false,
                hideExactDistance = data["hideExactDistance"] as? Boolean ?: false,
                blockedUserIds = (data["blockedUserIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                geohash = data["geohash"] as? String ?: "",
                isOnline = data["isOnline"] as? Boolean ?: false,
                lastActiveAt = (data["lastActiveAt"] as? Timestamp)?.toDate()?.time
                    ?: (data["lastActiveAt"] as? Number)?.toLong() ?: 0L,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
            )
        }
    }

    fun requireUserId(): String {
        return auth?.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---

    /**
     * Ensures a Firestore `users/{uid}` document exists after Google Sign-In,
     * retrieving the existing document without overwriting user customizations or
     * creating a rule-compliant profile if none exists yet.
     */
    suspend fun ensureUserProfileExists(
        user: FirebaseUser,
        cellId: String = "cell_local",
        cellName: String = "My Neighborhood"
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentUid = auth?.currentUser?.uid
        val uid = user.uid
        if (currentUid == null || currentUid != uid) {
            return@withContext Result.failure(
                IllegalStateException("Authenticated Firebase UID mismatch while synchronizing user profile.")
            )
        }
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val existing = parseUserProfile(uid, snapshot.data.orEmpty())
                val updates = hashMapOf<String, Any>(
                    "isOnline" to true,
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                runCatching { docRef.update(updates).await() }
                syncPrivateAccountRecord(uid, user.email.orEmpty())
                Result.success(existing)
            } else {
                val defaultEmail = user.email?.trim()?.take(200).orEmpty()
                val defaultName = user.displayName?.trim()?.ifBlank { null }
                    ?: defaultEmail.substringBefore("@").ifBlank { "Local Resident" }
                val defaultHandlePrefix = defaultEmail.substringBefore("@")
                    .filter { it.isLetterOrDigit() || it == '_' }
                    .ifBlank { "resident" }
                val newProfile = UserProfile(
                    userId = uid,
                    email = "",
                    displayName = defaultName.take(100),
                    handle = "@$defaultHandlePrefix".take(60),
                    bio = "Civic resident connected via Google Resident Pass.",
                    cellId = cellId.ifBlank { "cell_local" }.take(80),
                    cellName = cellName.ifBlank { "My Neighborhood" }.take(100),
                    avatarUri = user.photoUrl?.toString()?.take(1000),
                    privacyRadiusMeters = 1000,
                    fuzzyCoords = false,
                    incognito = false,
                    allowDMs = true,
                    locationPrecision = "EXACT",
                    isOnline = true
                )
                val payload = sanitizeUserProfilePayload(
                    uid = uid,
                    profile = newProfile,
                    includeCreatedAt = true
                )
                docRef.set(payload).await()
                syncPrivateAccountRecord(uid, defaultEmail)
                Result.success(newProfile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }

    private suspend fun syncPrivateAccountRecord(uid: String, email: String) {
        val privateRef = db.collection("users").document(uid)
            .collection("private").document("account")
        try {
            val snap = privateRef.get().await()
            if (!snap.exists()) {
                privateRef.set(sanitizePrivateAccountPayload(uid, email, includeCreatedAt = true)).await()
            } else {
                privateRef.update(sanitizePrivateAccountPayload(uid, email, includeCreatedAt = false)).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, privateRef.path, auth)
        }
    }

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
                if (auth?.currentUser?.uid == uid) {
                    handleFirestoreError(error, OperationType.GET, docRef.path, auth)
                }
                close(error)
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
            trySend(parseUserProfile(userId, data))
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val payload = sanitizeUserProfilePayload(
                    uid = uid,
                    profile = profile,
                    includeCreatedAt = true
                )
                docRef.set(payload).await()
            } else {
                val payload = sanitizeUserProfilePayload(
                    uid = uid,
                    profile = profile,
                    includeCreatedAt = false
                )
                docRef.update(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateUserLocationPresence(latitude: Double, longitude: Double, geohash: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth?.currentUser?.uid ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        if (latitude == 0.0 && longitude == 0.0) return@withContext Result.success(Unit)
        val docRef = db.collection("users").document(uid)
        val cleanHash = geohash.removePrefix("#").lowercase(Locale.ROOT).take(32)
        val updates = hashMapOf<String, Any>(
            "latitude" to latitude,
            "longitude" to longitude,
            "geohash" to cleanHash,
            "isOnline" to true,
            "lastActiveAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(updates).await()
            } else {
                val user = auth?.currentUser
                val email = user?.email?.trim()?.take(200).orEmpty()
                val defaultName = user?.displayName?.trim()?.ifBlank { null } ?: "Neighbor"
                val defaultHandle = "@" + (user?.displayName?.replace(" ", "")?.lowercase(Locale.ROOT)?.ifBlank { null } ?: "resident")
                val initialProfile = UserProfile(
                    userId = uid,
                    email = "",
                    displayName = defaultName,
                    handle = defaultHandle,
                    bio = "Local neighbor on NearGrid",
                    cellId = "cell_local",
                    cellName = "Local Area",
                    avatarUri = user?.photoUrl?.toString()?.take(1000),
                    privacyRadiusMeters = 500,
                    fuzzyCoords = true,
                    incognito = false,
                    allowDMs = true,
                    locationPrecision = "EXACT",
                    latitude = latitude,
                    longitude = longitude,
                    geohash = cleanHash,
                    isOnline = true
                )
                docRef.set(sanitizeUserProfilePayload(uid, initialProfile, includeCreatedAt = true)).await()
                if (email.isNotBlank()) {
                    syncPrivateAccountRecord(uid, email)
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun markUserOfflineBestEffort() = withContext(Dispatchers.IO) {
        val uid = auth?.currentUser?.uid ?: return@withContext
        val docRef = db.collection("users").document(uid)
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(
                    mapOf(
                        "isOnline" to false,
                        "lastActiveAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.d("FirestoreRepository", "Offline presence update skipped on sign-out: ${e.message}")
        }
    }

    suspend fun submitSupportTicket(
        category: String,
        message: String,
        userNickname: String,
        appVersion: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
            ?: return@withContext Result.failure(IllegalStateException("User must be signed in with Google before submitting a support ticket."))
        val uid = user.uid
        val trimmedMessage = message.trim().take(2000)
        if (trimmedMessage.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Support message cannot be blank."))
        }
        val ticketId = UUID.randomUUID().toString()
        val docRef = db.collection("support_tickets").document(ticketId)
        val ticket = mapOf(
            "ticketId" to ticketId,
            "userId" to uid,
            "userName" to userNickname.trim().ifBlank { user.displayName ?: "Resident" }.take(100),
            "userEmail" to (user.email ?: "").trim().take(200),
            "category" to category.trim().ifBlank { "GENERAL" }.take(60),
            "message" to trimmedMessage,
            "appVersion" to appVersion.trim().take(40),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "status" to "OPEN"
        )
        try {
            docRef.set(ticket).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    fun observeNearbyUsers(geohashPrefix: String? = null): Flow<List<UserProfile>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val cleanPrefix = geohashPrefix?.removePrefix("#")?.lowercase(Locale.ROOT)?.trim()
        val baseQuery = db.collection("users")
        val query = if (!cleanPrefix.isNullOrBlank() && cleanPrefix.length >= 3) {
            val prefix = cleanPrefix.take(3)
            baseQuery.whereGreaterThanOrEqualTo("geohash", prefix)
                .whereLessThanOrEqualTo("geohash", prefix + "\uf8ff")
                .limit(100)
        } else {
            baseQuery.limit(100)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (auth?.currentUser?.uid == uid) {
                    handleFirestoreError(error, OperationType.LIST, "users", auth)
                }
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                parseUserProfile(doc.id, data)
            }
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun blockUser(targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth?.currentUser?.uid ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        if (targetUserId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Target user ID required"))
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "blockedUserIds" to FieldValue.arrayUnion(targetUserId),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun unblockUser(targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth?.currentUser?.uid ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        if (targetUserId.isBlank()) return@withContext Result.failure(IllegalArgumentException("Target user ID required"))
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "blockedUserIds" to FieldValue.arrayRemove(targetUserId),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun reportUser(
        targetUserId: String,
        targetDisplayName: String,
        reason: String,
        notes: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth?.currentUser?.uid ?: return@withContext Result.failure(IllegalStateException("Not authenticated"))
        val reportId = UUID.randomUUID().toString()
        val docRef = db.collection("reports").document(reportId)
        val data = hashMapOf<String, Any>(
            "id" to reportId,
            "reporterId" to uid,
            "targetUserId" to targetUserId.trim().take(128),
            "targetDisplayName" to targetDisplayName.trim().take(100),
            "reason" to reason.trim().ifBlank { "OTHER" }.take(100),
            "notes" to notes.trim().take(1000),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    // --- Posts (Needs / Offers / Meetups) ---

    fun observePosts(cellId: String? = null, geohashPrefix: String? = null): Flow<List<NeighborPost>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        if (geohashPrefix.isNullOrBlank() && cellId.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "posts"
        var query: com.google.firebase.firestore.Query = db.collection(path).whereEqualTo("status", "ACTIVE")
        if (!geohashPrefix.isNullOrBlank()) {
            val prefix = geohashPrefix.trim().lowercase(Locale.ROOT).removePrefix("#")
            query = query.whereGreaterThanOrEqualTo("geohash", prefix)
                .whereLessThanOrEqualTo("geohash", prefix + "\uf8ff")
        } else if (!cellId.isNullOrBlank()) {
            query = query.whereEqualTo("authorCellId", cellId)
        }
        val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (auth?.currentUser?.uid == uid) {
                        handleFirestoreError(error, OperationType.LIST, path, auth)
                    }
                    close(error)
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
        val safeTitle = post.title.trim().take(200)
        if (safeTitle.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Post title cannot be blank."))
        }
        val safeType = if (post.type in setOf("NEED", "OFFER", "MEETUP")) post.type else "NEED"
        val safeUrgency = if (post.urgency in setOf("NORMAL", "TIME_SENSITIVE", "URGENT")) post.urgency else "NORMAL"
        val docId = if (post.id.isNotBlank()) post.id else UUID.randomUUID().toString()
        val docRef = db.collection("posts").document(docId)
        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to uid,
            "type" to safeType,
            "title" to safeTitle,
            "description" to post.description.trim().take(2000),
            "category" to post.category.trim().ifBlank { "TOOLS" }.take(50),
            "authorName" to post.authorName.trim().ifBlank { "Neighbor" }.take(100),
            "authorInitials" to post.authorInitials.trim().ifBlank { "N" }.take(10),
            "authorCellId" to post.authorCellId.trim().ifBlank { "cell_local" }.take(80),
            "cellName" to post.cellName.trim().ifBlank { "Local Area" }.take(100),
            "lat" to post.lat,
            "lng" to post.lng,
            "geohash" to com.example.util.GeohashUtil.encode(post.lat, post.lng, 7),
            "approxDistanceBand" to post.approxDistanceBand.trim().ifBlank { "nearby" }.take(50),
            "urgency" to safeUrgency,
            "expiresInHours" to post.expiresInHours.coerceIn(1, 720),
            "status" to "ACTIVE",
            "mediaUris" to post.mediaUris.take(10),
            "mediaTypes" to post.mediaTypes.take(10),
            "upvotesCount" to 0,
            "upvoterIds" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun togglePostUpvote(postId: String, currentUpvotes: Int, currentlyUpvoted: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = db.collection("posts").document(postId)
        val safeUpvotes = if (currentlyUpvoted) (currentUpvotes - 1).coerceAtLeast(0) else currentUpvotes + 1
        try {
            if (currentlyUpvoted) {
                docRef.update(
                    mapOf(
                        "upvoterIds" to FieldValue.arrayRemove(uid),
                        "upvotesCount" to safeUpvotes,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "upvoterIds" to FieldValue.arrayUnion(uid),
                        "upvotesCount" to safeUpvotes,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updatePostStatus(postId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val safeStatus = if (status in setOf("ACTIVE", "RESOLVED", "CANCELLED")) status else "ACTIVE"
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.update(
                mapOf(
                    "status" to safeStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updatePostContent(
        postId: String,
        title: String,
        description: String,
        category: String,
        urgency: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val safeTitle = title.trim().take(200)
        if (safeTitle.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Post title cannot be blank."))
        }
        val safeUrgency = if (urgency in setOf("NORMAL", "TIME_SENSITIVE", "URGENT")) urgency else "NORMAL"
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.update(
                mapOf(
                    "title" to safeTitle,
                    "description" to description.trim().take(2000),
                    "category" to category.trim().ifBlank { "TOOLS" }.take(50),
                    "urgency" to safeUrgency,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val docRef = db.collection("posts").document(postId)
        try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
            Result.failure(e)
        }
    }

    // --- Community Issues ---

    fun observeIssues(cellId: String? = null): Flow<List<CommunityIssue>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "issues"
        var query: com.google.firebase.firestore.Query = db.collection(path)
        if (!cellId.isNullOrBlank()) {
            query = query.whereEqualTo("cellId", cellId)
        }
        val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (auth?.currentUser?.uid == uid) {
                        handleFirestoreError(error, OperationType.LIST, path, auth)
                    }
                    close(error)
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
        val safeTitle = issue.title.trim().take(200)
        if (safeTitle.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Issue title cannot be blank."))
        }
        val allowedCategories = setOf("LIGHTING", "ROADS", "SAFETY", "SANITATION", "PARKS", "OTHER")
        val safeCategory = if (issue.category in allowedCategories) issue.category else "OTHER"
        val docId = if (issue.id.isNotBlank()) issue.id else UUID.randomUUID().toString()
        val docRef = db.collection("issues").document(docId)
        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "title" to safeTitle,
            "description" to issue.description.trim().take(2000),
            "locationSnippet" to issue.locationSnippet.trim().take(300),
            "cellId" to issue.cellId.trim().ifBlank { "cell_local" }.take(80),
            "category" to safeCategory,
            "status" to "REPORTED",
            "reporterName" to issue.reporterName.trim().ifBlank { "Resident" }.take(100),
            "reporterId" to uid,
            "upvotesCount" to 1,
            "upvoterIds" to listOf(uid),
            "mediaUris" to issue.mediaUris.take(10),
            "mediaTypes" to issue.mediaTypes.take(10),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateIssueStatus(issueId: String, newStatus: String, volunteer: String?): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val safeStatus = if (newStatus in setOf("REPORTED", "ACKNOWLEDGED", "IN_PROGRESS", "RESOLVED")) {
            newStatus
        } else {
            "REPORTED"
        }
        val docRef = db.collection("issues").document(issueId)
        val updates = hashMapOf<String, Any>(
            "status" to safeStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (volunteer != null) {
            updates["assignedVolunteer"] = volunteer.trim().take(200)
        }
        try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
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
                    if (auth?.currentUser?.uid == uid) {
                        handleFirestoreError(error, OperationType.LIST, path, auth)
                    }
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        val pIds = (data["participantIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val pNames = (data["participantNames"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val otherIndex = pIds.indexOfFirst { it != uid }
                        val otherName = (if (otherIndex >= 0) pNames.getOrNull(otherIndex) else null)
                            ?.takeIf { it.isNotBlank() }
                            ?: pNames.firstOrNull { it.isNotBlank() && it != (auth?.currentUser?.displayName ?: "") }
                            ?: pNames.firstOrNull()
                            ?: "Neighbor"

                        Conversation(
                            id = doc.id,
                            participantIds = pIds,
                            participantNames = pNames,
                            participantName = otherName,
                            relatedPostTitle = data["relatedPostTitle"] as? String ?: "",
                            relatedPostType = data["relatedPostType"] as? String ?: "NEED",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
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
                    if (auth?.currentUser?.uid == uid) {
                        handleFirestoreError(error, OperationType.LIST, path, auth)
                    }
                    close(error)
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
        val safeText = text.trim().take(2000)
        if (safeText.isEmpty() || conversationId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Message text cannot be blank."))
        }
        val displayName = (auth?.currentUser?.displayName ?: "Neighbor").trim().ifBlank { "Neighbor" }.take(100)
        val msgDoc = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document(UUID.randomUUID().toString())

        val msgPayload = hashMapOf<String, Any>(
            "id" to msgDoc.id,
            "conversationId" to conversationId,
            "senderId" to uid,
            "senderName" to displayName,
            "text" to safeText,
            "isPickupLocation" to isPickupLocation,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (!meetingPoint.isNullOrBlank()) {
            msgPayload["meetingPoint"] = meetingPoint.trim().take(200)
        }

        try {
            msgDoc.set(msgPayload).await()
            // Update parent conversation
            db.collection("conversations").document(conversationId).update(
                mapOf(
                    "lastMessage" to safeText.take(500),
                    "lastMessageSenderId" to uid,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgDoc.path, auth)
            Result.failure(e)
        }
    }

    suspend fun editChatMessage(
        conversationId: String,
        messageId: String,
        newText: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val safeText = newText.trim().take(2000)
        if (safeText.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Message text cannot be blank."))
        }
        val msgDoc = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document(messageId)
        try {
            msgDoc.update(
                mapOf(
                    "text" to safeText
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgDoc.path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteChatMessage(
        conversationId: String,
        messageId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val msgDoc = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document(messageId)
        try {
            msgDoc.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, msgDoc.path, auth)
            Result.failure(e)
        }
    }

    suspend fun startConversationForPost(post: NeighborPost): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val currentUserName = (auth?.currentUser?.displayName ?: "Neighbor").trim().take(100)
        val convId = "conv_${post.id}_${uid.take(6)}"
        val convRef = db.collection("conversations").document(convId)

        try {
            val existing = convRef.get().await()
            if (existing.exists()) {
                convRef.update(
                    mapOf(
                        "relatedPostTitle" to post.title.take(200),
                        "relatedPostType" to post.type.take(50),
                        "distanceBand" to post.approxDistanceBand.take(50),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val convPayload = hashMapOf<String, Any>(
                    "id" to convId,
                    "participantIds" to listOf(uid, post.userId.ifBlank { "neighbor_author" }),
                    "participantNames" to listOf(currentUserName, post.authorName.trim().ifBlank { "Neighbor" }.take(100)),
                    "relatedPostId" to post.id,
                    "relatedPostTitle" to post.title.take(200),
                    "relatedPostType" to post.type.take(50),
                    "lastMessage" to "Connecting on \"${post.title}\"".take(500),
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "distanceBand" to post.approxDistanceBand.take(50),
                    "isResolved" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                convRef.set(convPayload).await()
            }
            Result.success(convId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, convRef.path, auth)
            Result.failure(e)
        }
    }

    suspend fun startDirectConversationWithNeighbor(neighborUserId: String, neighborName: String): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val currentUserName = (auth?.currentUser?.displayName ?: "Neighbor").trim().take(100)
        val convId = if (uid < neighborUserId) "dm_${uid.take(8)}_${neighborUserId.take(8)}" else "dm_${neighborUserId.take(8)}_${uid.take(8)}"
        val convRef = db.collection("conversations").document(convId)

        try {
            val existing = convRef.get().await()
            if (existing.exists()) {
                convRef.update(
                    mapOf(
                        "distanceBand" to "nearby on map",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val convPayload = hashMapOf<String, Any>(
                    "id" to convId,
                    "participantIds" to listOf(uid, neighborUserId),
                    "participantNames" to listOf(currentUserName, neighborName.trim().ifBlank { "Neighbor" }.take(100)),
                    "lastMessage" to "Connecting nearby on the map",
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "distanceBand" to "nearby on map",
                    "isResolved" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                convRef.set(convPayload).await()
            }
            Result.success(convId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, convRef.path, auth)
            Result.failure(e)
        }
    }

    // --- Geohash Messages (Broadcasts by Geohash Area) ---

    fun observeGeohashMessages(geohash: String? = null): Flow<List<GeohashMessage>> = callbackFlow {
        val uid = auth?.currentUser?.uid
        if (uid == null || geohash.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "geohash_messages"
        val cleanHash = geohash.trim().lowercase(Locale.ROOT).removePrefix("#")
        val prefix = cleanHash.take(4)
        val query = db.collection(path)
            .whereGreaterThanOrEqualTo("geohash", prefix)
            .whereLessThanOrEqualTo("geohash", prefix + "\uf8ff")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (auth?.currentUser?.uid == uid) {
                    handleFirestoreError(error, OperationType.LIST, path, auth)
                }
                close(error)
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
        val safeText = text.trim().take(2000)
        val safeHash = geohash.trim().lowercase(Locale.ROOT).removePrefix("#").take(32)
        if (safeText.isEmpty() || safeHash.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Broadcast text and geohash are required."))
        }
        val displayName = authorName.trim().ifBlank { auth?.currentUser?.displayName ?: "Neighbor" }.take(100)
        val initials = displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "N" }
            .take(10)

        val docId = UUID.randomUUID().toString()
        val docRef = db.collection("geohash_messages").document(docId)

        val payload = hashMapOf<String, Any>(
            "id" to docId,
            "userId" to uid,
            "authorName" to displayName,
            "authorInitials" to initials,
            "text" to safeText,
            "geohash" to safeHash,
            "latitude" to latitude,
            "longitude" to longitude,
            "localityName" to localityName.trim().take(100),
            "timestamp" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
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
                    if (auth?.currentUser?.uid == uid) {
                        handleFirestoreError(error, OperationType.LIST, path, auth)
                    }
                    close(error)
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
        val safeText = text.trim().take(2000)
        if (safeText.isEmpty() || postId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Comment text and postId are required."))
        }
        val displayName = authorName.trim().ifBlank { auth?.currentUser?.displayName ?: "Neighbor" }.take(100)
        val initials = displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "N" }
            .take(10)

        val docId = UUID.randomUUID().toString()
        val docRef = db.collection("posts").document(postId).collection("comments").document(docId)

        val payload = hashMapOf<String, Any?>(
            "id" to docId,
            "postId" to postId,
            "authorId" to uid,
            "authorName" to displayName,
            "authorInitials" to initials,
            "text" to safeText,
            "parentId" to parentId?.takeIf { it.isNotBlank() },
            "timestamp" to FieldValue.serverTimestamp()
        )

        try {
            @Suppress("UNCHECKED_CAST")
            docRef.set(payload.filterValues { it != null } as Map<String, Any>).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
            Result.failure(e)
        }
    }
}
