package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.sam_chordas.android.stockhawk.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Leslie on 2016-09-15.
 */
public class WidgetUtilsTest extends AndroidTestCase { //LJG Should use Android Support Testing Library, but I don't know how yet
    Context mContext;
    String defaultSymbol = "Default";

    @Before
    public void setUp() throws Exception {

        mContext = getContext();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAddWidgetIdToSharedPrefs() throws Exception {
        mContext = getContext();
        int widgetID = 999;
        String stockSymbol = "ZZZZ";
        String prefKey = mContext.getString(R.string.widget_id_pref_key) + widgetID;

        //Put the shared pref in
        WidgetUtils.addWidgetIdToSharedPrefs(mContext, widgetID, stockSymbol);

        //Now get my own copy of shared prefs and see if it is there
        //get Shared Prefs
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_name), Context.MODE_PRIVATE);

        //First see if it is there
        assertTrue(sharedPref.contains(prefKey)); //see if key pair in shared preferences

        String symbolFromSharedPrefs = sharedPref.getString(prefKey, defaultSymbol);

        assertFalse(symbolFromSharedPrefs.equals(defaultSymbol)); //ensure value is not default
        assertTrue(symbolFromSharedPrefs.equals(stockSymbol)); //ensure symbol is the correct one

    }

    @Test
    public void testRemoveWidgetIdFromSharedPrefs() throws Exception {
        String stockSymbol = "LLL";
        int widgetID = 77;
        //use all WidgetUtils methods (now all tested for this test)

        //add symbol
        WidgetUtils.addWidgetIdToSharedPrefs(mContext, widgetID, stockSymbol);

        //get symbol and test that it really is there
        String symbolFromPrefs = WidgetUtils.getWidgetSymbolFromWidgetId(mContext, widgetID);

        //make sure it is the same
        assertTrue(symbolFromPrefs.equals(stockSymbol));

        //now delete that key/pair from shared prefs
        WidgetUtils.removeWidgetIdFromSharedPrefs(mContext, widgetID);

        //now try to get it back and make sure that we only get back default value and NOT the original stock symbol
        symbolFromPrefs = WidgetUtils.getWidgetSymbolFromWidgetId(mContext, widgetID);
        assertFalse(symbolFromPrefs.equals(stockSymbol)); //SHOULD no longer be the stock symbol we passed in
        assertTrue(symbolFromPrefs.equals(WidgetUtils.NO_SUCH_STOCK_SYMBOL_IN_PREFS));//IT should be the default stock symbol
        //assertTrue(false);
    }

    @Test
    public void testGetWidgetSymbolFromWidgetId() throws Exception {
        String stockSymbol = "YYYY";
        int widgetID = 88;
        String prefKey = mContext.getString(R.string.widget_id_pref_key) + widgetID;

        //first add symbol to shared prefs using tested method
        WidgetUtils.addWidgetIdToSharedPrefs(mContext, widgetID, stockSymbol);

        //now get symbol from shared pref
        String widgetSymbolFromPrefs = WidgetUtils.getWidgetSymbolFromWidgetId(mContext, widgetID);

        assertFalse(widgetSymbolFromPrefs.equals(WidgetUtils.NO_SUCH_STOCK_SYMBOL_IN_PREFS)); //not default symbol
        assertTrue(widgetSymbolFromPrefs.equals(stockSymbol)); //but it is the symbol passed in
    }

}