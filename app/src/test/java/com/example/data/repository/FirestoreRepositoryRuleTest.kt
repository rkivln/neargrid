package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CommunityIssue
import com.example.data.model.NeighborPost
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.UUID

@Ignore("Requires local Firebase emulator process")
class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createPost_authenticatedUser_succeeds() = runBlocking {
        val uid = signInTestUser("testuser1@example.com")
        val repo = FirestoreRepository(firestore)

        val postId = UUID.randomUUID().toString()
        val post = NeighborPost(
            id = postId,
            userId = uid,
            type = "NEED",
            title = "Test Ladder Need",
            description = "Need a 16ft ladder",
            category = "TOOLS",
            authorName = "Test User",
            status = "ACTIVE"
        )

        val result = withTimeout(5000L) { repo.createPost(post) }
        assertTrue("Expected post creation to succeed", result.isSuccess)

        // Read directly from firestore to verify
        val doc = withTimeout(5000L) {
            firestore.collection("posts").document(postId).get().await()
        }
        assertTrue(doc.exists())
        assertEquals("Test Ladder Need", doc.getString("title"))
    }

    @Test
    fun createPost_unauthenticatedUser_fails() = runBlocking {
        auth.signOut()
        val repo = FirestoreRepository(firestore)

        val post = NeighborPost(
            id = UUID.randomUUID().toString(),
            userId = "someone",
            type = "NEED",
            title = "Unauth Post",
            description = "Should fail",
            category = "TOOLS",
            authorName = "Anon",
            status = "ACTIVE"
        )

        try {
            repo.createPost(post)
            assertTrue("Expected requireUserId() to throw when unauthenticated", false)
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("User must be signed in with Google") == true)
        }
    }

    @Test
    fun createIssue_and_toggleUpvote_succeeds() = runBlocking {
        val uid = signInTestUser("issue_reporter@example.com")
        val repo = FirestoreRepository(firestore)

        val issueId = UUID.randomUUID().toString()
        val issue = CommunityIssue(
            id = issueId,
            title = "Broken lamp",
            description = "Streetlight flickers",
            locationSnippet = "Corner of 5th",
            cellId = "cell_oakwood_center",
            category = "LIGHTING",
            status = "REPORTED",
            reporterName = "Reporter",
            reporterId = uid,
            upvotesCount = 1
        )

        val result = withTimeout(5000L) { repo.createIssue(issue) }
        assertTrue(result.isSuccess)

        // Upvote from another user
        val user2Id = signInTestUser("user2@example.com")
        val upvoteResult = withTimeout(5000L) {
            repo.toggleIssueUpvote(issueId, currentUpvotes = 1, currentlyUpvoted = false)
        }
        assertTrue(upvoteResult.isSuccess)
    }

    @Test
    fun conversation_access_nonParticipant_fails() = runBlocking {
        val aliceUid = signInTestUser("alice_conv@example.com")
        val bobUid = signInTestUser("bob_conv@example.com")

        val convId = UUID.randomUUID().toString()
        // Setup conversation
        withTimeout(5000L) {
            firestore.collection("conversations").document(convId).set(
                mapOf(
                    "id" to convId,
                    "participantIds" to listOf(aliceUid, bobUid),
                    "lastMessage" to "Hey Bob",
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
        }

        // Charlie tries to read
        val charlieUid = signInTestUser("charlie_conv@example.com")
        try {
            withTimeout(5000L) {
                firestore.collection("conversations").document(convId).get().await()
            }
            assertTrue("Expected PERMISSION_DENIED for non-participant", false)
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException() ?: e as? FirebaseFirestoreException
            assertNotNull(firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }
}
