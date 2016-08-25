package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

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
import java.util.Date;

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
            StockHistoryColumns._ID,
            StockHistoryColumns.SYMBOL,
            StockHistoryColumns.DATE,
            StockHistoryColumns.CLOSEPRICE,
    };

    // These indices are tied to STOCK_HISTORY_COLUMNS.  If STOCK_HISTORY_COLUMNS changes, these must change.
    static final int COL_STOCK_HISTORY_ID = 0;
    static final int COL_STOCK_HISTORY_SYMBOL = 1;
    static final int COL_STOCK_HISTORY_DATE = 2;
    static final int COL_STOCK_HISTORY_CLOSEPRICE = 3;
    /////////////////////////////////////////////////////////

    //TODO close ALL cursors in app!!!!!


    public StockHistoryIntentService() {
        super("StockHistoryIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Stock Intent Service");


        //Get the name of stock to look for
        String stockSymbol = intent.getStringExtra(DetailActivity.STOCK_SYMBOL_DETAIL_TAG);
        //  Log.v(LOG_TAG, "LJG - the stock symbol passed into intent is " +stockSymbol);
        //Get the dates to look for?????


        //Check for days already in database
        Uri uriForSymbol = QuoteProvider.Histories.withSymbol(stockSymbol);
        Cursor stockHistoryCursor = this.getContentResolver().query(
                uriForSymbol //Uri
                , STOCK_HISTORY_COLUMNS //projection (columns to return) (use nyll for no projection)
                , null // //selection Clause
                , null//selection Arguments - get the latest date ONLY
                // , null); //poosibly have sort order date ascending
                , StockHistoryColumns.DATE + " DESC LIMIT 1"); //latest date - only one


        Log.v(LOG_TAG, "LJG Stock History DateONLY Cursor is " + stockHistoryCursor);


        //String latestDateToGetHistoriesFromApi = null;
        String todaysDate = Utils.getTodayDate();
        String latestDateToGetHistoriesFromApi = todaysDate;
        String earliestDateToGetHistoriesFromApi = null;

        //examine the cursor and see what the latest date in the StockHistory Table is
        if (!stockHistoryCursor.moveToFirst()) { //No dates in database for that stock symbol

            //Go back a year for API call
           // latestDateToGetHistoriesFromApi = todaysDate;
            earliestDateToGetHistoriesFromApi = Utils.getDateOffset(todaysDate, -365); //go back a full year in history
            Log.v(LOG_TAG, "LJG Stock History DateONLY Cursor is EMPTY!!!!! - APi call goes back a whole year from "
                    + earliestDateToGetHistoriesFromApi + " to " + latestDateToGetHistoriesFromApi);

        } else { //There are dates in Database - see what the API call should be
            String latestDateInDatabase = stockHistoryCursor.getString(COL_STOCK_HISTORY_DATE);
            Date latestDateInDatabaseAsDate = Utils.convertStringToDate(latestDateInDatabase);
            Date todaysDateAsDate = Utils.convertStringToDate(todaysDate);
            Boolean isLatestDateNewerThanToday = latestDateInDatabaseAsDate.after(todaysDateAsDate);//Did you change time zones and have dates in database ahead of today's date

            //TODO THis is not working to see if dates are equal
            if (todaysDateAsDate.equals(latestDateInDatabaseAsDate) || isLatestDateNewerThanToday) {
                Log.v(LOG_TAG, "database is already up to date - cancelling stock history API call");
                //if database is ahead of today or equal to today Don't update stocks
                //If database is up to date - do NOT do API call
                return;
            } else { //do the API call from one day after the latest date in database
               // latestDateToGetHistoriesFromApi = todaysDate;
                earliestDateToGetHistoriesFromApi = Utils.getDateOffset(latestDateInDatabase, +1); //do API call for one day after what is in database
                Log.v(LOG_TAG, "LJG Stock HistoryAPI call goes from "
                        + earliestDateToGetHistoriesFromApi + " to " + latestDateToGetHistoriesFromApi);
            }
        }



        //Check the database and see if I have any stock close info for this stock
        //if yes, just chose the next day AFTer I have (no need to consult database afterwards)
        //download all dates from the day AFTER I have in DB until today's date
        //THen put all new dates into database


        //Build Yahoo API query URL for stock history
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22");
        urlStringBuilder.append(stockSymbol); //String of Stock Symbol
        urlStringBuilder.append("%22%20and%20startDate%20%3D%20%22");
       // urlStringBuilder.append("2015-09-11");//replace with coded start date
        urlStringBuilder.append(earliestDateToGetHistoriesFromApi);//replace with coded start date
        urlStringBuilder.append("%22%20and%20endDate%20%3D%20%22");
        //urlStringBuilder.append("2016-08-23"); //replace with coded end date
        urlStringBuilder.append(latestDateToGetHistoriesFromApi); //replace with coded end date
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
       // this.getContentResolver().delete(QuoteProvider.Histories.CONTENT_URI, null, null); //delete the database
       // Log.v(LOG_TAG, "LJG Delete the database");

        //TODO make a intent service that will delete the Stock histories of stocks that are deleted from Quotes database


        try {
            jsonObject = new JSONObject(getResponse);
            Log.v(LOG_TAG, "LJG JSON StockHistory is OK");
            if (jsonObject != null && jsonObject.length() != 0) {
                JSONObject queryJsonObject = jsonObject.getJSONObject(getString(R.string.json_query));

                int count = Integer.parseInt(queryJsonObject.getString(getString(R.string.json_count)));
                Log.v(LOG_TAG, "LJG JSON StockHistory count is " + count);

                if (count > 0) { //if there is a count - i.e. if there is historical data from API
                    Log.v(LOG_TAG, "LJG JSON Stock history count is 1 or more");

                    JSONObject resultsJsonObject = queryJsonObject.getJSONObject(getString(R.string.json_results));
                    JSONArray quoteArray = resultsJsonObject.getJSONArray(getString(R.string.json_quote));
                    JSONObject individualQuoteJson = null;

                    if (quoteArray != null && quoteArray.length() != 0) {
                        Log.v(LOG_TAG, "Quote Array Not null and length is not Zero");

                        int quoteArrayLength = quoteArray.length();
                        Log.v(LOG_TAG, "Quote array Length is " + quoteArrayLength);

                        //do the for loop backwards to put the data in date ascending order (oldest to newest)
                        // for (int i = 0; i < quoteArray.length(); i++) { //loop forwards - how API delivers it (dates descending)
                        for (int i = quoteArrayLength - 1; i >= 0; i--) { //loop backwards - ascending date
                            individualQuoteJson = quoteArray.getJSONObject(i);

                            //get the indivual parts that I want to keep
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

        } catch (JSONException e) {
            Log.e(LOG_TAG, "LJG Stock History String to JSON failed: " + e);
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
