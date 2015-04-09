package com.himanshuvirmani.androidcache;

import java.io.IOException;

/**
 * Created by himanshu.virmani on 08/04/15.
 */
public interface Cache {
  public String getValue(String key) throws IOException;
  public boolean contains(String key) throws IOException;
  public void setKeyValue(String key, String value) throws IOException;
  public void clearCache() throws IOException;

}
