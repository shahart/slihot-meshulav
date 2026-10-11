# סדר סליחות — Android

This is an offline native Android reader for the combined Selichot service.
All Markdown source files from `../docs` are copied to `app/src/main/assets`
and are packaged in the APK, so displaying the service never requires network
access.

Build an installable debug APK with:

```sh
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Debugging a service date

The app accepts the same `SLIHOT_DEBUG_DATE` concept as the web reader. Launch
it with either an ISO-8601 instant or a `YYYY-MM-DD` date:

```sh
/Users/shahart/Library/Android/sdk/platform-tools/adb shell am start -W \
  -n org.malegilboa.slihot/.MainActivity \
  --es SLIHOT_DEBUG_DATE "2026-09-05"
```

End users can open a selected service date through this link format:

```text
slihot://service?SLIHOT_DEBUG_DATE=2026-09-05
```

When both are supplied, the link's query parameter takes precedence over the
ADB intent extra.
