package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.StockHistoryColumns;
import com.sam_chordas.android.stockhawk.library_helper.MyMarkerView;
import com.sam_chordas.android.stockhawk.library_helper.TestFormatter;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Used to display the stock history
 * Uses MPAndroidChart Library for Linegraph display
 * <p>
 * <p>
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    private static final String STOCK_SYMBOL = "stock_symbol_param";
    private String stockSymbolName;

    private StockHistoryReceiver stockHistoryReceiver; //Broadcast Receiver to receive updates for stock history
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private LineChart stockHistoryLineChart;
    String mEarliestDateInStockHistory = null;
    TestFormatter mStockHistoryDateAxisFormatter = null;

    /////////////////////Database projection constants///////////////
    //For making good use of database Projections specify the columns we need
    private static final String[] STOCK_HISTORY_COLUMNS = {
            StockHistoryColumns.DATE,
            StockHistoryColumns.CLOSEPRICE,
    };

    // These indices are tied to STOCK_HISTORY_COLUMNS.  If STOCK_HISTORY_COLUMNS changes, these must change.
    static final int COL_STOCK_HISTORY_DATE = 0;
    static final int COL_STOCK_HISTORY_CLOSEPRICE = 1;
    /////////////////////////////////////////////////////////

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stockSymbolName Parameter 1.
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance(String stockSymbolName) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(STOCK_SYMBOL, stockSymbolName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockSymbolName = getArguments().getString(STOCK_SYMBOL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_detail, container, false);

        //assign the line chart
        stockHistoryLineChart = (LineChart) fragmentView.findViewById(R.id.stock_history_line_chart);
        updateLineChart(stockHistoryLineChart, stockSymbolName);

        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        //dynamically register Stock History Broadcast Receiver
        if (stockHistoryReceiver == null) {
            stockHistoryReceiver = new StockHistoryReceiver(); //make a new one
        }
        //either way reigister the receiver
        IntentFilter intentFilter = new IntentFilter(getString(R.string.stock_history_received_intent_key));
        getContext().registerReceiver(stockHistoryReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        //Dynmaically Unregister the Stock History Broadcast Receiver
        if (stockHistoryReceiver != null) getContext().unregisterReceiver(stockHistoryReceiver);
        super.onPause();
    }


    /*
    Method to access database and update the linechart.
    Starts the AsyncTask - update is off UI thread
     */
    private void updateLineChart(LineChart stockLineChart, String stockSymbol) {
        new updateLineChartTask().execute(stockLineChart, stockSymbol);
    }


    /**
     * class to do updating line chart off the main UI thread
     * <p/>
     * <p>
     * params are LineChart, String Stockname
     */
    private class updateLineChartTask extends AsyncTask<Object, Void, LineData> {
        final String LOG_TAG = updateLineChartTask.class.getSimpleName();

        @Override
        protected LineData doInBackground(Object... params) {
            if (params.length == 0) //no LineChart passed in
            {
                // Log.e(LOG_TAG, "No LineChart passed in");
                return null;
            }

            //LineChart stockHistoryLineChart = (LineChart) params[0]; //get the LineChart from the input parameters
            String stockSymbol = (String) params[1];

            Cursor stockHistoryCursor;
            Context mContext = getContext();

            Uri uriForSymbol = QuoteProvider.Histories.withSymbol(stockSymbol);
            stockHistoryCursor = mContext.getContentResolver().query(
                    uriForSymbol //Uri
                    , STOCK_HISTORY_COLUMNS //projection (columns to return) (use null for no projection)
                    , null // //selection Clause
                    , null//selection Arguments
                    , null); //poosibly have sort order date ascending

            List<Entry> stockHistoryDataEntries = new ArrayList<Entry>(); //list entries for stock for graphing

            //Test the response
            if (!stockHistoryCursor.moveToFirst()) {
                //Log.v(LOG_TAG, "Database test Cursor is empty!");
            } else {
                int cursorCount = stockHistoryCursor.getCount();
                // Log.v(LOG_TAG, "Database test cursor is valid and count is " + cursorCount);

                stockHistoryCursor.moveToFirst();     //get the first date for setting up x axis
                mEarliestDateInStockHistory = stockHistoryCursor.getString(COL_STOCK_HISTORY_DATE);
                //Log.v(LOG_TAG, "Earliest date in cursor history is " + mEarliestDateInStockHistory);

                do { //Loop through all data from cursor
                    String stockHistoryDate = stockHistoryCursor.getString(COL_STOCK_HISTORY_DATE);
                    String stockHistoryClosePrice = stockHistoryCursor.getString(COL_STOCK_HISTORY_CLOSEPRICE);

                    //convert the Strings to date difference from first date - so graph starts at x=0
                    int dateDiff = Utils.numberOfDaysSinceFirstDate(mEarliestDateInStockHistory, stockHistoryDate);

                    //make up new graphing points
                    Float xValue = (float) dateDiff;
                    Float yValue = Float.valueOf(stockHistoryClosePrice);

                    Entry stockGraphEntry = new Entry(xValue, yValue); //x,y
                    stockHistoryDataEntries.add(stockGraphEntry);
                }
                while (stockHistoryCursor.moveToNext()); //move to next row in cursor if there is one
            }

            //Make a full Line Data set with the list and a String to describe the
            //dataset (and to use as label)
            LineDataSet setCompany1 = new LineDataSet(stockHistoryDataEntries, stockSymbol);
            setCompany1.setAxisDependency(YAxis.AxisDependency.LEFT);
            // By calling setAxisDependency(...), the axis the
            // DataSet should be plotted against is specified.

            //Now we put all the datasets (lines) that we want on our chart
            //into a list of IDataSets
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setCompany1); //could add the other companies too here

            //now put it all into the final Chart data
            LineData data = new LineData(dataSets); //gathers all data together

            stockHistoryCursor.close(); //close cursor before leaving method
            //for AsyncTask
            return data;
        }


        //this is called on UI thread
        @Override
        protected void onPostExecute(LineData lineData) {
            super.onPostExecute(lineData);
            //Log.v(LOG_TAG, "onPostExecute - The earliest date in stock history is " + mEarliestDateInStockHistory);

            stockHistoryLineChart.setData(lineData); //puts all the data into chart

            //Style the chart
            // stockHistoryLineChart.setBackgroundColor(Color.WHITE); //sets background colour
            stockHistoryLineChart.setDescription(stockSymbolName + " stock history for the past year");//Sets the Chart Description
            stockHistoryLineChart.setDescriptionColor(Color.YELLOW); //sets the graph description colour
            stockHistoryLineChart.setDescriptionTextSize(16f); //sets size of Description from 6f to 16f
            stockHistoryLineChart.setNoDataTextDescription("No Stock History");

            //setting the MarkerView
            Log.v(LOG_TAG, "just before MyMarkerView Declared");
            MyMarkerView markView = new MyMarkerView(getContext(), R.layout.marker_view_layout, mEarliestDateInStockHistory);
            Log.v(LOG_TAG, "Just after MyMarkerView Declared");
            stockHistoryLineChart.setMarkerView(markView);

            //Style the Axis
            YAxis leftAxis = stockHistoryLineChart.getAxisLeft();
            leftAxis.setTextColor(Color.WHITE);

            YAxis rightAxis = stockHistoryLineChart.getAxisRight();
            rightAxis.setTextColor(Color.WHITE);

            XAxis xAxis = stockHistoryLineChart.getXAxis();
            xAxis.setTextColor(Color.WHITE);

            xAxis.setGranularity(1f);//do not make any more lines that one per date
            xAxis.setLabelCount(3); //do not show more than 3 (ish) label lines for x Axis - stops dates overlapping

            //reset the xaxis formatter with the new date if possible
            // Log.v(LOG_TAG, "Just before declaring new TestFormatter. Earliest date is " + mEarliestDateInStockHistory);
            mStockHistoryDateAxisFormatter = new TestFormatter(stockHistoryLineChart, mEarliestDateInStockHistory);

            //set the formatter
            stockHistoryLineChart.getXAxis().setValueFormatter(mStockHistoryDateAxisFormatter);

            // stockHistoryLineChart.setAutoScaleMinMaxEnabled(true); //allow y-axis to change scale to allow financial data left/right scrolling to change the scale as needed
            stockHistoryLineChart.invalidate(); //redraws chart
        }
    }


    /*
        Receives call that API call is done for stock history
        Then updates the stock history
        Credit: http://stackoverflow.com/users/574859/maximumgoat
        from this thread http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
     */
    private class StockHistoryReceiver extends BroadcastReceiver {
        private final String LOG_TAG = StockHistoryReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.stock_history_received_intent_key))) {
                updateLineChart(stockHistoryLineChart, stockSymbolName); //update linechart with new database info

            }
        }
    }
}
