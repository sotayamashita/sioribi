# Release Signing (Android)

This document explains how to generate signed release APK/AAB for this project.
Release distribution (especially publishing to Google Play) requires signing.

## Options by goal

- **Test on a local device**: build a signed APK with `assembleRelease`
- **Publish to Google Play**: typically use Play App Signing (create an upload key and sign uploads)

## Method A: Sign with Android Studio

Use **Build > Generate Signed App Bundle or APK** from the Android Studio menu.
The wizard can also create a signing key for you.

### What to enter in the wizard

1. **Select artifact type**
   - Choose **Android App Bundle** (recommended for Play) or **APK** (easier for local install).
2. **Create or choose a keystore**
   - If this is your first time, click **Create new...** under **Key store path**.
3. **New Key Store fields (when creating a new keystore)**
   - **Key store path (required)**: select the location where the keystore should be created,
     and add a filename ending in `.jks`. The official guide does not prescribe a specific
     path, but it explicitly says to keep the keystore in a safe, secure place.
   - **Password / Confirm (required)**: keystore password.
   - **Alias (required)**: key name (example: `release`).
   - **Key password / Confirm (required)**: key password (recommended to be the same as the keystore password).
   - **Validity (years) (required)**: at least **25** years. For Google Play, the key's validity
     must end **after 2033/10/22**.
   - **Certificate (required)**: enter name/organization/location details; these are stored in the certificate.
     For example, the country code for Japan is `JP` (ISO 3166-1 alpha-2).
4. **Sign with your key**
   - Select the **Module**, then fill:
     **Key store path**, **Key store password**, **Key alias**, **Key password**.
   - Click **Next**, then set:
     **Destination folder**, **Build type**, and **Product flavor(s)** if applicable.
   - The **Destination folder** controls where the output is written. If it is set to
     `app/`, the artifact will be placed under `app/release/` or `app/debug/`.
     Recommended: use the Gradle default output base, e.g. set it to
     `app/build/outputs/` so artifacts end up alongside `./gradlew assembleRelease`
     outputs.
   - If you are generating an APK, select the **Signature Versions** to support.
   - Click **Create** to build the signed artifact.

## Method B: Sign with the command line + Gradle

### 1. Create a keystore (first time only)

Create a keystore using the JDK `keytool`.
For Google Play, the signing key should be valid for at least **25** years,
and must end **after 2033/10/22**.

```bash
keytool -genkey -v \
  -keystore my-release-key.jks \
  -alias my-alias \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

- Store `my-release-key.jks` in a secure place.
- You will set the password/alias in Gradle.

### 2. Separate secrets into a file

Create `keystore.properties` at the repo root and put secrets there.
(**Do not** commit it to git.)

```properties
storeFile=my-release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=my-alias
keyPassword=YOUR_KEY_PASSWORD
```

```diff
# .gitignore
+ keystore.properties
```

#### Optional: Fetch the keystore file from 1Password (op CLI)

If you store the `.jks` as a file attachment in 1Password, you can download it
with `op` and write it to disk before building:

```bash
op read "op://<Vault>/<Item>/<file-name>.jks?attribute=content" > my-release-key.jks
```

- Replace `<Vault>`, `<Item>`, and `<file-name>.jks` with your values.
- This bypasses fnox and uses the 1Password CLI directly.

### 3. Add signing config to `app/build.gradle.kts`

Add `signingConfigs` and apply it to `release`.

```kotlin
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

> Builds will fail if `keystore.properties` is missing, so in CI you may want
> to load these values from environment variables instead.

### 4. Build a release APK

```bash
./gradlew assembleRelease
```

Output directory (where the APK is produced):

```
app/build/outputs/apk/release/
```

The filename can vary by environment and Gradle config, so confirm with:

```bash
ls app/build/outputs/apk/release/
```

### 5. Verify the signature (optional)

```bash
apksigner verify --verbose --print-certs <apk-path>
```

`apksigner` is included in Android SDK Build-Tools.

### 6. Install on a device

```bash
adb install -r <apk-path>
```

#### USB vs Wireless ADB

- **USB (recommended for first time)**: most reliable and required for initial setup.
- **Wireless ADB**: available after pairing a device on the same network.

USB steps (high level):
- Enable **Developer options** and **USB debugging** on the device.
- Connect via USB and approve the debugging prompt.

Wireless steps (high level):
- Pair the device once via **Developer options > Wireless debugging**.
- Then connect from the PC using the pairing/connection code shown on the device.

## Troubleshooting

### INSTALL_FAILED_UPDATE_INCOMPATIBLE

Error:

```
INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package com.example.sioribi signatures do not match newer version
```

Cause:
- The APK you are installing is signed with a different key than the one already
  installed on the device (e.g., debug vs release).

Fix:
- Uninstall the existing app, then reinstall the new APK:

```bash
adb uninstall com.example.sioribi
adb install -r app/build/outputs/apk/release/app-release.apk
```

### `adb devices` shows no devices

Cause:
- The device is not detected by ADB (USB debugging not enabled, authorization not accepted,
  or cable/USB mode issues).

Fix:
- On the device: enable **Developer options** and **USB debugging**.
- When prompted, accept **Allow USB debugging**.
- Ensure the USB mode is **File transfer (MTP)**, not charging-only.
- Try a different USB cable/port (some cables are charge-only).
- Restart ADB and retry:

```bash
adb kill-server
adb start-server
adb devices
```

## Notes for publishing to Google Play

- New apps are recommended to use **Play App Signing**.
- In that case, the key above is treated as an **upload key**, and
  **Google manages the app signing key**.

To build an AAB:

```bash
./gradlew bundleRelease
```

Output directory:

```
app/build/outputs/bundle/release/
```
