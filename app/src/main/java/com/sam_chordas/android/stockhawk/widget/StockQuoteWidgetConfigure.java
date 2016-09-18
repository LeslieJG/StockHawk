package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by Leslie on 2016-09-12.
 * <p>
 * For Handling the One-time configuration of each widget.
 * <p>
 * This allows the users to pick which stock the widget displays,
 * allowing for many widgets each displaying their own stock information.
 * <p>
 * This Activity is created by an intent which passes in the widget ID
 * <p>
 * This Activity must return a result that included with widget ID
 * This Activity should also call the widget's onUpdate() method to update the data for
 * the first time after being created
 * <p>
 * <p>
 * <p>
 * Modelled after https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/appwidget/ExampleAppWidgetConfigure.java
 */
public class StockQuoteWidgetConfigure extends AppCompatActivity {
    private static final String LOG_TAG = StockQuoteWidgetConfigure.class.getSimpleName();
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID; //for storing the widget Id of the widget we are making
    int mCursorRowId; //to store the row ID of the cursor of the button pressed (to allow to symbol lookup from the cursor later on)

    //For cursor projections
    /////////////////////Database projection constants///////////////
    //For making good use of database Projections specify the columns we need
    private static final String[] STOCK_QUOTE_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.NAME,
    };

    // These indices are tied to STOCK_QUOTE_COLUMNS.  If STOCK_QUOTE_COLUMNS changes, these must change.
    static final int COL_STOCK_ID = 0;
    static final int COL_STOCK_SYMBOL = 1;
    static final int COL_STOCK_NAME = 2;

    /////////////////////////////////////////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use for this configure class
        setContentView(R.layout.widget_configure);

        //Make this screen a PopUp Screen on top of home screen
        //Credit for Popup: https://www.youtube.com/watch?v=fn5OlqQuOCk
        //change dimensions of this activity to 80% of screen size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.80), (int) (height * 0.70));


        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID); //default is invalid widget id
        }


        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        //Radio Buttons
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.widget_radio_group);
        if (radioGroup != null) {
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mCursorRowId = checkedId; //sets the Button ID to be the same as Database
                }
            });
        }

        //get the list of stocks from Database

        //data should have the entire contents of the quote Cursor Database in it
        Uri stockQuoteUri = QuoteProvider.Quotes.CONTENT_URI;
        final Cursor data = getContentResolver().query(stockQuoteUri, //uri
                STOCK_QUOTE_COLUMNS, //projection
                null,
                null,
                QuoteColumns.SYMBOL + " ASC"); //sort order

        if (data != null) {
            while (data.moveToNext()) {//do a loop of all cursor items
                int cursorRow = data.getPosition(); //get the row id of cursor

                RadioButton radioButton = new RadioButton(getApplicationContext());
                radioButton.setText(data.getString(COL_STOCK_SYMBOL));
                radioButton.setContentDescription(data.getString(COL_STOCK_NAME));
                radioButton.setId(cursorRow); //use the ID from the
                radioGroup.addView(radioButton);
            }
        }

        //Get the Make widget Button
        Button button = (Button) findViewById(R.id.widget_configure_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Toast.makeText(getBaseContext(), "Button Clicked"
                //        , Toast.LENGTH_SHORT).show();

                //pass the info to Shared Pref
                //Pass the Symbol into Shared Pref
                //find out which row the stock symbol is on - Just get the Cursor Row id NOT the db _ID
                //
                data.moveToPosition(mCursorRowId);
                String widgetStockSymbol = data.getString(COL_STOCK_SYMBOL);
                //put widget ID and symbol into sharedPreferences
                WidgetUtils.addWidgetIdToSharedPrefs(getApplicationContext(), mAppWidgetId, data.getString(COL_STOCK_SYMBOL));
                //use these to look up widget details in Widget Intent Service


                //It is the responsibility of the configuration Activity to request
                // an update from the AppWidgetManager when the App Widget is first
                // created. However, onUpdate() will be called for subsequent
                // updates—it is only skipped the first time.
                //So call the update Here

                //start the intent service to update widget
                getApplicationContext().startService(new Intent(getApplicationContext(), StockQuoteWidgetIntentService.class));


                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                data.close(); //ensure that the cursor is closed before leaving the Configure Activity

                finish();
            }
        });
    }
}
