# Laundromatic Android

Native Android rewrite of the original Laundromatic web app.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Firebase Auth + Firestore

## Backend compatibility

- Uses same Firebase project as the web app.
- Uses same Firestore schema: each signed-in user reads and writes `${uid}_items`.
- Supports existing Firebase email/password accounts.

Firebase is initialized manually in `app/src/main/java/com/julianjelfs/laundromatic/LaundromaticFirebase.kt`, so local builds do **not** need `google-services.json`.

The Firebase API key is not in the repo. Add it to `local.properties` (gitignored) before building:

```properties
firebase.apiKey=<web app API key from Firebase console>
```

Or set `LAUNDROMATIC_FIREBASE_API_KEY` in the environment. The build fails if neither is set.

## Build APKs

Debug APK:

```sh
./gradlew assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install on device:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Signed release APK

Set env vars:

```sh
export LAUNDROMATIC_KEYSTORE_FILE=/absolute/path/to/keystore.jks
export LAUNDROMATIC_KEYSTORE_PASSWORD=...
export LAUNDROMATIC_KEY_ALIAS=...
export LAUNDROMATIC_KEY_PASSWORD=...
```

Then build:

```sh
./gradlew assembleRelease
```

Output:

```text
app/build/outputs/apk/release/app-release.apk
```

If signing env vars are missing, Gradle still builds release variant, but it will not be device-installable until signed.

## Product scope in this version

- Email/password sign-in
- List items by urgency
- Add new item
- Mark item washed
- Pause / resume item
- Pause / resume all items
- Delete item
- Manual refresh
- Firestore offline cache
