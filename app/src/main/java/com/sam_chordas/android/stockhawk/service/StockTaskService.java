package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;



    //Constructor
    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    /*
    Over Ridden method for GcmTaskService
     */
    @Override
    public int onRunTask(TaskParams params) {
        Log.v(LOG_TAG, "LJG Starting onRunTask");

        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append(mContext.getString(R.string.api_url_query_base));
            urlStringBuilder.append(URLEncoder.encode(mContext.getString(R.string.api_url_query_2)
                    + "in (", mContext.getString(R.string.utf_8_encoding)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Do update if the tag is init(ial) or periodic
        //  if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
        if (params.getTag().equals(mContext.getString(R.string.intent_init))
                || params.getTag().equals(mContext.getString(R.string.service_periodic))) {

            isUpdate = true;

            initQueryCursor = mContext.getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI, //table name
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, //projection (columns to return)
                    null, //selection Clause
                    null, //selection Arguments
                    null); //sort order
            if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"" + mContext.getString(R.string.yahoo_stock_symbol) +
                                            "\",\"" + mContext.getString(R.string.apple_stock_symbol) +
                                            "\",\"" + mContext.getString(R.string.google_stock_symbol) +
                                            "\",\"" + mContext.getString(R.string.microsoft_stock_symbol) +
                                            "\")",
                                    mContext.getString(R.string.utf_8_encoding)));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else if (initQueryCursor != null) {

                 // DatabaseUtils.dumpCursor(initQueryCursor);
                //Get a full list of DB here and dump it out to logcat
                /*Log.v(LOG_TAG, "LJG Dumping full database BEFORE it is updated");
                dumpFullDbToLogcat(mContext);*/



                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex(QuoteColumns.SYMBOL)) + "\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(),
                            mContext.getString(R.string.utf_8_encoding)));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            initQueryCursor.close(); //close cursor when done with it

        } else if (params.getTag().equals(mContext.getString(R.string.intent_add))) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString(mContext.getString(R.string.intent_symbol));
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")",
                        mContext.getString(R.string.utf_8_encoding)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append(mContext.getString(R.string.api_url_query_3)
                + mContext.getString(R.string.api_url_query_4));

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {

                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        Log.v(LOG_TAG, "LJG Updating database");
                        mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                null, null);

                        Log.v(LOG_TAG, "Dump database After UPDATING");
                        dumpFullDbToLogcat(mContext);
                    }

                    //Log the value of output for debuggin
                    //   Log.v(LOG_TAG, "The JSON response is "+ getResponse);

                    //if data is valid, ONLY then try to put it into Database and pass get the JSON
                    Boolean stockValid = Utils.isStockValid(getResponse, mContext);
                    if (stockValid) { //ONLY update if stock is valid
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                                Utils.quoteJsonToContentVals(getResponse, mContext));

                        Log.v(LOG_TAG, "LJG Dumping Entire Database for debugging AFTER entire DB is updated!");
                        dumpFullDbToLogcat(mContext);
                        //////////////end debugging - delete above lines when done //////////////



                    } else {  //stock NOT valid!!!! - delete this else!!!!
                    }

                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //LJG before returning result let the SwipreRefresh know that the refresh is done
        //Credit: http://stackoverflow.com/users/574859/maximumgoat
        //from this thread http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity

        Log.v(LOG_TAG, "LJG Stock Task Service Done - will send broadcast for update");
        Utils.sendBroadcastForUpdate(mContext);

        return result;
    }


    //Helpful method for debugging ///
    private void dumpFullDbToLogcat(Context mContext){
        Uri stockQuoteUri = QuoteProvider.Quotes.CONTENT_URI; //use the general Content Uri for now to get all stock quotes
        Cursor dbTestCursor = mContext.getContentResolver().query(stockQuoteUri, null, null, null, null);
        DatabaseUtils.dumpCursor(dbTestCursor);
        dbTestCursor.close();
        //////////////end debugging - delete above lines when done //////////////
    }

}
