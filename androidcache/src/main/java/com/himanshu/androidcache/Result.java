package com.himanshu.androidcache;

public class Result<T> {
  T cacheObject;
  boolean expired;

  public Result(T object) {
    this.cacheObject = object;
  }

  public Result(T object, boolean isExpired) {
    this.cacheObject = object;
    this.expired = isExpired;
  }

  public T getCachedObject() {
    return cacheObject;
  }

  public boolean isExpired() {
    return expired;
  }

  @Override public String toString() {
    final StringBuilder result = new StringBuilder("Cache is expired : " + expired);
    if (cacheObject != null) result.append(" Cache Object : " + cacheObject.toString());
    return result.toString();
  }
}