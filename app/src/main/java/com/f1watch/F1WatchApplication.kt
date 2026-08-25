package com.f1watch

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.f1watch.worker.F1SyncWorker

class F1WatchApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Schedule background periodic sync for F1 schedule and widgets
        F1SyncWorker.schedulePeriodicSync(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB cache
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}
