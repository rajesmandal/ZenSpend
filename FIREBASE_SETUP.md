# Firebase Setup for ZenSpend Shared Cashbook

Google Sign-In and Firestore sync require manual Firebase project configuration.

## Steps

1. Create a Firebase project at https://console.firebase.google.com
2. Add Android app with package `com.android.zenspend`
3. Download `google-services.json` → place at `app/google-services.json` (gitignored)
4. Enable **Authentication → Google** sign-in provider
5. Create a **Firestore** database (production mode)
6. Deploy security rules: `firebase deploy --only firestore:rules`
7. Deploy Cloud Functions from `functions/`:
   ```bash
   cd functions && npm install && firebase deploy --only functions
   ```
8. Add SHA-1 and SHA-256 fingerprints (debug + release) in Firebase Console
9. Set Web client ID in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"YOUR_WEB_CLIENT_ID\"")
   ```
10. Register deep link intent filter (already in AndroidManifest): `zenspend://join?token=...`

## Emulator testing (debug builds)

Debug builds set `USE_FIREBASE_EMULATORS=true` and connect to:
- Auth: `10.0.2.2:9099`
- Firestore: `10.0.2.2:8080`
- Functions: `10.0.2.2:5001`

Start emulators:
```bash
firebase emulators:start --only auth,firestore,functions
```

## Important

- Do **not** commit `google-services.json` or service account keys
- Google Sign-In is **not** functional until steps 1–9 are complete
- Restoring a `.zenspend` backup does **not** auto-overwrite Firestore cloud data
