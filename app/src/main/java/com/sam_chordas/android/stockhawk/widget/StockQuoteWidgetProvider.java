package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Leslie on 2016-09-08.
 */
public class StockQuoteWidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // super.onUpdate(context, appWidgetManager, appWidgetIds);

        //N.B. on Update is called on UI thread. Move it off the UI Thread and put it on intent service
        context.startService(new Intent(context, StockQuoteWidgetIntentService.class));





        /*//Update all the static widgets
        for (int appWidgetID : appWidgetIds) { //go through all the widgets we have
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_stock_quotes); //here is the view to use


            //Set what happens when you click on a widget
            Intent intent = new Intent(context, MyStocksActivity.class); //explicit intent to start the app itself
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0); //context, int request code, intent, int flags
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, views); //update the info in them


        }*/


    }
}
