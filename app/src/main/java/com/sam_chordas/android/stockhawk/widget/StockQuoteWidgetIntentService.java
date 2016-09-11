package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by Leslie on 2016-09-09.
 *
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
            QuoteColumns.ISCURRENT    };

    // These indices are tied to STOCK_QUOTE_COLUMNS.  If STOCK_QUOTE_COLUMNS changes, these must change.
    static final int COL_STOCK_ID = 0;
    static final int COL_STOCK_SYMBOL=1;
    static final int COL_STOCK_PERCENT_CHANGE=2;
    static final int COL_STOCK_NAME=3;
    static final int COL_STOCK_CHANGE=4;
    static final int COL_STOCK_BIDPRICE=5;
    static final int COL_STOCK_ISUP=6;
    static final int COL_STOCK_ISCURRENT=7;
    /////////////////////////////////////////////////////////


    public StockQuoteWidgetIntentService() {
        super("Stock Quote Widget Intent Service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        //Retrieve all of the Stock Quote widget ids: these are the widgets we need to update
        //This was originally done in the stock Quote Widget Provider
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, StockQuoteWidgetProvider.class));


        ////////////////The code from the StockQuoteWidgetProvider - moved here to take it off UI thread! ////////////

        //Update all the static widgets
        for (int appWidgetID : appWidgetIds) { //go through all the widgets we have
            RemoteViews views = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.widget_stock_quotes); //here is the view to use - TODO Will have to change this when using different sizes



            //update the widget view with real data



            //Get a cursor for the data in database
            Uri stockQuoteUri = QuoteProvider.Quotes.CONTENT_URI; //use the general Content Uri for now to get all stock quotes

            //data should have the entire contents of the quote Cursor Database in it
            Cursor data = getContentResolver().query(stockQuoteUri, //uri
                    STOCK_QUOTE_COLUMNS, //projection
                    null,
                    null,
                    QuoteColumns._ID + " ASC"); //sort order

            if (data == null){ //cursor null - something went wrong - don't update Widget
                return;
            }

            if (!data.moveToFirst()) { //if no data in cursor ----then what???? - show error in widget?
                //it means there are no stocks to show in widget - what to do???
                // TODO what to show in widget if no stocks in database?
                //Do that here

            }

            //we have stocks

            //for now just show the first stock
            //TODO Make a stock selector for the widget and show that stock - for now just show first stock in db
            String stockSymbol = data.getString(COL_STOCK_SYMBOL);
            String stockPrice = data.getString(COL_STOCK_BIDPRICE);
            String stockPercentChange = data.getString(COL_STOCK_PERCENT_CHANGE);

            //display the single stock into the 1x1 widget

            views.setTextViewText(R.id.widget_stock_symbol, stockSymbol);
            views.setTextViewText(R.id.widget_stock_price, stockPrice);

            //TODO will have to change percent change background colour and  + or - sign and also add percent symbol
            views.setTextViewText(R.id.widget_stock_price_change, stockPercentChange);

            //change the background of stock percent change to red or green depending on whether stock is going up or down
            int sdk = Build.VERSION.SDK_INT;
            if (data.getInt(COL_STOCK_ISUP) == 1) { //if stock going up
                //credit for below line:  http://stackoverflow.com/questions/6201410/how-to-change-widget-layout-background-programatically
                views.setInt(R.id.widget_stock_price_change, "setBackgroundResource",R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.widget_stock_price_change, "setBackgroundResource",R.drawable.percent_change_pill_red);
            }


            //TODO Add content descriptions to widget as well - later


/*
            String stockSymbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
            String stockName = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME));
            viewHolder.symbol.setText(stockSymbol);
            viewHolder.symbol.setContentDescription(stockName);

            String stockBidPrice = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
            viewHolder.bidPrice.setText(stockBidPrice);
            viewHolder.bidPrice.setContentDescription(mContext.getString(R.string.bid_price_content_description, stockBidPrice));

            int sdk = Build.VERSION.SDK_INT;
            if (cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.change.setBackgroundDrawable(
                            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
                } else {
                    viewHolder.change.setBackground(
                            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
                }
            } else {
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.change.setBackgroundDrawable(
                            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
                } else {
                    viewHolder.change.setBackground(
                            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
                }
            }
            if (Utils.showPercent) {
                String stockPercentChange = cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                viewHolder.change.setText(stockPercentChange);
                viewHolder.change.setContentDescription(mContext
                        .getString(R.string.percent_change_content_description
                                , stockPercentChange
                                , stockChangeUpOrDown(stockPercentChange)));


            } else {

                String stockChange = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
                viewHolder.change.setText(stockChange);
                viewHolder.change.setContentDescription(mContext
                        .getString(R.string.stock_change_content_description
                                , stockChange
                                , stockChangeUpOrDown(stockChange)));
            }





            */










            //close cursor when done
            data.close();


            //Set what happens when you click on a widget
            Intent launchIntent = new Intent(mContext, MyStocksActivity.class); //explicit intent to start the app itself
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, 0); //context, int request code, intent, int flags
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, views); //update the info in widgets


        }



    }
}
