# Google login setup

The sign-in code is already in the app (Credential Manager → Firebase Auth → Firestore sync).
It only needs two pieces of configuration, both kept out of git.

## 1. Firebase project
1. Open <https://console.firebase.google.com> → create (or pick) a project.
2. **Add app → Android**, package name `com.android.zenspend`.
3. Add your **SHA-1** (and SHA-256) fingerprints. Debug key:
   `./gradlew signingReport` (use the `debug` variant), or
   `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android`.
   Add the release key's SHA-1 too before publishing.
4. Download **`google-services.json`** and put it at:

   ```
   ZenSpend/app/google-services.json
   ```

   The build detects the file automatically (`FIREBASE_CONFIGURED`).

## 2. Enable Google as a sign-in method
Firebase console → **Authentication → Sign-in method → Google → Enable** → save.
Also create the **Firestore Database** (Build → Firestore) if you want cloud sync.

## 3. Web client ID
Firebase console → Authentication → Sign-in method → Google → **Web SDK configuration**
→ copy the *Web client ID* (ends with `.apps.googleusercontent.com`). Or from
`google-services.json` → the `client` entry with `"client_type": 3`.

Put it in `ZenSpend/local.properties` (already git-ignored):

```
GOOGLE_WEB_CLIENT_ID=1234567890-abcdefg.apps.googleusercontent.com
```

## 4. Build and test
Rebuild the app; the Google button on the login screen should now work on a device with a Google account.

Debug builds talk to the **real** Firebase project. To use the local Firebase emulators instead, add
`USE_FIREBASE_EMULATORS=true` to `local.properties`.
