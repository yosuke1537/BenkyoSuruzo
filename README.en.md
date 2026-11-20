# BenkyoSuruzo

A language-learning support app for Android.

It helps you do self-testing (memorization drills) by letting you tap text shown in a
Chinese learning app (or a similar language-learning app) and temporarily hide it behind a
green band, so you can try to recall it and then tap again to check the answer.

## Key Features

- **Mask mode**
  - Tap text on screen to hide it behind a green band; tap it again to reveal it.
  - You can set the hiding/revealing granularity to word-level or sentence-level.
- **Floating button "遮"**
  - Shown only while a target app is in the foreground; one tap starts mask mode.
  - You can drag it to any position, and its position is remembered on the device.
- **Circular button "显"**
  - Shown during mask mode; tap it to exit mask mode.
- **Languages excluded from masking**
  - You can exclude your native language or UI language so they are never hidden.
- **Target app selection**
  - You can choose the apps on which the floating button appears.

## How to Use

1. In Settings, add the app you study with to "勉強するぞ対象アプリ" (target apps).
2. Turn on "フローティングボタンを表示する" (show floating button) and enable the
   accessibility service.
3. Open your study app: the "遮" button appears.
4. Tap "遮" to start mask mode, then tap the text you want to hide to cover it with a green band.
5. Tap the green band again to reveal the text.
6. Tap "显" to exit mask mode.

## Notes

### The screen appears locked while the "显" button is shown
During mask mode a red circular "显" button is shown, and **the screen may appear to be
locked. This is expected behavior.**

In mask mode, to prevent accidental taps and let you focus on hiding text, taps are used
for the masking operation and scrolling is also suppressed.

To return to normal operation, **tap the "显" button to exit mask mode**.

### Other notes
- This app uses Android's **Accessibility Service** to read the on-screen text of other apps.
  You need to enable the service during initial setup.
- Because the Accessibility Service reads on-screen text, **be careful when screens that
  contain personal or confidential information are displayed**.

## Required Permissions
- Accessibility Service (reading on-screen text, drawing the overlay)
- Notifications (persistent notification during mask mode)

## Requirements
- Android 8.0 (API 26) or later

## Build
Clone the repository and build it with Android Studio (a JDK is required).

## Disclaimer
This app is provided to support personal language learning.

- This app is **not affiliated in any way** with any specific language-learning app or
  service, and it is not endorsed, recommended, or partnered with by their operators.
- The developer assumes **no liability** for any damage arising from the use of, or
  inability to use, this app (including data loss, misoperation, interrupted study, or
  disputes with third parties).
- This app is provided AS-IS, with no warranty of completeness, continuity, or fitness
  for a particular purpose.

## License
This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file
for the full text.
