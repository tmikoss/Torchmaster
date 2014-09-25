package com.tmikoss.torchmaster;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class JSONClient {
  private static final String                  url                 = "http://raspberrypi.local.net:3000/api";

  private static final JsonHttpResponseHandler nullResponseHandler = new JsonHttpResponseHandler() {
                                                                     @Override
                                                                     public void onSuccess(int statusCode, Header[] headers, JSONObject json) {}

                                                                     @Override
                                                                     public void onSuccess(int statusCode, Header[] headers,
                                                                         JSONArray timeline) {}
                                                                   };

  private static AsyncHttpClient               client              = new AsyncHttpClient();

  public static void get(JsonHttpResponseHandler responseHandler) {
    client.get(url, null, responseHandler);
  }

  public static void post(RequestParams params) {
    client.post(url, params, nullResponseHandler);
  }

  public static void post(RequestParams params, JsonHttpResponseHandler responseHandler) {
    client.post(url, params, responseHandler);
  }
}
