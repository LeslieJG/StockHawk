package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static final String STOCK_SYMBOL_DETAIL_TAG = "Stock Symbol Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            //get the stock sybmol name
            String stockSymbolName = getIntent().getStringExtra(STOCK_SYMBOL_DETAIL_TAG);

            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            DetailFragment detailFragment = DetailFragment.newInstance(stockSymbolName);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_fragment_container, detailFragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }
}
