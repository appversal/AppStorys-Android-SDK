package com.appversal.appstorys.utils

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object VideoCache {
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null

    // Cache size in bytes (1 GB)
    private const val CACHE_SIZE = 1024L * 1024 * 1024

    fun getCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            simpleCache = SimpleCache(
                File(context.cacheDir, "video_cache"),
                LeastRecentlyUsedCacheEvictor(CACHE_SIZE),
                StandaloneDatabaseProvider(context)
            )
        }
        return simpleCache!!
    }

    fun getFactory(context: Context): CacheDataSource.Factory {
        if (cacheDataSourceFactory == null) {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)

            val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(getCache(context))
                .setUpstreamDataSourceFactory(defaultDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
        return cacheDataSourceFactory!!
    }

    fun releaseCache() {
        try {
            simpleCache?.release()
        } catch (e: Exception) {
            // Handle release exception
        } finally {
            simpleCache = null
            cacheDataSourceFactory = null
        }
    }
}