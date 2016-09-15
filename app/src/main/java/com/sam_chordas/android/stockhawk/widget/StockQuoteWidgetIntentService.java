package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by Leslie on 2016-09-09.
 * <p>
 * To Deal with the Widget's onUpdate Method, but OFF the UI thread we are
 * using this intent service
 */
public class StockQuoteWidgetIntentService extends IntentService {
    private final static String LOG_TAG = StockQuoteWidgetIntentService.class.getSimpleName();
    Context mContext = this; //just to show the context explicitly for now (should be able to change all context calls to "this" later on

    //For cursor projections
    /////////////////////Database projection constants///////////////
    //For making good use of database Projections specify the columns we need
    private static final String[] STOCK_QUOTE_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.NAME,
            QuoteColumns.CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.ISUP,
            QuoteColumns.ISCURRENT};

    // These indices are tied to STOCK_QUOTE_COLUMNS.  If STOCK_QUOTE_COLUMNS changes, these must change.
    static final int COL_STOCK_ID = 0;
    static final int COL_STOCK_SYMBOL = 1;
    static final int COL_STOCK_PERCENT_CHANGE = 2;
    static final int COL_STOCK_NAME = 3;
    static final int COL_STOCK_CHANGE = 4;
    static final int COL_STOCK_BIDPRICE = 5;
    static final int COL_STOCK_ISUP = 6;
    static final int COL_STOCK_ISCURRENT = 7;
    /////////////////////////////////////////////////////////


    public StockQuoteWidgetIntentService() {
        super("Stock Quote Widget Intent Service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "in onHandle Intent for updating widgets");

        //Retrieve all of the Stock Quote widget ids: these are the widgets we need to update
        //This was originally done in the stock Quote Widget Provider
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, StockQuoteWidgetProvider.class));


        ////////////////The code from the StockQuoteWidgetProvider - moved here to take it off UI thread! ////////////

        //Update all the static widgets
        for (int appWidgetID : appWidgetIds) { //go through all the widgets we have

            Log.v(LOG_TAG, "Widget ID to update is " + appWidgetID);
            ///////Set correct Layout Depending on widget size///////
            //get teh widget's width to assign correct layout
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetID);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_stock_quotes_default_width);

            int layoutId; //to store the layour ID to use depending on widget size
            if (widgetWidth < defaultWidth) {
                layoutId = R.layout.widget_stock_quotes_small;
            } else {
                layoutId = R.layout.widget_stock_quotes_default_size;
            }
            RemoteViews views = new RemoteViews(
                    mContext.getPackageName(), layoutId); //here is the view to use

          /*  RemoteViews views = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.widget_stock_quotes_default_size); //here is the view to use
*/


            //////update the widget view with real data//////
            //Get a cursor for the data in database
            //Uri stockQuoteUri = QuoteProvider.Quotes.CONTENT_URI; //use the general Content Uri for now to get all stock quotes

           Uri stockQuoteUri = QuoteProvider.Quotes.withSymbol("YHOO"); //use the general Content Uri for now to get all stock quotes

            //TODO This is where I get symbol from prefs
            //Use the Stock symbol associated with the widgetID
          //  String stockSymbolFromPrefs = WidgetUtils.getWidgetSymbolFromWidgetId(mContext, appWidgetID);
           // Uri stockQuoteUri = QuoteProvider.Quotes.withSymbol(stockSymbolFromPrefs); //use the general Content Uri for now to get all stock quotes


            //data should have the entire contents of the quote Cursor Database in it
                       Cursor data = getContentResolver().query(stockQuoteUri, //uri
                    STOCK_QUOTE_COLUMNS, //projection
                    null,
                    null,
                    QuoteColumns._ID + " ASC"); //sort order

/*
            Cursor data = getContentResolver().query(stockQuoteUri, //uri
                    STOCK_QUOTE_COLUMNS, //projection
                    null,
                    null,
                    null); //sort order - should just have ONE row*/

            //TODO Dump the cursor to see what's in DB
            DatabaseUtils.dumpCursor(data);

            if (data == null) { //cursor null - something went wrong - don't update Widget\
                Log.v(LOG_TAG, "Cursor data is NULL");

                return;
            }

            if (!data.moveToFirst()) { //if no data in cursor ----then what???? - show error in widget?
                //it means there are no stocks to show in widget - what to do???
                // TODO what to show in widget if no stocks in database?
                //Do that here
                Log.v(LOG_TAG, "Cursor data is ZERO data");
                //Log.v(LOG_TAG, "")

                //change view to error view
               /* views = new RemoteViews(
                        mContext.getPackageName(), R.id.widget_error); //here is the view to use - Gives defulat widget error meesage!!!
*/
                if (widgetWidth < defaultWidth) {
                    layoutId = R.layout.widget_error_small;
                } else {
                    layoutId = R.layout.widget_error_default_size;
                }

                views = new RemoteViews(
                        mContext.getPackageName(), layoutId);


               // views.setTextViewText(R.id.widget_error_text)
               /* views.setTextViewText(R.id.widget_stock_symbol, "No Stock in App. Delete Widget"); //should display error message here
                views.setTextViewText(R.id.widget_stock_price, "");
                views.setTextViewText(R.id.widget_stock_price_change, "");*/
                //  appWidgetManager.updateAppWidget(appWidgetID, views); //update the info in widgets
                //  data.close();

                //  continue; //skips out of current iteration of loop only
                //allows other iterations to update other widgets
            } else {//we have stocks


                //for now just show the first stock
                //TODO Make a stock selector for the widget and show that stock - for now just show first stock in db
                String stockSymbol = data.getString(COL_STOCK_SYMBOL);
                String stockPrice = data.getString(COL_STOCK_BIDPRICE);
                String stockPercentChange = data.getString(COL_STOCK_PERCENT_CHANGE);

                //display the single stock into the 1x1 widget


                views.setTextViewText(R.id.widget_stock_symbol, stockSymbol);
                views.setTextViewText(R.id.widget_stock_price, "$" + stockPrice);
                //views.setTextViewText(R.id.widget_stock_price, "$$66");
                Log.v(LOG_TAG, "Stock Price from DB is " + stockPrice + " symbol is " + stockSymbol + " percent change is " + stockPercentChange);
                //TODO: Stock pid price in widget is not always same as displayed in app - solve!!!!


                views.setTextViewText(R.id.widget_stock_price_change, stockPercentChange);
                //change the background of stock percent change to red or green depending on whether stock is going up or down
                int sdk = Build.VERSION.SDK_INT;
                if (data.getInt(COL_STOCK_ISUP) == 1) { //if stock going up
                    //credit for below line:  http://stackoverflow.com/questions/6201410/how-to-change-widget-layout-background-programatically
                    views.setInt(R.id.widget_stock_price_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                    views.setInt(R.id.widget_stock_price_change, "setTextColor", R.color.widget_text_color);

                } else {
                    views.setInt(R.id.widget_stock_price_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                    views.setInt(R.id.widget_stock_price_change, "setTextColor", Color.WHITE);
                }


            }


            //TODO Add content descriptions to widget as well - later




            //close cursor when done
            data.close();


            //Set what happens when you click on a widget
            Intent launchIntent = new Intent(mContext, MyStocksActivity.class); //explicit intent to start the app itself
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, 0); //context, int request code, intent, int flags
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, views); //update the info in widgets


        }


    }


    /* Credit :Udactiy Sunshine
    returns widget width in pixels
     */
    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_stock_quotes_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    /*
    Credit: Udacity Sunshine app
    returns widget width in pixels
    This method will calculate the pixel width based on resizable widget
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_stock_quotes_default_width);
    }

}
