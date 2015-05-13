package com.himanshuvirmani.androidcache;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class CacheManager {
  private final static int CACHE_RUSH_SECONDS = 60 * 2;
  private static final String TAG = CacheManager.class.getSimpleName();
  private static CacheManager ourInstance;
  private static Gson gson;
  private Cache diskCache;
  private HashMap<String, CachedObject> runtimeCache;
  private boolean isDebug;

  public static CacheManager getInstance(Cache diskCache) {
    if (ourInstance == null) {
      ourInstance = new CacheManager(diskCache);
      gson = new GsonBuilder().create();
    }

    return ourInstance;
  }

  private CacheManager(Cache diskCache) {
    if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager initiating:");
    this.diskCache = diskCache;
    runtimeCache = new HashMap<>();
  }

  public void setDebug(boolean isDebug) {
    this.isDebug = isDebug;
  }

  public boolean exists(String key) {
    boolean result = false;

    try {
      result = diskCache.contains(key);
      if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager: key exists? :" + result);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  public <T> Result<T> get(String key, Type objectClass) {
    Result<T> result = null;

    if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager: get :" + key);
    try {
      result = getObject(key, objectClass);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  private <T> Result<T> getObject(String key, Type objectClass) throws IOException {
    T result = null;
    boolean isExpired = false;

    if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager: getObject :" + key);

    CachedObject runtimeCachedObject = runtimeCache.get(key);
    if (runtimeCachedObject != null && !runtimeCachedObject.isExpired()) {
      result = (T) gson.fromJson(runtimeCachedObject.getPayload(), objectClass);
    } else if (runtimeCachedObject != null && runtimeCachedObject.isSoftExpired()) {
      result = (T) gson.fromJson(runtimeCachedObject.getPayload(), objectClass);
      isExpired = true;
    } else {
      String json = diskCache.getValue(key);
      if (json != null) {
        CachedObject cachedObject = gson.fromJson(json, CachedObject.class);
        if (!cachedObject.isExpired()) {
          runtimeCache.put(key, cachedObject);
          result = (T) gson.fromJson(cachedObject.getPayload(), objectClass);
        } else {
          if (cachedObject.isSoftExpired()) {
            result = (T) gson.fromJson(cachedObject.getPayload(), objectClass);
          }
          isExpired = true;
          // To avoid cache rushing, we insert the value back in the cache with a longer expiry
          // Presumably, whoever received this expiration result will have inserted a fresh value by now
          putAsync(key, gson.fromJson(cachedObject.getPayload(), objectClass), objectClass,
              CACHE_RUSH_SECONDS, false, new PutCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(Exception e) {

                }
              });
        }
      }
    }
    Result res = new Result<>(result, isExpired);
    if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager: getObject :<result, isExpired>: " + res.toString());
    return res;
  }

  public <T> void getAsync(String key, Type objectClass, GetCallback<T> getCallback) {
    new GetAsyncTask<T>(key, objectClass, getCallback).execute();
  }

/*  public boolean unset(String key) {
    return put(key, null, -1, false);
  }

  public void unsetAsync(String key, PutCallback putCallback) {
    putAsync(key, null, -1, false, putCallback);
  }*/

  public <T> boolean put(String key, T object, Type clazz) {
    return put(key, object, clazz, -1, false);
  }

  public <T> boolean put(String key, T object, Type clazz, int expiryTimeSeconds, boolean allowSoftExpiry) {
    boolean result = false;

    if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager Put:" + key);

    try {
      putObject(key, object, clazz, expiryTimeSeconds, allowSoftExpiry);
      result = true;
    } catch (Exception e) {
      e.printStackTrace();
      // Do nothing, return false
    }

    return result;
  }

  public <T> void putAsync(String key, T object, Type clazz, PutCallback putCallback) {
    putAsync(key, object, clazz, -1, false, putCallback);
  }

  public <T> void putAsync(String key, T object, Type clazz, int expiryTimeSeconds, boolean allowSoftExpiry,
      PutCallback putCallback) {
    new PutAsyncTask<>(key, object, clazz, expiryTimeSeconds, allowSoftExpiry, putCallback).execute();
  }

  public void clear() throws IOException {
    runtimeCache.clear();
    diskCache.clearCache();
  }

  public enum ExpiryTimes {
    ONE_SECOND(1),
    ONE_MINUTE(60),
    ONE_HOUR(60 * 60),
    ONE_DAY(60 * 60 * 24),
    ONE_WEEK(60 * 60 * 24 * 7),
    ONE_MONTH(60 * 60 * 24 * 30),
    ONE_YEAR(60 * 60 * 24 * 365);

    private final int seconds;

    ExpiryTimes(int seconds) {
      this.seconds = seconds;
    }

    public int asSeconds() {
      return seconds;
    }
  }

  @SuppressWarnings("unchecked")
  private class GetAsyncTask<T> extends AsyncTask<Void, Void, Result<T>> {
    private final String key;
    private final GetCallback callback;
    private final Type objectClass;
    private Exception e;

    private GetAsyncTask(String key, Type objectClass, GetCallback callback) {
      this.callback = callback;
      this.key = key;
      this.objectClass = objectClass;
    }

    @Override
    protected Result<T> doInBackground(Void... voids) {
      Result<T> result = null;
      try {
        result = getObject(key, objectClass);
      } catch (IOException e1) {
        this.e = e1;
      }

      return result;
    }

    @Override
    protected void onPostExecute(Result<T> object) {
      if (callback != null) {
        if (e == null) {
          callback.onSuccess(object);
        } else {
          callback.onFailure(e);
        }
      }
    }
  }

  private class PutAsyncTask<T> extends AsyncTask<Void, Void, Void> {
    private final PutCallback callback;
    private final String key;
    private final T payload;
    private final int expiryTimeSeconds;
    private final boolean allowSoftExpiry;
    private Exception e;
    private final Type clazz;

    private PutAsyncTask(String key, T payload,Type clazz, int expiryTimeSeconds, boolean allowSoftExpiry,
        PutCallback callback) {
      this.key = key;
      this.callback = callback;
      this.payload = payload;
      this.expiryTimeSeconds = expiryTimeSeconds;
      this.allowSoftExpiry = allowSoftExpiry;
      this.clazz = clazz;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        putObject(key, payload, clazz, expiryTimeSeconds, allowSoftExpiry);
      } catch (Exception e) {
        this.e = e;
      }

      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (callback != null) {
        if (e == null) {
          callback.onSuccess();
        } else {
          callback.onFailure(e);
        }
      }
    }
  }

  private <T> void putObject(String key, T payload, Type clazz, int expiryTimeSeconds, boolean allowSoftExpiry)
      throws Exception {
    if(BuildConfig.DEBUG || isDebug) Log.d(TAG, "CacheManager putObject:" + key + ":" + payload.toString());

    String payloadJson = gson.toJson(payload, clazz);
    CachedObject cachedObject = new CachedObject(payloadJson, expiryTimeSeconds, allowSoftExpiry);
    String json = gson.toJson(cachedObject);
    runtimeCache.put(key, cachedObject);
    diskCache.setKeyValue(key, json);
    diskCache.setKeyValue(key, json);
  }
}
