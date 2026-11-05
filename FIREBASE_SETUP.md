# Firebase setup for the ZenSpend shared cashbook

Everything runs on the **free Spark plan**. There are no Cloud Functions: creating, joining and
leaving a household are done by the app with atomic Firestore batches that `firestore.rules` validates.

## One-time console steps

1. Firebase console → your project → add the Android app `com.android.zenspend`
   (add the debug SHA-1 / SHA-256, see `docs/SETUP_GOOGLE_LOGIN.md`).
2. Download `google-services.json` → `app/google-services.json` (git-ignored).
3. **Authentication → Sign-in method → Google → Enable.**
4. **Firestore Database → Create database** (production mode).
5. **Firestore → Rules** → paste the whole content of `firestore.rules` → **Publish**.
   Re-publish whenever `firestore.rules` changes.
6. Put `GOOGLE_WEB_CLIENT_ID=...` in `local.properties`.

## Data layout

```
users/{uid}                          currentHouseholdId
invitations/{sha256(token)}          householdId, householdName, expiresAt, status PENDING|USED|REVOKED
households/{hid}                     name, ownerUserId, memberLimit (2), memberCount
households/{hid}/members/{uid}       role OWNER|MEMBER, displayName
households/{hid}/transactions|wallets|categories|budgets|creditCards|emis|recurringTransactions/...
```

The invitation id is the SHA-256 of the secret token, so only the person holding the token can read it.
Joining is one batch: invitation → `USED`, member document created, `memberCount + 1`; the rules check all
three together, so an invitation works once and a household never exceeds two members.

## Testing with two accounts on one phone

1. Account A: Settings → Shared Cashbook → create household → create invitation → copy the token.
2. Sign out, sign in with account B → Shared Cashbook → paste the token → Join.
3. Back on account A: the partner appears in the member list after the next refresh/sync.

## Important

- Never commit `google-services.json`.
- Restoring a `.zenspend` backup does not overwrite Firestore data.
- Cloud Functions are intentionally not used; if one is added later it needs the Blaze plan.
