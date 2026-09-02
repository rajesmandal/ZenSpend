const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

function sha256(input) {
  const crypto = require("crypto");
  return crypto.createHash("sha256").update(input).digest("hex");
}

exports.joinHouseholdWithToken = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }
  const token = data.token;
  if (!token) {
    throw new functions.https.HttpsError("invalid-argument", "Token required");
  }
  const tokenHash = sha256(token);
  const now = Date.now();

  const invitations = await db.collectionGroup("invitations")
    .where("tokenHash", "==", tokenHash)
    .where("status", "==", "PENDING")
    .limit(1)
    .get();

  if (invitations.empty) {
    throw new functions.https.HttpsError("not-found", "Invalid or used invitation");
  }

  const invitationDoc = invitations.docs[0];
  const invitation = invitationDoc.data();
  if (invitation.expiresAt < now) {
    await invitationDoc.ref.update({ status: "EXPIRED" });
    throw new functions.https.HttpsError("deadline-exceeded", "Invitation expired");
  }

  const householdId = invitation.householdId;
  const householdRef = db.collection("households").doc(householdId);
  const householdSnap = await householdRef.get();
  if (!householdSnap.exists) {
    throw new functions.https.HttpsError("not-found", "Household not found");
  }
  const household = householdSnap.data();

  const membersSnap = await householdRef.collection("members")
    .where("status", "==", "ACTIVE").get();
  if (membersSnap.size >= (household.memberLimit || 2)) {
    throw new functions.https.HttpsError("resource-exhausted", "Household is full");
  }

  const uid = context.auth.uid;
  const batch = db.batch();
  batch.set(householdRef.collection("members").doc(uid), {
    householdId,
    userId: uid,
    role: "MEMBER",
    displayName: context.auth.token.name || "Member",
    joinedAt: admin.firestore.FieldValue.serverTimestamp(),
    status: "ACTIVE",
  });
  batch.update(db.collection("users").doc(uid), {
    currentHouseholdId: householdId,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  batch.update(invitationDoc.ref, { status: "USED" });
  await batch.commit();

  return {
    householdId,
    name: household.name,
    ownerUserId: household.ownerUserId,
  };
});

exports.leaveHousehold = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }
  const householdId = data.householdId;
  const uid = context.auth.uid;
  const memberRef = db.collection("households").doc(householdId).collection("members").doc(uid);
  const memberSnap = await memberRef.get();
  if (!memberSnap.exists) {
    throw new functions.https.HttpsError("not-found", "Not a member");
  }
  if (memberSnap.data().role === "OWNER") {
    throw new functions.https.HttpsError("failed-precondition", "Owner cannot leave without transfer");
  }
  const batch = db.batch();
  batch.update(memberRef, { status: "REMOVED" });
  batch.update(db.collection("users").doc(uid), {
    currentHouseholdId: null,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  await batch.commit();
  return { success: true };
});

exports.removeMember = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }
  const { householdId, memberUserId } = data;
  const ownerRef = db.collection("households").doc(householdId).collection("members").doc(context.auth.uid);
  const ownerSnap = await ownerRef.get();
  if (!ownerSnap.exists || ownerSnap.data().role !== "OWNER") {
    throw new functions.https.HttpsError("permission-denied", "Owner only");
  }
  await db.collection("households").doc(householdId).collection("members").doc(memberUserId)
    .update({ status: "REMOVED" });
  await db.collection("users").doc(memberUserId).update({
    currentHouseholdId: null,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  return { success: true };
});
