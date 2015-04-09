package com.himanshuvirmani.androidcache;

public interface PutCallback {
  public void onSuccess();

  public void onFailure(Exception e);
}
