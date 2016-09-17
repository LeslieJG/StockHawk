package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockHistoryColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by sam_chordas on 10/8/15.
 *
 * Other Utility Methods Added By Leslie G
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
                           // batchOperations.add(buildBatchOperationUpdate(jsonObject, context));
                            batchOperations.add(buildBatchOperation(jsonObject, context)); //add result to
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
        Log.v(LOG_TAG, "truncateBidPrice - bidprice is " + bidPrice);
        if (!bidPrice.contains("null")) {
            bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
                } else {
            bidPrice = "Retry";
        }

        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        //deal with null values from API
        if (change.contains("null")) {
            return "No Change";
        }

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

        //Here is where db is told to insert, should be told to update data already in Db - Do this in a future update
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString(context.getString(R.string.json_change));
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(context.getString(R.string.json_symbol)));
            //LJG this is where the "null" bid price comes in
            builder.withValue(QuoteColumns.NAME, jsonObject.getString(context.getString(R.string.json_name)));
            // Log.v(LOG_TAG, "LJG Inserted Name into database is " + jsonObject.getString("Name"));

            builder.withValue(QuoteColumns.BIDPRICE
                    , truncateBidPrice(jsonObject.getString(context.getString(R.string.json_bid))));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
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
     * TODO: THis is my attempt at just updating
     * NOT inserting new info
     * @param jsonObject
     * @param context
     * @return
     *//*

    public static ContentProviderOperation buildBatchOperationUpdate (JSONObject jsonObject, Context context) {

        //TODO Here is where db is told to insert, should be told to update if many additions
        //get the id to update
        Uri uriForUpdate = null;
        try {
            uriForUpdate = QuoteProvider.Quotes.withSymbol(jsonObject.getString(context.getString(R.string.json_symbol)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(
                uriForUpdate);
        try {
            String change = jsonObject.getString(context.getString(R.string.json_change));
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(context.getString(R.string.json_symbol)));
            //LJG this is where the "null" bid price comes in
            builder.withValue(QuoteColumns.NAME, jsonObject.getString(context.getString(R.string.json_name)));
            // Log.v(LOG_TAG, "LJG Inserted Name into database is " + jsonObject.getString("Name"));

            builder.withValue(QuoteColumns.BIDPRICE
                    , truncateBidPrice(jsonObject.getString(context.getString(R.string.json_bid))));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
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


*/


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
                jsonObject = jsonObject.getJSONObject(context.getString(R.string.json_query));

                int count = Integer.parseInt(jsonObject.getString(context.getString(R.string.json_count)));

                //Invalid User input will just result in one stock being searched - this is the only thing we will look for
               if (count == 1) {
                   jsonObject = jsonObject.getJSONObject(context.getString(R.string.json_results))
                            .getJSONObject(context.getString(R.string.json_quote));
                    String theBidPrice = jsonObject.getString(context.getString(R.string.json_bid));

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
            Log.e(LOG_TAG, "IsValid? String to JSON failed: " + e);
        }
        return true; //stock is valid
    }


    //LJG Trying to broadcast that API data is done
    public static void sendBroadcastForUpdate(Context context) {
        Intent dataUpdated = new Intent(context.getString(R.string.refresh_data_intent_key));
        context.sendBroadcast(dataUpdated);

    }

    //Broadcast that Stock History API data is done - This is received by MyStocksActivity to stop refresh symbol showing
    public static void sendHistoryBroadcastForUpdate(Context context) {
        Intent dataUpdated = new Intent(context.getString(R.string.stock_history_received_intent_key));
        Log.v(LOG_TAG, "In sendHistoryBroadcastForUpdate");
        context.sendBroadcast(dataUpdated);
    }


    /**
     * Used to make a Content Value for he closing price of a stock
     * on a given date.
     * To put into Histories Database eventually
     *
     * @param symbol     Stock Symbol String
     * @param date       Stock Date String
     * @param closePrice Stock Closing Price String
     * @return Content Values of the stock on this date
     */
    public static ContentValues makeStockHistoryContentValue(String symbol, String date, String closePrice) {
        ContentValues value = new ContentValues();
        value.put(StockHistoryColumns.SYMBOL, symbol);
        value.put(StockHistoryColumns.DATE, date);
        value.put(StockHistoryColumns.CLOSEPRICE, closePrice);
        return value;
    }







    /**
     * Method used for plotting stock history
     * Finds the Difference between two dates in days
     * Used for laying out x-axis for stock history chart
     *
     * @param earliestDate String of Reference date ("yyyy-MM-dd")
     * @param laterDate    tring of later date ("yyyy-MM-dd")
     * @return integer number of days between them
     */
    public static int numberOfDaysSinceFirstDate(String earliestDate, String laterDate) {
        Date dateEarly = convertStringToDate(earliestDate);
        Date dateLate = convertStringToDate(laterDate);

        TimeUnit timeUnit = TimeUnit.DAYS; //find difference in days
        long diffOfDays = getDateDiff(dateEarly, dateLate, timeUnit);
        return (int) diffOfDays;
    }



    /**
     *  Provides the date that is a number of days offset from the reference Date
     *
     * @param date String of reference day
     * @param offsetDays Number of days to offset (+ adds to date, - subtracts from date)
     * @return
     */
    public static String getDateOffset (String date, int offsetDays){
        Date incomingDate = convertStringToDate(date);
       DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //TODO LJG Confirm this is better for date format
       // DateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        Calendar c = Calendar.getInstance();
        c.setTime(incomingDate);
        c.add(Calendar.DATE, offsetDays);  // number of days to add
        String referenceDate = df.format(c.getTime());  // dt is now the new date
        return referenceDate;
    }

    /**
     * Same as above, but passing in the simple date format
     * @param date
     * @param offsetDays
     * @param df
     * @return
     */
    public static String getDateOffset (String date, int offsetDays, SimpleDateFormat df){
        Date incomingDate = convertStringToDate(date);
        Calendar c = Calendar.getInstance();
        c.setTime(incomingDate);
        c.add(Calendar.DATE, offsetDays);  // number of days to add
        String referenceDate = df.format(c.getTime());  // dt is now the new date
        return referenceDate;
    }








    /**
     * Get's today's date
     * @return String = today's date in yyyy-MM-dd format
     */
    public static String getTodayDate(){
        Date today = Calendar.getInstance().getTime(); //get current date!!!!
        //make it a string
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //TODO COnfirm this is better for date instance
       // DateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        String todayDate = df.format(today);

        return todayDate;
    }


    /**
     * Credit based on code stub by citizen conn
     * At: http://stackoverflow.com/questions/6510724/how-to-convert-java-string-to-date-object
     *
     * @param dateAsString The date in the form yyyy-MM-dd
     * @return Java.Date version of the date
     */
    public static Date convertStringToDate(String dateAsString) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //TODO COnfirm this is better for date instance
        //DateFormat df = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
        Date date;
        try {
            date = df.parse(dateAsString);
            return date;
        } catch (ParseException e) {
         //   e.printStackTrace();
        }
        return null; //if error return null instead of catching ParseException
    }


    /**
     * Credit Sebastien Lorber
     * From: http://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
     * Get a diff between two dates
     *
     * @param oldestDate the oldest date
     * @param newestDate the newest date
     * @param timeUnit   the unit in which you want the diff (eg. TimeUnit.DAYS)
     * @return the diff value, in the provided unit
     */
    private static long getDateDiff(Date oldestDate, Date newestDate, TimeUnit timeUnit) {
        long diffInMillies = newestDate.getTime() - oldestDate.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }



    ///TODO Delete this function after testing
    public static void reportNumberOfRowsInHistoriesDatabase(Context mContext){
        //Test Entire Database
        Uri uriForSAllHistories = QuoteProvider.Histories.CONTENT_URI;
        Cursor fullStockHistoryCursor = mContext.getContentResolver().query(
                uriForSAllHistories //Uri
                , null //projection (columns to return) (use nyll for no projection)
                , null // //selection Clause
                , null//selection Arguments
                , null); //poosibly have sort order date ascending


        Log.v(LOG_TAG, " THe Histories Tables still has " + fullStockHistoryCursor.getCount() + " Rows in it");
        fullStockHistoryCursor.close();


    }


}
