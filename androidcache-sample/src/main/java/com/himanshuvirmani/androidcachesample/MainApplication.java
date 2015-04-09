package com.himanshuvirmani.androidcachesample;

import android.app.Application;
import com.himanshuvirmani.androidcache.Cache;
import com.himanshuvirmani.androidcache.CacheManager;
import com.himanshuvirmani.androidcache.DiskCache;
import java.io.File;

public class MainApplication extends Application {

  private static CacheManager cacheManager;
  private static final int CACHE_SIZE = 1024 * 1024 * 10; //10MB

  @Override public void onCreate() {
    prepareCache();
    super.onCreate();
  }

  private void prepareCache() {
    final String cachePath = getCacheDir().getPath();
    final File cacheFile = new File(cachePath + File.separator + BuildConfig.APPLICATION_ID);
    try {
      Cache diskCache = new DiskCache(cacheFile, BuildConfig.VERSION_CODE, CACHE_SIZE);
      cacheManager = CacheManager.getInstance(diskCache);
      cacheManager.setDebug(true); //Do this if you want to see logs from cachemanager
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static CacheManager getCacheManager() {
    return cacheManager;
  }

}