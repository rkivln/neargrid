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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

// 1. Unauthenticated read/write rejection
test("Unauthenticated user: cannot read posts or write profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").get());
  await assertFails(unauthDb.collection("users").doc("test_user").set({
    userId: "test_user",
    email: "test@example.com",
    displayName: "Test",
    createdAt: new Date(),
  }));
});

// 2. User profile ownership
test("Authenticated user: can create own profile but cannot overwrite another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      handle: "@alice_oakwood",
      createdAt: new Date(),
    })
  );

  // Bob trying to write to Alice's profile must fail
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({
      userId: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob",
      createdAt: new Date(),
    })
  );
});

// 3. Posts creation and deletion
test("Authenticated user: can create post and delete own post, but cannot delete another's post", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const postData = {
    id: "post_1",
    userId: ALICE_UID,
    type: "NEED",
    title: "Need ladder",
    description: "Need a ladder for gutter cleaning",
    category: "TOOLS",
    authorName: "Alice",
    status: "ACTIVE",
    createdAt: new Date(),
  };
  await assertSucceeds(aliceDb.collection("posts").doc("post_1").set(postData));

  // Bob cannot delete Alice's post
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("posts").doc("post_1").delete());

  // Alice can delete her own post
  await assertSucceeds(aliceDb.collection("posts").doc("post_1").delete());
});

// 4. Conversations security isolation
test("Conversation isolation: only participants can read and write messages", async () => {
  // Setup conversation between Alice and Bob
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_1").set({
      id: "conv_1",
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Hello",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext("charlie_789").firestore();

  // Alice and Bob can read conversation
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").get());
  await assertSucceeds(bobDb.collection("conversations").doc("conv_1").get());

  // Charlie is NOT a participant, read must fail
  await assertFails(charlieDb.collection("conversations").doc("conv_1").get());

  // Alice can add message to conversation
  await assertSucceeds(
    aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
      id: "msg_1",
      conversationId: "conv_1",
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "See you at the park",
      createdAt: new Date(),
    })
  );

  // Charlie cannot add message to conversation
  await assertFails(
    charlieDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_2").set({
      id: "msg_2",
      conversationId: "conv_1",
      senderId: "charlie_789",
      senderName: "Charlie",
      text: "Uninvited",
      createdAt: new Date(),
    })
  );
});
