package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Leslie on 2016-09-15.
 *
 * Put some general Utilities used across widget classes here
 *
 * This is NOT working - need more info on Android Testing in General
 *
 * Hopefully I'll learn more in the next course!!!!
 */

public class WidgetUtils {
        public static final String NO_SUCH_STOCK_SYMBOL_IN_PREFS = "Stock Symbol Not In Prefs";
        public final static String LOG_TAG = WidgetUtils.class.getSimpleName();

    public static void addWidgetIdToSharedPrefs (Context context, int widgetId, String stockSymbol){
        Log.v(LOG_TAG, "addWidgetIdToSharedPrefs - incoming widgetID:" + widgetId + " StockSymbol:" + stockSymbol );


        //get Shared Prefs
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_name), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(
               context.getString(R.string.widget_id_pref_key) + widgetId, stockSymbol);
       editor.apply(); //apply instead of commit for immediate adding- use so that it is known to
        // be added before it is accessed by widget provider
    }

    public static void removeWidgetIdFromSharedPrefs(Context context, int widgetId){
        //get Shared Prefs
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_name), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(
                context.getString(R.string.widget_id_pref_key) + widgetId);
        editor.commit(); //commit is for synchronus acting - use so that it is known to
        // be added/removed before it is accessed by widget provider
    }

    public static String getWidgetSymbolFromWidgetId(Context context, int widgetId) {
        //get Shared Prefs
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_name), Context.MODE_PRIVATE);

       return  sharedPref.getString(
               context.getString(R.string.widget_id_pref_key) + widgetId, NO_SUCH_STOCK_SYMBOL_IN_PREFS);

    }


}
