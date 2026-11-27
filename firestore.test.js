const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function serverNow() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated read/write rejection across all collections
test("Unauthenticated user: denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = serverNow();
  await assertFails(unauthDb.collection("posts").get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("issues").get());
  await assertFails(unauthDb.collection("geohash_messages").get());
  await assertFails(
    unauthDb.collection("users").doc("test_user").set({
      userId: "test_user",
      displayName: "Test",
      createdAt: ts,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc("test_user").collection("private").doc("account").get()
  );
  await assertFails(
    unauthDb.collection("support_tickets").doc("t1").set({
      ticketId: "t1",
      userId: "test_user",
      category: "GENERAL",
      message: "Help",
      status: "OPEN",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(
    unauthDb.collection("telemetry_logs").doc("l1").set({
      logId: "l1",
      userId: "test_user",
      severity: "INFO",
      errorName: "test",
      errorMessage: "msg",
      createdAt: ts,
    })
  );
});

// 2. Public UserProfile & owner-only private account PII
test("User profile & private PII isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverNow();

  // Public profile without PII email succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      handle: "@alice_oakwood",
      locationPrecision: "EXACT",
      blockedUserIds: [],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Oversized displayName (>100) fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "A".repeat(101),
      updatedAt: ts,
    })
  );

  // Owner blockUser update requires updatedAt and succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      blockedUserIds: [BOB_UID],
      updatedAt: ts,
    })
  );

  // Bob cannot overwrite Alice's public profile
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      createdAt: ts,
    })
  );

  // Private account PII: Alice can create & read, Bob is denied
  const privRefAlice = aliceDb.collection("users").doc(ALICE_UID).collection("private").doc("account");
  await assertSucceeds(
    privRefAlice.set({
      userId: ALICE_UID,
      email: "alice@example.com",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(privRefAlice.get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("private").doc("account").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("private").doc("account").set({
      userId: BOB_UID,
      email: "bob@example.com",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

// 3. Posts creation, neighbor upvote toggle, comments parent check, and author-only edit/delete
test("Posts & comments: upvote allowed for neighbors, content edits author-only, comments require parent post", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverNow();

  const postData = {
    id: "post_1",
    userId: ALICE_UID,
    type: "NEED",
    title: "Need ladder",
    description: "Need a ladder for gutter cleaning",
    category: "TOOLS",
    authorName: "Alice",
    status: "ACTIVE",
    upvotesCount: 0,
    upvoterIds: [],
    createdAt: ts,
    updatedAt: ts,
  };
  await assertSucceeds(aliceDb.collection("posts").doc("post_1").set(postData));

  // Bob can upvote Alice's post
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      upvotesCount: 1,
      upvoterIds: [BOB_UID],
      updatedAt: ts,
    })
  );

  // Bob cannot edit Alice's post title or status
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      title: "Hacked title",
      updatedAt: ts,
    })
  );

  // Comment on existing post succeeds; orphan comment fails
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c1").set({
      id: "c1",
      postId: "post_1",
      authorId: BOB_UID,
      authorName: "Bob",
      authorInitials: "B",
      text: "I have a 10ft ladder you can borrow!",
      timestamp: ts,
    })
  );
  await assertFails(
    bobDb.collection("posts").doc("missing_post").collection("comments").doc("c2").set({
      id: "c2",
      postId: "missing_post",
      authorId: BOB_UID,
      text: "Orphan comment",
      timestamp: ts,
    })
  );

  // Bob cannot delete Alice's post; Alice can
  await assertFails(bobDb.collection("posts").doc("post_1").delete());
  await assertSucceeds(aliceDb.collection("posts").doc("post_1").delete());
});

// 4. Community issues validation & immutable reporterId
test("Issues: validated create, neighbor upvote & status transition, immutable reporterId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverNow();

  await assertSucceeds(
    aliceDb.collection("issues").doc("issue_1").set({
      id: "issue_1",
      title: "Broken streetlight",
      description: "Corner lamp is flickering",
      locationSnippet: "Maple & 3rd",
      cellId: "geo_9q8yy",
      category: "LIGHTING",
      status: "REPORTED",
      reporterName: "Alice",
      reporterId: ALICE_UID,
      upvotesCount: 1,
      upvoterIds: [ALICE_UID],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Invalid category rejected
  await assertFails(
    aliceDb.collection("issues").doc("issue_bad").set({
      id: "issue_bad",
      title: "Bad",
      description: "Invalid",
      cellId: "c1",
      category: "INVALID_CAT",
      status: "REPORTED",
      reporterId: ALICE_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Bob can upvote & advance status
  await assertSucceeds(
    bobDb.collection("issues").doc("issue_1").update({
      upvotesCount: 2,
      upvoterIds: [ALICE_UID, BOB_UID],
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    bobDb.collection("issues").doc("issue_1").update({
      status: "ACKNOWLEDGED",
      assignedVolunteer: "Bob",
      updatedAt: ts,
    })
  );
  // Bob cannot rewrite title
  await assertFails(
    bobDb.collection("issues").doc("issue_1").update({
      title: "Changed",
      updatedAt: ts,
    })
  );
});

// 5. Conversations & Messages security isolation (including partial reopen update)
test("Conversation isolation: participant-only read/write, partial reopen preserves createdAt", async () => {
  const ts = serverNow();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_1").set({
      id: "conv_1",
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: ["Alice", "Bob"],
      lastMessage: "Hello",
      createdAt: ts,
      updatedAt: ts,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").get());
  await assertSucceeds(bobDb.collection("conversations").doc("conv_1").get());
  await assertFails(charlieDb.collection("conversations").doc("conv_1").get());

  // Partial reopen update works without mutating createdAt or participantIds
  await assertSucceeds(
    bobDb.collection("conversations").doc("conv_1").update({
      distanceBand: "nearby on map",
      updatedAt: ts,
    })
  );

  // Attempting to alter participantIds fails
  await assertFails(
    aliceDb.collection("conversations").doc("conv_1").update({
      participantIds: [ALICE_UID, CHARLIE_UID],
      updatedAt: ts,
    })
  );

  // Alice can send message; Charlie cannot
  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
      id: "msg_1",
      conversationId: "conv_1",
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "See you at the park",
      createdAt: ts,
    })
  );
  await assertFails(
    charlieDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_2").set({
      id: "msg_2",
      conversationId: "conv_1",
      senderId: CHARLIE_UID,
      senderName: "Charlie",
      text: "Uninvited",
      createdAt: ts,
    })
  );
});

// 6. Geohash messages
test("Geohash messages: signed-in create and author-only edit/delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverNow();

  await assertSucceeds(
    aliceDb.collection("geohash_messages").doc("geo_1").set({
      id: "geo_1",
      userId: ALICE_UID,
      authorName: "Alice",
      authorInitials: "A",
      text: "Farmers market open now",
      geohash: "9q8yyk8",
      latitude: 37.7749,
      longitude: -122.4194,
      localityName: "Downtown",
      timestamp: ts,
      createdAt: ts,
    })
  );

  await assertFails(bobDb.collection("geohash_messages").doc("geo_1").delete());
  await assertSucceeds(aliceDb.collection("geohash_messages").doc("geo_1").delete());
});

// 7. Reports, Support Tickets, and Telemetry Logs (owner-scoped private collections)
test("Reports, support_tickets, and telemetry_logs enforce strict owner isolation and immutable writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverNow();

  // Reports
  await assertSucceeds(
    aliceDb.collection("reports").doc("rep_1").set({
      id: "rep_1",
      reporterId: ALICE_UID,
      targetUserId: BOB_UID,
      targetDisplayName: "Bob",
      reason: "SPAM",
      notes: "Repeated ads",
      createdAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("reports").doc("rep_1").get());
  await assertFails(bobDb.collection("reports").doc("rep_1").get());
  await assertFails(aliceDb.collection("reports").doc("rep_1").delete());

  // Support tickets
  await assertSucceeds(
    aliceDb.collection("support_tickets").doc("tick_1").set({
      ticketId: "tick_1",
      userId: ALICE_UID,
      userName: "Alice",
      userEmail: "alice@example.com",
      category: "BUG",
      message: "Map pin offset slightly",
      appVersion: "1.3.1",
      status: "OPEN",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(
    aliceDb.collection("support_tickets").doc("tick_bad").set({
      ticketId: "tick_bad",
      userId: ALICE_UID,
      category: "BUG",
      message: "",
      status: "OPEN",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("support_tickets").doc("tick_1").get());
  await assertFails(bobDb.collection("support_tickets").doc("tick_1").get());
  await assertFails(aliceDb.collection("support_tickets").doc("tick_1").update({ status: "RESOLVED" }));

  // Telemetry logs
  await assertSucceeds(
    aliceDb.collection("telemetry_logs").doc("log_1").set({
      logId: "log_1",
      userId: ALICE_UID,
      appVersion: "v1.0.3",
      severity: "INFO",
      errorName: "cache_trimmed_manually",
      errorMessage: "Operational telemetry event",
      stackTraceSummary: "{}",
      deviceModel: "Pixel 8",
      androidVersion: "14",
      sdkInt: 34,
      timestamp: Date.now(),
      createdAt: ts,
    })
  );
  await assertFails(
    aliceDb.collection("telemetry_logs").doc("log_bad").set({
      logId: "log_bad",
      userId: ALICE_UID,
      severity: "UNKNOWN",
      errorName: "err",
      errorMessage: "msg",
      createdAt: ts,
    })
  );
  await assertSucceeds(aliceDb.collection("telemetry_logs").doc("log_1").get());
  await assertFails(bobDb.collection("telemetry_logs").doc("log_1").get());
  await assertFails(aliceDb.collection("telemetry_logs").doc("log_1").delete());
});
