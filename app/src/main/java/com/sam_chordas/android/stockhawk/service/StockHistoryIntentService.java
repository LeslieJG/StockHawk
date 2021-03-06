package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockHistoryColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;
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
    /////////////////////Database projection constants///////////////
    //For making good use of database Projections specify the columns we need
    private static final String[] STOCK_HISTORY_COLUMNS = {
            StockHistoryColumns.DATE};
    // These indices are tied to STOCK_HISTORY_COLUMNS.  If STOCK_HISTORY_COLUMNS changes, these must change.
    static final int COL_STOCK_HISTORY_DATE = 0;
    /////////////////////////////////////////////////////////

    public StockHistoryIntentService() {
        super("StockHistoryIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Get the name of stock to look for
        String stockSymbol = intent.getStringExtra(DetailActivity.STOCK_SYMBOL_DETAIL_TAG);

        //Check for days already in database
        Uri uriForSymbol = QuoteProvider.Histories.withSymbol(stockSymbol);
        Cursor stockHistoryCursor = this.getContentResolver().query(
                uriForSymbol //Uri
                , STOCK_HISTORY_COLUMNS //projection (columns to return) (use null for no projection)
                , null // //selection Clause
                , null//selection Arguments - get the latest date ONLY
                // , null); //poosibly have sort order date ascending
                , StockHistoryColumns.DATE + " DESC LIMIT 1"); //latest date - only one

        String todaysDate = Utils.getTodayDate();
        String latestDateToGetHistoriesFromApi = todaysDate;
        String earliestDateToGetHistoriesFromApi = null;

        try {
            //examine the cursor and see what the latest date in the StockHistory Table is
            if (!stockHistoryCursor.moveToFirst()) { //No dates in database for that stock symbol
                earliestDateToGetHistoriesFromApi = Utils.getDateOffset(todaysDate, -365); //go back a full year in history for API call
            } else { //There are dates in Database - see what the API call should be
                String latestDateInDatabase = stockHistoryCursor.getString(COL_STOCK_HISTORY_DATE);


                if (Utils.numberOfDaysSinceFirstDate(todaysDate, latestDateInDatabase) <= 0) {
                    //if latest date in database is equal to or ahead of today (i.e. you've changed time zones)
                    //If database is up to date - do NOT do API call
                    return;
                } else { //do the API call from one day after the latest date in database
                    earliestDateToGetHistoriesFromApi = Utils.getDateOffset(latestDateInDatabase, +1); //do API call for one day after what is in database
                }
            }
        } finally {
            stockHistoryCursor.close();
        }


        //Build Yahoo API query URL for stock history
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22");
        urlStringBuilder.append(stockSymbol); //String of Stock Symbol
        urlStringBuilder.append("%22%20and%20startDate%20%3D%20%22");
        urlStringBuilder.append(earliestDateToGetHistoriesFromApi);//replace with coded start date
        urlStringBuilder.append("%22%20and%20endDate%20%3D%20%22");
        urlStringBuilder.append(latestDateToGetHistoriesFromApi); //replace with coded end date
        urlStringBuilder.append("%22&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");

        String urlString;
        String getResponse = null; //JSON from Yahoo HTTP
        if (urlStringBuilder != null) { //do api call and get data back
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //Now get the JSON from the fetchData string
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(getResponse);
            // if (jsonObject != null && jsonObject.length() != 0) {
            if (jsonObject != null) {
                if (jsonObject.length() != 0) {
                    JSONObject queryJsonObject = jsonObject.getJSONObject(getString(R.string.json_query));

                    int count = Integer.parseInt(queryJsonObject.getString(getString(R.string.json_count)));
                    if (count > 0) { //if there is a count - i.e. if there is historical data from API
                        JSONObject resultsJsonObject = queryJsonObject.getJSONObject(getString(R.string.json_results));
                        JSONArray quoteArray = resultsJsonObject.getJSONArray(getString(R.string.json_quote));
                        JSONObject individualQuoteJson = null;

                        if (quoteArray != null && quoteArray.length() != 0) {
                            int quoteArrayLength = quoteArray.length();

                            //do the for loop backwards to put the data in date ascending order (oldest to newest)
                            for (int i = quoteArrayLength - 1; i >= 0; i--) { //loop backwards - ascending date
                                individualQuoteJson = quoteArray.getJSONObject(i);

                                //get the indivual parts to keep
                                String stockSymbolFromJson = individualQuoteJson.getString(getString(R.string.json_symbol_for_historical_data));
                                String stockDate = individualQuoteJson.getString(getString(R.string.json_date));
                                String stockCloseValue = individualQuoteJson.getString(getString(R.string.json_close));

                                ContentProviderOperation.Builder batchContentProviderOperationBuilder = ContentProviderOperation.newInsert(
                                        QuoteProvider.Histories.CONTENT_URI); //Builder to build bulk insert content provider operation


                                /////////////////
                                //Build content values from this data
                                ContentValues historicCloseContentValue =
                                        Utils.makeStockHistoryContentValue(stockSymbolFromJson, stockDate, stockCloseValue);

                                //add content values to batch operation (content values list?)
                                batchContentProviderOperationBuilder.withValues(historicCloseContentValue);

                          /*  ContentProviderOperation contentProviderOperation =
                                    batchContentProviderOperationBuilder.build();
                            */
                                //add it to the arraylist of batch operations
                                batchOperations.add(batchContentProviderOperationBuilder.build());
                            }
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Build the bulk insert content values operation
        //do the bulk insert
        try {
            getContentResolver().applyBatch(QuoteProvider.AUTHORITY, batchOperations); //do Bulk insert of all stock history info
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        //Broadcast that results are in - so that the line chart can grab new data and update
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
