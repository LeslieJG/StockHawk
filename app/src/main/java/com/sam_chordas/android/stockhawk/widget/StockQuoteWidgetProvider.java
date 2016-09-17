package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Leslie on 2016-09-08.
 */
public class StockQuoteWidgetProvider extends AppWidgetProvider {
    private static final String LOG_TAG = StockQuoteWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // super.onUpdate(context, appWidgetManager, appWidgetIds);

        //N.B. on Update is called on UI thread. Move it off the UI Thread and put it on intent service
        context.startService(new Intent(context, StockQuoteWidgetIntentService.class));





        /*//Update all the static widgets
        for (int appWidgetID : appWidgetIds) { //go through all the widgets we have
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_stock_quotes_default_size); //here is the view to use


            //Set what happens when you click on a widget
            Intent intent = new Intent(context, MyStocksActivity.class); //explicit intent to start the app itself
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0); //context, int request code, intent, int flags
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetID, views); //update the info in them


        }*/


    }




    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        //update it too!
        context.startService(new Intent(context, StockQuoteWidgetIntentService.class));
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(context.getString(R.string.refresh_data_intent_key))) {
            // Do stuff - maybe update my view based on the changed DB contents

            Log.v(LOG_TAG, "onREceive - LJG API call done will now update widget");
            context.startService(new Intent(context, StockQuoteWidgetIntentService.class));


        }


    }

    //Delete the shared pref key value pair of widget id and symbol for widget
    //when deleting widget
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        //remove the widgetId from shared prefs - no longer need to keep track of it
        WidgetUtils.removeWidgetIdFromSharedPrefs(context, appWidgetIds[0]);

        /*Log.v(LOG_TAG, "LJG Deleted a Widget");
        Log.v(LOG_TAG, "LJG Number of widgets deleted is " + appWidgetIds.length);
        Log.v(LOG_TAG, "LJG Deleted Widget ID is " + appWidgetIds[0]);
      */
    }
}
