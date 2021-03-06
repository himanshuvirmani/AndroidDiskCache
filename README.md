# Android Disk Cache

## Introduction

Cache plays an important role in android as there is a crunch of memory and consistent network connectivity on mobile device. Hitting server again and again for the rarely changing data also takes a toll on server resources. No one likes to see loading icon till the data is being fetched from server. AndroidDiskCache library is there to make your job easy when working towards resolving above mentioned use cases.

Android disk cache is an LRU based Disk Cache which persists your android pojo/data objects in a key value format in your phone's internal memory. It uses the outstanding [DiskLruCache](https://github.com/JakeWharton/DiskLruCache) library) of JSON representations of your Objects (using the superb [GSON](https://code.google.com/p/google-gson/) library) and an in-memory, runtime cache of your Objects. You can optionally specify a time when those cache entries expire, and the goodness of cache-rush-mitigation is baked right into the crust.

Original credit for the base of this project goes out to [iainconnor/ObjectCache](https://github.com/iainconnor/ObjectCache). This project did most things right but did not use enough Java Generics standard to make it easy to use and integrate with your project. Also added is the debug logs to help you test and identify if you are going in the right direction and are getting the desired results from cache.

## Installing in Gradle

1. Add the repository to your `build.gradle` file;

	``` groovy
	repositories {
		mavenCentral()
	}
	```
2. And add the dependency;

	``` groovy
	dependencies {
		compile 'com.himanshuvirmani:androidcache:1.0.1'
	}
	```

## Installing in other build tools

1. Download the `.jar` for the latest version [from this repository](https://search.maven.org/remote_content?g=com.himanshuvirmani&a=androidcache&v=LATEST).
2. Add it to your project.
3. If you're building for Android, beg your boss to give you the time to switch to Gradle.

## Usage

First, you'll need to create an instance of global cache manager instance in your application class `DiskCache ( File cacheDirectory, int appVersion, int cacheSizeKb )`. For an Android application, this is simple;

``` java
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
```

You can get the `CacheManager` singleton instance by creating a getter in Application class;

``` java
CacheManager cacheManager = MainApplication.getCacheManager();
```

Insert an Object to be cached;

``` java
MyObject myObject = new MyObject("foo");
cacheManager.put("myKey", myObject);

MyExpiryObject myExpiryObject = new MyExpiryObject("bar");
cacheManager.put("myKeyExpiry", myExpiryObject, CacheManager.ExpiryTimes.ONE_WEEK.asSeconds());
```

And retrieve it;

``` java
MyObject myObject = cacheManager.get("myKey", MyObject.class);
if ( myObject != null ) {
	// Object was found!
} else {
	// Object was not found, or was expired.
	// You should re-generate it and trigger a `.put()`.
}
```

If you're on Android, these operations can be run off the main thread;

``` java
cacheManager.putAsync("myKeyExpiry", myExpiryObject, CacheManager.ExpiryTimes.ONE_WEEK.asSeconds(), new PutCallback() {
    @Override
    public void onSuccess () {

    }

    @Override
    public void onFailure ( Exception e ) {

    }
});

cacheManager.getAsync("myKeyExpiry", ExpiryObject.class, new GetCallback<ExpiryObject>() {
    @Override
    public void onSuccess ( Result<ExpiryObject> myObject ) {
	if ( myObject.getCachedObject() != null ) {
        	// Object was found!
        } else {
        	if ( myObject.isExpired()) {
        	    // Object is expired
        	} else {
        	    // Object was never added to cache.
        	}
        }
    }

    @Override
    public void onFailure ( Exception e ) {

    }
});
```

If you want to clear the cache manually, you can use;

``` java
diskCache.clearCache();
```

## ToDo
To add in-memory cache option for cases where you do not want to maintain data in disk.

## Contact

Would love to hear from you for any suggestions and extensions to this. [himanshuvirmani@gmail.com](mailto:himanshuvirmani@gmail.com). 
