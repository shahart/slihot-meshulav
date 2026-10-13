const CACHE_NAME = 'selichot-v3';
const CORE_ASSETS = [
  "./",
  "./index.html",
  "./app.js",
  "./style.css",
  "./המשך.md",
  "./התחלה.md",
  "./יום חמישי עמוד 28.md",
  "./יום חמישי של עשרת ימי תשובה עמוד 42.md",
  "./יום ראשון עמוד 24.md",
  "./יום רביעי עמוד 27.md",
  "./יום רביעי של עשרת ימי תשובה עמוד 40.md",
  "./יום שביעי עמוד 30.md",
  "./יום שישי עמוד 29.md",
  "./יום שלישי עמוד 26.md",
  "./יום שלישי של עשרת ימי תשובה עמוד 38.md",
  "./יום שני עמוד 25.md",
  "./יום שני של עשרת ימי תשובה עמוד 36.md",
  "./ערב יום כיפור עמוד 44.md",
  "./ערב ראש השנה עמוד 31.md",
  "./צום גדליה עמוד 34.md"
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(CORE_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(
        keys
          .filter((key) => key !== CACHE_NAME)
          .map((key) => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;

  const url = new URL(event.request.url);
  if (url.origin !== self.location.origin) return;

  const pathname = url.pathname;
  const isCoreAsset = CORE_ASSETS.some((asset) => {
    const assetPath = new URL(asset, self.location.origin).pathname;
    return pathname === assetPath;
  });

  if (isCoreAsset) {
    event.respondWith(
      caches.open(CACHE_NAME).then((cache) =>
        cache.match(event.request).then((cached) => {
          if (cached) {
            fetch(event.request)
              .then((res) => {
                if (res && res.status === 200) {
                  cache.put(event.request, res.clone());
                }
              })
              .catch(() => {});
            return cached;
          }
          return fetch(event.request).then((res) => {
            if (res && res.status === 200) {
              cache.put(event.request, res.clone());
            }
            return res;
          });
        })
      )
    );
    return;
  }

  event.respondWith(
    caches.open(CACHE_NAME).then((cache) =>
      fetch(event.request)
        .then((networkResponse) => {
          if (networkResponse && networkResponse.status === 200) {
            cache.put(event.request, networkResponse.clone());
          }
          return networkResponse;
        })
        .catch(() => cache.match(event.request))
    )
  );
});
