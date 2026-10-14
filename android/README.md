# סדר סליחות — Android

This is an offline-first native Android reader for the combined Selichot
service. All Markdown source files from `../docs` are copied to
`app/src/main/assets` and are packaged in the APK, so displaying the service
never requires network access.

When a connection is available the app also refreshes content from the deployed
site (`https://shahart.github.io/slihot-meshulav/`) in the background and caches
it under the app's files directory (stale-while-revalidate). Refreshes are
conditional requests: the stored `ETag`/`Last-Modified` are sent back and an
unchanged file answers `304 Not Modified`, so no body is re-downloaded. Bundled
or cached content is shown immediately, so the reader keeps working fully
offline.

Build an installable debug APK with:

```sh
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Choosing a service date

Open תוכן, choose בחירת תאריך לסליחות, and select a Gregorian date. The
selection is optional and lasts until you choose חזרה לסדר של היום or restart
the app.
