package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.util.Log;

import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Leslie on 2016-08-23.
 *
 * Intent Service to get Stock History from Yahoo API and put into database
 * Using OkHttp for API call
 *
 * Credit for Sending/Receiving Broadcast status from IntentService:
 * http://stacktips.com/tutorials/android/creating-a-background-service-in-android
 */
public class StockHistoryIntentService extends IntentService{
    public final static String LOG_TAG = StockHistoryIntentService.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();


    public StockHistoryIntentService() {
        super("StockHistoryIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Stock Intent Service");


        //Build Yahoo API query URL for stock history
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22YHOO%22%20and%20startDate%20%3D%20%22");

        urlStringBuilder.append("2015-09-11");//replace with coded start date

        urlStringBuilder.append("%22%20and%20endDate%20%3D%20%22");

        urlStringBuilder.append("2016-08-23"); //replace with coded end date

        urlStringBuilder.append("%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");

        String urlString;
        String getResponse = null; //JSON from Yahoo HTTP
        if (urlStringBuilder != null) { //do api call and get data back
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
               // Log.v(LOG_TAG, "LJG The JSON for history is " + getResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //Now get the JSON from the fetchData string
        String tester = getResponse;

        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(getResponse);
            Log.v(LOG_TAG, "LJG JSON StockHistory is OK");
            /*if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject(context.getString(R.string.json_query));
                int count = Integer.parseInt(jsonObject.getString(context.getString(R.string.json_count)));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject(context.getString(R.string.json_results))
                            .getJSONObject(context.getString(R.string.json_quote));
                    batchOperations.add(buildBatchOperation(jsonObject, context)); //add result to
                } else {
                    resultsArray = jsonObject.getJSONObject(context.getString(R.string.json_results))
                            .getJSONArray(context.getString(R.string.json_quote));

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject, context));
                        }
                    }
                }*/

        } catch (JSONException e) {
            Log.e(LOG_TAG, "LJG Stock History String to JSON failed: " + e);
        }





        //Broadcast a String of history answers
        ArrayList<String> historyAnswers = new ArrayList<>();
        historyAnswers.add("One");
        historyAnswers.add("Two");
        historyAnswers.add("Three");







        //Broadcast that results are in
        Utils.sendHistoryBroadcastForUpdate(getApplicationContext());



    }


    /**
     * Using OkHttp to get data from HTTP
     *
     * @param url to do API call
     * @return API call string
     * @throws IOException
     */
    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


}
