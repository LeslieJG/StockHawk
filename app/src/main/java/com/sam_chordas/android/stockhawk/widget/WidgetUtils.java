package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.SharedPreferences;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Leslie on 2016-09-15.
 * <p>
 * Put some general Utilities used across widget classes here.
 * <p>
 * To allow the widget Ids (as key) and the stock symbol (as Value)
 * to be stored in shared preferrences. To allow each widget to be able to
 * update when the stock information changes
 */

public class WidgetUtils {
    public static final String NO_SUCH_STOCK_SYMBOL_IN_PREFS = "Stock Symbol Not In Prefs";
    public final static String LOG_TAG = WidgetUtils.class.getSimpleName();

    public static void addWidgetIdToSharedPrefs(Context context, int widgetId, String stockSymbol) {
        //get Shared Prefs
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_name), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(
                context.getString(R.string.widget_id_pref_key) + widgetId, stockSymbol);
        editor.apply(); //apply instead of commit for immediate adding- use so that it is known to
        // be added before it is accessed by widget provider
    }

    public static void removeWidgetIdFromSharedPrefs(Context context, int widgetId) {
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

        return sharedPref.getString(
                context.getString(R.string.widget_id_pref_key) + widgetId, NO_SUCH_STOCK_SYMBOL_IN_PREFS);

    }
}
