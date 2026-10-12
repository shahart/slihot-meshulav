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

## Choosing a service date

Open תוכן, choose בחירת תאריך לסליחות, and select a Gregorian date. The
selection is optional and lasts until you choose חזרה לסדר של היום or restart
the app.
