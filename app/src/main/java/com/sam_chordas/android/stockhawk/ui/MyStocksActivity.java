package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockHistoryIntentService;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    private boolean isConnected;
    SwipeRefreshLayout mSwipeLayout;
    private DataUpdateReceiver dataUpdateReceiver; //for getting updates From StockTaskService that API call done
    private final String LOG_TAG = MyStocksActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        isConnected = checkInternetConnected();
        setContentView(R.layout.activity_my_stocks);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra(getString(R.string.intent_tag),
                    getString(R.string.intent_init));
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                networkToast();
            }
        }


        //TODO: Deal with Swipe Layout
        //For Swipe Layout
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.stock_swipe_container);
        mSwipeLayout.setOnRefreshListener(this);


        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        //TODO: LJG do something on item click
                   //     Log.v(LOG_TAG, "Item Clicked");

                        Cursor cursor = mCursorAdapter.getCursor(); //Get the cursor with all the data in it
                        cursor.moveToPosition(position); //move cursor to correct position
                        //get Stcok symbol
                        String stockSymbolClicked = cursor
                                .getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));

                        //change tp upper case for nicer UI experience
                        stockSymbolClicked = stockSymbolClicked.toUpperCase();

                        Log.v(LOG_TAG, "LJG Click listener - stock symbol is " + stockSymbolClicked );


                        //pass the stock symbol to detail activity to let it know what to look up

                        //start an API call for historic data

                        //Make new database for historic data



                        //ensure that the data is loaded into DB



                        //Launch the StockHistoryIntentService to update the Stock Histories into Database

                        //Start Downloading Stock History from API at the same time as starting the fragment
                        //so they happen in parallel
                        //TODO Put this into MyStocksActivity Instead???
                        Intent stockHistoryIntent = new Intent(getApplicationContext(), StockHistoryIntentService.class);
                        stockHistoryIntent.putExtra(DetailActivity.STOCK_SYMBOL_DETAIL_TAG, stockSymbolClicked); //pass the IntentService name of stock symbol
                        getApplicationContext().startService(stockHistoryIntent);




                        //Launch the Detail Activity with explicit intent
                       // Intent detailActivityIntent = new Intent(this, DetailActivity.class);
                        Intent detailActivityIntent = new Intent(getApplicationContext() , DetailActivity.class);
                        detailActivityIntent.putExtra(DetailActivity.STOCK_SYMBOL_DETAIL_TAG, stockSymbolClicked);
                        startActivity(detailActivityIntent);





                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    if (isConnected){
                if (checkInternetConnected()) { //connectivity may have changed since app started - check if connected NOW
                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{input.toString()}, null);

                                    try {
                                        if (c.getCount() != 0) {
                                            Toast toast =
                                                    Toast.makeText(MyStocksActivity.this, R.string.stock_already_exists_toast,
                                                            Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                            toast.show();
                                            return;
                                        } else {
                                            // Add the stock to DB
                                            mServiceIntent.putExtra(getString(R.string.intent_tag), getString(R.string.intent_add));
                                            mServiceIntent.putExtra(getString(R.string.intent_symbol), input.toString());
                                            startService(mServiceIntent);
                                        }
                                    } finally {
                                        c.close(); //close cursor
                                    }

                                }
                            })
                            .show();
                } else {
                    networkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();
        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = getString(R.string.service_periodic);

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    @Override
    protected void onPause() {
        if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver); //for indicating stock refresh

        Log.v(LOG_TAG, "LJG onPause");
        super.onPause();

    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

        Log.v(LOG_TAG, "LJG onResume");
        //LJG ensure Dataupdate Receiver is available
        if (dataUpdateReceiver == null) { //for indicating stock refresh
            dataUpdateReceiver = new DataUpdateReceiver();
        //    Log.v(LOG_TAG, "LJG onResume made new DataUpdateReceiver");
        }

       // IntentFilter intentFilter = new IntentFilter(REFRESH_DATA_INTENT);
        IntentFilter intentFilter = new IntentFilter(getString(R.string.refresh_data_intent_key));
        registerReceiver(dataUpdateReceiver, intentFilter);

        //LJG quick network check for better user experience
        if (!checkInternetConnected()) {
            networkToast();
        }

    }

    /*
    To show user no network connection
     */
    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    public void refreshFailedToast() {
        Toast.makeText(mContext, R.string.can_not_refresh_network_toast, Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    LJG This is VERY important - Add the column names from database HERE
    TODO: Put new database columns here
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
              //  new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                       // QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL,QuoteColumns.NAME, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP },
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    /*
    FOr the swipe Refresh
     */
    @Override
    public void onRefresh() {
        Log.v(LOG_TAG, "Swipe Refresh");
        updateDbfromAPi();
    }

    //stops the refreshing display
    private void stopRefresh() {
        if (mSwipeLayout != null) mSwipeLayout.setRefreshing(false);
    }


    /*
     //LJG before returning result let the SwipreRefresh know that the refresh is done
        Receives call that API call is done
        Used to let UI refresh symbol know that refresh is done now.
        Credit: http://stackoverflow.com/users/574859/maximumgoat
        from this thread http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
     */
    private class DataUpdateReceiver extends BroadcastReceiver {
        private final String LOG_TAG = DataUpdateReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(getString(R.string.refresh_data_intent_key))) {
                // Do stuff - maybe update my view based on the changed DB contents

                Log.v(LOG_TAG, "LJG API call done, data updated");
                //mSwipeLayout.setRefreshing(false);
                stopRefresh();
            }
        }
    }

    /*
        Helper method to check for internet connectivity
         */
    private boolean checkInternetConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


    /*
    Updates the database from API
     */
    private void updateDbfromAPi() {
        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);

       // mServiceIntent.putExtra("tag", getString(R.string.intent_value_init));//could also use "periodic" either will work
        mServiceIntent.putExtra(getString(R.string.intent_tag), getString(R.string.intent_init));//could also use "periodic" either will work

        // if (isConnected) {
        if (checkInternetConnected()) {
            startService(mServiceIntent);
        } else {
            //networkToast();
            refreshFailedToast();
            stopRefresh();
        }
    }

}
