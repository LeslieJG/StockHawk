package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by Leslie on 2016-08-23.
 *
 * Intent Service to get Stock History from Yahoo API and put into database
 * Using OkHttp for API call
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

        urlStringBuilder.append("%22&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");

        String urlString;
        String getResponse; //JSON from Yahoo HTTP
        if (urlStringBuilder != null) { //do api call and get data back
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                Log.v(LOG_TAG, "LJG The JSON for history is " + getResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }





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
