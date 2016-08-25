package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;

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


            // Create the detail fragment and add it to the activity using a fragment transaction.

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


            //Start Downloading Stock History from API at the same time as starting the fragment
            //so they happen in parallel
            //TODO Put this into MyStocksActivity Instead???
            //Now in MyStocksActivity in Click Listener
            /*
            Intent stockHistoryIntent = new Intent(this, StockHistoryIntentService.class);
            stockHistoryIntent.putExtra(DetailActivity.STOCK_SYMBOL_DETAIL_TAG, stockSymbolName); //pass the IntentService name of stock symbol
            this.startService(stockHistoryIntent);
            */


        }
    }


}
