package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON, Context context) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
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
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    //LJG this is what really errors off - who calls it
    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject, Context context) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            //String change = jsonObject.getString("Change");
            String change = jsonObject.getString(context.getString(R.string.json_change));
            // builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(context.getString(R.string.json_symbol)));
            //LJG this is where the "null" bid price comes in
            //  builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));


            //TODO Inserting name here from JSON
            //LJG Delete comments when done
            builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
           // Log.v(LOG_TAG, "LJG Inserted Name into database is " + jsonObject.getString("Name"));


            builder.withValue(QuoteColumns.BIDPRICE
                    , truncateBidPrice(jsonObject.getString(context.getString(R.string.json_bid))));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    //  jsonObject.getString("ChangeinPercent"), true));
                    jsonObject.getString(context.getString(R.string.json_change_in_percent)), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }


    /**
     * Checks to see if JSON is for a stock with a valid Bid Price (i.e. is this a valid stock?)
     * If NOT it shows a toast on User's screen indicating stock is not valid
     *
     * @return Boolean true for valid stock
     */
    public static Boolean isStockValid(String JSON, final Context context) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                // jsonObject = jsonObject.getJSONObject("query");
                jsonObject = jsonObject.getJSONObject(context.getString(R.string.json_query));

                // int count = Integer.parseInt(jsonObject.getString("count"));
                int count = Integer.parseInt(jsonObject.getString(context.getString(R.string.json_count)));

                //Invalid User input will just result in one stock being searched - this is the only thing we will look for
                if (count == 1) {
                    //  jsonObject = jsonObject.getJSONObject("results")
                    jsonObject = jsonObject.getJSONObject(context.getString(R.string.json_results))
                            // .getJSONObject("quote");
                            .getJSONObject(context.getString(R.string.json_quote));
                    // String theBidPrice = jsonObject.getString("Bid");
                    String theBidPrice = jsonObject.getString(context.getString(R.string.json_bid));

                    // if (theBidPrice.equals("null")) { //this is Not a valid stock
                    if (theBidPrice.equals(context.getString(R.string.json_null))) { //this is Not a valid stock
                        //return a toast - Handler needed to show toast on UI thread from non-UI thread
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //    Toast.makeText(context, "Not A Valid Stock", Toast.LENGTH_SHORT).show();
                                Toast.makeText(context, R.string.toast_error_not_valid_stock_symbol
                                        , Toast.LENGTH_SHORT).show();
                            }
                        });

                        return false; //stock is not valid
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return true; //stock is valid
    }


    //LJG Trying to broadcast that API data is done
    public static void sendBroadcastForUpdate(Context context) {
        // Intent dataUpdated = new Intent(MyStocksActivity.REFRESH_DATA_INTENT);
        Intent dataUpdated = new Intent(context.getString(R.string.refresh_data_intent_key));
        // getApplicationContext().sendBroadcast(new Intent(MyStocksActivity.REFRESH_DATA_INTENT));
        context.sendBroadcast(dataUpdated);

    }

    //LJG Trying to broadcast that Stock History API data is done
    public static void sendHistoryBroadcastForUpdate(Context context) {
        // Intent dataUpdated = new Intent(MyStocksActivity.REFRESH_DATA_INTENT);
       Intent dataUpdated = new Intent(context.getString(R.string.stock_history_received_intent_key));
       // Intent dataUpdated = new Intent();
       // dataUpdated.setAction("FUCK");
        // getApplicationContext().sendBroadcast(new Intent(MyStocksActivity.REFRESH_DATA_INTENT));
        Log.v(LOG_TAG, "In sendHistoryBroadcastForUpdate");

        context.sendBroadcast(dataUpdated);

    }


}
