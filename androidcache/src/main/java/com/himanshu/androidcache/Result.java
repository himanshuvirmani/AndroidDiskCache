package com.himanshu.androidcache;

public class Result<T> {
  T object;
  boolean isExpired;

  public Result(T object) {
    this.object = object;
  }

  public Result(T object, boolean isExpired) {
    this.object = object;
    this.isExpired = isExpired;
  }

  public T getObject() {
    return object;
  }

  public boolean getIsExpired() {
    return isExpired;
  }

  @Override public String toString() {
    final StringBuilder result = new StringBuilder("Cache is expired : " + isExpired);
    if (object != null) result.append(" Cache Object : " + object.toString());
    return result.toString();
  }
}