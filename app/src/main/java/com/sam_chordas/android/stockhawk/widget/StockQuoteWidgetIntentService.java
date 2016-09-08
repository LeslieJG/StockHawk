package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
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
                    R.layout.widget_stock_quotes); //here is the view to use


            //Set what happens when you click on a widget
            Intent launchIntent = new Intent(mContext, MyStocksActivity.class); //explicit intent to start the app itself
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, 0); //context, int request code, intent, int flags
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, views); //update the info in widgets


        }



    }
}
