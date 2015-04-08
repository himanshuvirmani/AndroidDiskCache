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
}
