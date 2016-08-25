package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
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
 * <p>
 * Intent Service to get Stock History from Yahoo API and put into database
 * Using OkHttp for API call
 * <p>
 * Credit for Sending/Receiving Broadcast status from IntentService:
 * http://stacktips.com/tutorials/android/creating-a-background-service-in-android
 */
public class StockHistoryIntentService extends IntentService {
    public final static String LOG_TAG = StockHistoryIntentService.class.getSimpleName();
    private OkHttpClient client = new OkHttpClient();


    public StockHistoryIntentService() {
        super("StockHistoryIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Stock Intent Service");


        //Check the database and see if I have any stock close info for this stock
        //if yes, just chose the next day AFTer I have (no need to consult database afterwards)
        //download all dates from the day AFTER I have in DB until today's date
        //THen put all new dates into database


        //Build Yahoo API query URL for stock history
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22");
        urlStringBuilder.append("YHOO"); //replace this with String of Stock Symbol
        urlStringBuilder.append("%22%20and%20startDate%20%3D%20%22");
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

        //TODO Delete below line soon
        this.getContentResolver().delete(QuoteProvider.Histories.CONTENT_URI, null, null); //delete the database
        Log.v(LOG_TAG, "LJG Delete the database");

        try {
            jsonObject = new JSONObject(getResponse);
            Log.v(LOG_TAG, "LJG JSON StockHistory is OK");
            if (jsonObject != null && jsonObject.length() != 0) {
                JSONObject queryJsonObject = jsonObject.getJSONObject(getString(R.string.json_query));

                int count = Integer.parseInt(queryJsonObject.getString(getString(R.string.json_count)));
                Log.v(LOG_TAG, "LJG JSON StockHistory count is " + count);

                if (count > 0) { //if there is a count - i.e. if their is historical data
                    Log.v(LOG_TAG, "LJG JSON Stock history count is 1 or more");

                    JSONObject resultsJsonObject = queryJsonObject.getJSONObject(getString(R.string.json_results));
                    JSONArray quoteArray = resultsJsonObject.getJSONArray(getString(R.string.json_quote));
                    JSONObject individualQuoteJson = null;

                    // Log.v(LOG_TAG, "quoteArray is " +quoteArray );

                    if (quoteArray != null && quoteArray.length() != 0) {
                        Log.v(LOG_TAG, "Quote Array Not null and length is not Zero");

                        int quoteArrayLength = quoteArray.length();
                        Log.v(LOG_TAG, "Quote array Length is " + quoteArrayLength);

                        //do the for loop backwards to put the data in date ascending order (oldest to newest)
                        // for (int i = 0; i < quoteArray.length(); i++) { //loop forwards - how API delivers it (dates descending)
                        for (int i = quoteArrayLength - 1; i >= 0; i--) { //loop backwards - ascending date
                            individualQuoteJson = quoteArray.getJSONObject(i);
                            //  Log.v(LOG_TAG, "Individual Quote is "+ individualQuoteJson);

                            //get the indivual parts that I want to keep
                            String stockSymbol = individualQuoteJson.getString(getString(R.string.json_symbol_for_historical_data));
                            String stockDate = individualQuoteJson.getString(getString(R.string.json_date));
                            String stockCloseValue = individualQuoteJson.getString(getString(R.string.json_close));

                            //  Log.v(LOG_TAG, "From historic data - Symbol:" + stockSymbol + " Date:" + stockDate + " Object:" + i);


                            //put into database (or assemble into a batch process to add into database
                            //need to figure out which values I already have in database?
                            //or just empty the database?

                            //temporarily empty the database of all of that symbol
                            //better just to update the dates that I need first


                            /////////////////
                            //Build content values from this data
                            ContentValues historicCloseContentValue =
                                    Utils.makeStockHistoryContentValue(stockSymbol,stockDate,stockCloseValue);

                            //add content values to batch operation (content values list?)

                            //for now just insert one at a time - Ineffiecient but good to test
                            Uri uriForInsert = QuoteProvider.Histories.CONTENT_URI;


                            //Replace this below with a batch operation later
                            this.getContentResolver().insert(uriForInsert,historicCloseContentValue);
                            Log.v(LOG_TAG, "LJG Insert one row into database, content value is " + historicCloseContentValue);


                            //   batchOperations.add(buildBatchOperation(jsonObject, context));
                        }


                    }


                }


               /*
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


            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "LJG Stock History String to JSON failed: " + e);
        }


        //Broadcast that results are in
        Utils.sendHistoryBroadcastForUpdate(getApplicationContext());


    }


    /**
     * Using OkHttp to get data from HTTP
     *
     * @param url to do API call
     * @return API return string
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
