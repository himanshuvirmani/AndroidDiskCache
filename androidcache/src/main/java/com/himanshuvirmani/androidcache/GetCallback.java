package com.himanshuvirmani.androidcache;

public interface GetCallback<T> {
  public void onSuccess(Result<T> object);

  public void onFailure(Exception e);
}
