package com.himanshuvirmani.androidcachesample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.himanshuvirmani.androidcache.CacheManager;
import com.himanshuvirmani.androidcache.GetCallback;
import com.himanshuvirmani.androidcache.Result;

public class MainActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setDummyDataToCache();
    fetchDataFromCache();
  }


  private void setDummyDataToCache() {
    CacheManager cacheManager = MainApplication.getCacheManager();
    // we can also dp a put async here
    cacheManager.put("dummy","Hello World!",CacheManager.ExpiryTimes.ONE_HOUR.asSeconds(),true);
    Toast.makeText(this,"Storing data to cache : - " + "Hello World!", Toast.LENGTH_LONG).show();
  }

  private void fetchDataFromCache() {
    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        // you can also do a direct get here instead of async get
        // if you are already doing an operation in background thread
        // or you want to handle stuff synchronously.
        MainApplication.getCacheManager().getAsync("dummy",String.class, new GetCallback<String>() {
          @Override public void onSuccess(Result<String> object) {
            showToast(object.getCachedObject());
          }

          @Override public void onFailure(Exception e) {

          }
        });
      }
    }, 5000); // to add a little delay
  }

  private void showToast(String cachedObject) {
    Toast.makeText(this,"Received data from cache : - " + cachedObject, Toast.LENGTH_LONG).show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
