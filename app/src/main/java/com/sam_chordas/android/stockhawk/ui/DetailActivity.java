package com.sam_chordas.android.stockhawk.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockHistoryColumns;
import com.sam_chordas.android.stockhawk.service.StockHistoryIntentService;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String STOCK_SYMBOL_DETAIL_TAG = "Stock Symbol Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Log.v(LOG_TAG, "LJG Detail Activity - onCreate");

        if (savedInstanceState == null) {

            //get the stock sybmol name
            String stockSymbolName = getIntent().getStringExtra(STOCK_SYMBOL_DETAIL_TAG);
            Log.v(LOG_TAG, "LJG onCreate - the stock symbol received in DetailActivity is " + stockSymbolName);


            // Create the detail fragment and add it to the activity
            // using a fragment transaction.


            Bundle arguments = new Bundle();
            // arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());
            //  arguments.putBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, true);


            DetailFragment detailFragment = DetailFragment.newInstance(stockSymbolName);


            // DetailFragment fragment = new DetailFragment();
            //  fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    //  .add(R.id.detail_fragment_container, fragment)
                    .add(R.id.detail_fragment_container, detailFragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();


            ///////////////////////****************************///////////////////////////////////
            //This is where my testing for stock history starts

            //LJG Test Stock History Table
            Context mContext = this;

            //make data values
            ContentValues contentValues = new ContentValues();

            contentValues.put(StockHistoryColumns.DATE, "2001-09-11");
            contentValues.put(StockHistoryColumns.CLOSEPRICE, "100");
            contentValues.put(StockHistoryColumns.SYMBOL, "FYOUTOO");

            //Uri testUri = QuoteProvider.Histories.CONTENT_URI;

            Uri testUri = QuoteProvider.Histories.CONTENT_URI;
            Log.v(LOG_TAG, "Test URI is " + testUri);

             mContext.getContentResolver().insert(QuoteProvider.Histories.CONTENT_URI,
                    contentValues);
            mContext.getContentResolver().insert(testUri, contentValues);
            Log.v(LOG_TAG, "Inerted test Stock into history");


            //LJG or try batch insert
          /*  ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                    QuoteProvider.Histories.CONTENT_URI);
            builder.withValue(StockHistoryColumns.DATE, "2001-09-11");
            builder.withValue(StockHistoryColumns.CLOSEPRICE, "100");
            builder.withValue(StockHistoryColumns.SYMBOL, "FYOUTOO");
            batchOperations.add(builder.build());


            try {
                this.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, batchOperations);
            } catch (RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, "Error applying batch insert", e);

            }

*/

            //Now test retrieving stock from history
            Uri databaseQuery = QuoteProvider.Histories.CONTENT_URI;
            Cursor stocksFromDbCursor = mContext.getContentResolver().query(databaseQuery ,null,null,null,null);
            stocksFromDbCursor.moveToFirst();
            String stockDateFromDB;
            stockDateFromDB = stocksFromDbCursor.getString(stocksFromDbCursor.getColumnIndex(StockHistoryColumns.DATE));
           // Log.v(LOG_TAG, "I got the date back from DB! it is " + stockDateFromDB);



            //Testing Yahoo API Quaery
            Intent stockHistoryIntent = new Intent(this, StockHistoryIntentService.class);
            this.startService(stockHistoryIntent);
            //This works




        }
    }


}
