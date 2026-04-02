const TILE_CACHE_PREFIX = 'tianditu-tiles-'
const TILE_CACHE_VERSION = 'v2'
const TILE_CACHE_NAME = `${TILE_CACHE_PREFIX}${TILE_CACHE_VERSION}`
const TILE_CACHE_MAX_ENTRIES = 2400
const TILE_TYPES = new Set(['img_w', 'cia_w'])
const TILE_CACHEABLE_IMAGE_TYPES = ['image/', 'application/octet-stream']
const ongoingTileFetches = new Map()

const isTiandituTileRequest = (request) => {
  try {
    const url = new URL(request.url)
    if (url.hostname.indexOf('tianditu.gov.cn') === -1) {
      return false
    }
    if (!url.pathname.includes('/DataServer')) {
      return false
    }
    const tileType = (url.searchParams.get('T') || '').toLowerCase()
    return TILE_TYPES.has(tileType)
  } catch (_error) {
    return false
  }
}

const createCanonicalTileCacheKey = (request) => {
  const url = new URL(request.url)
  const tileType = (url.searchParams.get('T') || '').toLowerCase()
  const x = url.searchParams.get('x') || ''
  const y = url.searchParams.get('y') || ''
  const level = url.searchParams.get('l') || url.searchParams.get('z') || ''
  return `https://t.tianditu.gov.cn/DataServer?T=${tileType}&x=${x}&y=${y}&l=${level}`
}

const canBeCached = (response) => {
  if (!response) {
    return false
  }
  if (response.type === 'opaque') {
    // 跨域不透明响应无法读取状态码，保守允许缓存，后续由容量控制清理
    return true
  }
  if (!response.ok) {
    return false
  }
  const contentType = (response.headers.get('content-type') || '').toLowerCase()
  if (!contentType) {
    return true
  }
  return TILE_CACHEABLE_IMAGE_TYPES.some((typePrefix) => contentType.includes(typePrefix))
}

const trimTileCache = async (cache) => {
  const keys = await cache.keys()
  if (keys.length <= TILE_CACHE_MAX_ENTRIES) {
    return
  }
  const deleteCount = keys.length - TILE_CACHE_MAX_ENTRIES
  for (let i = 0; i < deleteCount; i += 1) {
    await cache.delete(keys[i])
  }
}

const updateTileCache = async (cache, request, cacheKey) => {
  try {
    const response = await fetch(request)
    if (!canBeCached(response)) {
      return
    }
    await cache.put(cacheKey, response.clone())
    await trimTileCache(cache)
  } catch (_error) {
    // 网络失败时静默，避免影响当前展示
  }
}

const fetchAndCacheTile = async (cache, request, cacheKey) => {
  const pending = ongoingTileFetches.get(cacheKey)
  if (pending) {
    return pending
  }
  const task = (async () => {
    const networkResponse = await fetch(request)
    if (canBeCached(networkResponse)) {
      await cache.put(cacheKey, networkResponse.clone())
      await trimTileCache(cache)
    }
    return networkResponse
  })()
    .catch((error) => {
      throw error
    })
    .finally(() => {
      ongoingTileFetches.delete(cacheKey)
    })
  ongoingTileFetches.set(cacheKey, task)
  return task
}

self.addEventListener('install', (event) => {
  event.waitUntil(self.skipWaiting())
})

self.addEventListener('activate', (event) => {
  event.waitUntil(
    (async () => {
      const cacheNames = await caches.keys()
      await Promise.all(
        cacheNames
          .filter((name) => name.startsWith(TILE_CACHE_PREFIX) && name !== TILE_CACHE_NAME)
          .map((name) => caches.delete(name))
      )
      await self.clients.claim()
    })()
  )
})

self.addEventListener('fetch', (event) => {
  const request = event.request
  if (request.method !== 'GET' || !isTiandituTileRequest(request)) {
    return
  }

  event.respondWith(
    (async () => {
      const cache = await caches.open(TILE_CACHE_NAME)
      const cacheKey = createCanonicalTileCacheKey(request)
      const cachedResponse = await cache.match(cacheKey)
      if (cachedResponse) {
        // 命中后触发后台刷新，并刷新缓存顺序，提升热点瓦片留存概率
        event.waitUntil(
          (async () => {
            await cache.put(cacheKey, cachedResponse.clone())
            await updateTileCache(cache, request, cacheKey)
          })()
        )
        return cachedResponse
      }

      try {
        const networkResponse = await fetchAndCacheTile(cache, request, cacheKey)
        return networkResponse
      } catch (_error) {
        return cachedResponse || Response.error()
      }
    })()
  )
})
