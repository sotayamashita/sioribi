# How to Test on Your Own Device via Google Play

This guide explains how to install a build on your own device through Google Play
without a public release. It is written to pair with `docs/how-to-sign-release.md`.

## When to use which option

- **Internal testing (closest to TestFlight)**: fastest Play distribution for up to
  100 testers, no review wait, install via Play Store.
- **Internal app sharing (fastest for yourself)**: upload an APK/AAB and get a
  shareable link; great for quick personal installs.
- **Closed testing**: larger private groups or staged testing beyond 100 users.

## Prerequisites

- A Google Play Developer account and a Play Console app created.
  (You must create the app in Play Console before you can use any Play testing tracks
  or internal app sharing.)
- A signed release build (see `@docs/how-to-sign-release.md`).

## Option A: Internal testing (TestFlight-like)

1. In Play Console, go to **Testing > Internal testing**.
2. Create or open the internal testing track.
3. Add testers (your own Google account email is enough).
4. Create a release and upload your **AAB**.
5. Save and review, then **publish** the internal test release.
6. Copy the **opt-in link** and open it on your device.
7. Install the app from the Play Store page that opens.

Notes:
- Internal testing supports up to **100 testers** per app.
- Builds are often available within minutes/seconds, but the first publish can
  take a few hours for the opt-in link to become active.
- Paid apps are free for internal testing users.

## Option B: Internal app sharing (quickest for yourself)

1. In Play Console, enable **Internal app sharing** (one-time setup).
2. Upload an **AAB or APK**.
3. Copy the generated **shareable link**.
4. Open the link on your device and install.

Notes:
- You can upload **debuggable** artifacts.
- Version codes **do not need to be unique**.
- Access can be restricted by email allow list or left open.

## Option C: Closed testing (larger private groups)

1. Go to **Testing > Closed testing** and open the default track or create a new one.
2. Add testers by email or Google Groups.
3. Create a release and upload your **AAB**.
4. Publish, then share the opt-in link with testers.

## Troubleshooting tips

- If the Play Store says your test is unavailable, confirm:
  - The tester account is in the allowed list.
  - The tester accepted the opt-in link.
  - The app has a release published to the chosen track.

## References

- Google Play Console: Set up open, closed, and internal testing
  https://support.google.com/googleplay/android-developer/answer/9845334
- Internal app sharing
  https://play.google.com/console/about/internalappsharing
- Android App Bundle overview
  https://developer.android.com/guide/app-bundle
