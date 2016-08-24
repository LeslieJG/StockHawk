package com.sam_chordas.android.stockhawk.ui;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
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
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // private Context mContext;
    private StockHistoryReceiver stockHistoryReceiver; //Broadcast Receiver to receive updates for stock history
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private LineChart stockHistoryLineChart;


    ////////////**** Default Fragment Stuff ///////////////////////// - can delete if not used
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String stockSymbolName;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param stockSymbolName Parameter 1.
     *                        param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String stockSymbolName) { //, String param2
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, stockSymbolName);
        // args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockSymbolName = getArguments().getString(ARG_PARAM1);
            //  mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View fragmentView = inflater.inflate(R.layout.fragment_detail, container, false);

        //TextView (to erase?)
        //  TextView stockStymbolTextView = (TextView) fragmentView.findViewById(R.id.detail_fragment_stock_symbol);
        // stockStymbolTextView.setText("The stock symbol name is " + stockSymbolName);

        stockHistoryLineChart = (LineChart) fragmentView.findViewById(R.id.stock_history_line_chart);


        ///////////////Try putting dummy points into database then retrieving them//////////////
    ///////////////////PUtting data into database ///////////////////////////////////////////////////////

        ///Dummy points for database
        String stockSymbol = "TestStock";
        String date4 = "2015-03-25";
        String date3 = "2015-03-24";
        String date2 = "2015-03-23";
        String date1 = "2015-03-22";


        String close1 = "42.700001";
        String close2 = "40.100009";
        String close3 = "28.123456";
        String close4 = "11.123456";

        //Make a method to quote days from first date - i.e. first date is zero
        //next date is 1 etc
        //to make nice x values for graph

        //Make Content Values
        ContentValues value1 = Utils.makeStockHistoryContentValue(stockSymbol, date1, close1);
        ContentValues value2 = Utils.makeStockHistoryContentValue(stockSymbol, date2, close2);
        ContentValues value3 = Utils.makeStockHistoryContentValue(stockSymbol, date3, close3);
        ContentValues value4 = Utils.makeStockHistoryContentValue(stockSymbol, date4, close4);

        /*ContentValues value1 = new ContentValues();
        value1.put(StockHistoryColumns.SYMBOL, stockSymbol);
        value1.put(StockHistoryColumns.DATE, date1);
        value1.put(StockHistoryColumns.CLOSEPRICE, close1);

        ContentValues value2 = new ContentValues();
        value2.put(StockHistoryColumns.SYMBOL, stockSymbol);
        value2.put(StockHistoryColumns.DATE, date2);
        value2.put(StockHistoryColumns.CLOSEPRICE, close2);

        ContentValues value3 = new ContentValues();
        value3.put(StockHistoryColumns.SYMBOL, stockSymbol);
        value3.put(StockHistoryColumns.DATE, date3);
        value3.put(StockHistoryColumns.CLOSEPRICE, close3);

        ContentValues value4 = new ContentValues();
        value4.put(StockHistoryColumns.SYMBOL, stockSymbol);
        value4.put(StockHistoryColumns.DATE, date4);
        value4.put(StockHistoryColumns.CLOSEPRICE, close4);
*/

        //Erase the database for my test
        Context mContext = getContext();
        mContext.getContentResolver().delete(QuoteProvider.Histories.CONTENT_URI, null, null);

        //put into database - This will get moved off UI thread!
        Uri stockHistoryUri = QuoteProvider.Histories.CONTENT_URI;


        //Put values into database
        mContext.getContentResolver().insert(stockHistoryUri, value1);
        mContext.getContentResolver().insert(stockHistoryUri, value2);
        mContext.getContentResolver().insert(stockHistoryUri, value3);
        mContext.getContentResolver().insert(stockHistoryUri, value4);

    ///   ////////////////////////////////getting Data OUT of database//////////////////////////////////////////////////

        //Test getting value out of database
        String value1FromDatabase;
        Cursor databaseTestCursor;

        Uri uriForSymbol = QuoteProvider.Histories.withSymbol(stockSymbol);
        databaseTestCursor = mContext.getContentResolver().query(uriForSymbol
                , null
                , null
                , null
                , null); //poosibly have sort order date ascending


        //Test the response
        if (!databaseTestCursor.moveToFirst()) {
            Log.v(LOG_TAG, "Database test Cursor is empty!");
        } else {
            int cursorCount = databaseTestCursor.getCount();
            Log.v(LOG_TAG, "Database test cursor is valid and count is " + cursorCount);


            //get the first date for setting up x axis
            databaseTestCursor.moveToFirst();
            String earliestDateInHistory =  databaseTestCursor.getString(databaseTestCursor.getColumnIndex(StockHistoryColumns.DATE));


            //Loop through all data from cursor
            for (int i = 0; i < cursorCount; i++) {

                String testcursorname =
                        databaseTestCursor.getString(databaseTestCursor.getColumnIndex(StockHistoryColumns.SYMBOL));
                String testcursorDate =
                        databaseTestCursor.getString(databaseTestCursor.getColumnIndex(StockHistoryColumns.DATE));
                String testCursorClose =
                        databaseTestCursor.getString(databaseTestCursor.getColumnIndex(StockHistoryColumns.CLOSEPRICE));
                String testCursorID =
                        databaseTestCursor.getString(databaseTestCursor.getColumnIndex(StockHistoryColumns._ID));


                //Loop through database table for all items and put them in log statement

                //convert the Strings to dates
                int dateDiff = Utils.numberOfDaysSinceFirstDate(earliestDateInHistory, testcursorDate);

               // int newDate = Utils.getDateDiff(earliestDateInHistory, testcursorDate);



                Log.v(LOG_TAG, "Database read is _ID:" + testCursorID
                        + " Symbol:" + testcursorname
                        + " Date:" + testcursorDate
                        + " Date diff:" + dateDiff
                        + " ClosePrice:" + testCursorClose);



              //  Log.v(LOG_TAG, "Data to put into graph is - Date: " + " ");


                databaseTestCursor.moveToNext(); //move to next item

            }

        }

/*
        QuoteProvider.Quotes.CONTENT_URI, //table name
                new String[]{"Distinct " + QuoteColumns.SYMBOL}, //projection (columns to return)
                null, //selection Clause
                null, //selection Arguments
                null); //sort order
*/




        ////////////////////////////////////Dummy Points to make up //////////////////

        //Data point Entry
        Entry point1 = new Entry(10f, 5f); //x,y
        Entry point2 = new Entry(12f, 10f);
        Entry point3 = new Entry(15f, 11f);
        Entry point4 = new Entry(16f, 19f);

        //Data pointEntry with dates


//            String dateString = "30/09/2014";
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        //TODO LJG Leslie start here tomorrow!!!!!! - WOrking on converting date and displaying it in Linegraph
/*

            String dateString = "2015-03-25";
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Date date = null;
            try {
                date = sdf.parse(dateString);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            long startDate = date.getTime();
            Log.v(LOG_TAG, "The test date as long is " + stockSymbol.toString());

*/


      //  float point1Date =  Float.parseFloat("2015-03-25");
      /*      float point1Date =  startDate;
        float point1ClosePrice = Float.parseFloat("42.700001");
        Entry point1 = new Entry(0f, 5f); //x,y
*/
       // Entry point1 = new Entry(0f, 5f); //x,y
     //   Entry point2 = new Entry(2f, 10f);
     //   Entry point3 = new Entry(5f, 11f);
     //   Entry point4 = new Entry(6f, 19f);


        //List containing all the entries for one line
        List<Entry> valsCompany1 = new ArrayList<Entry>();
        valsCompany1.add(point1);
        valsCompany1.add(point2);
        valsCompany1.add(point3);
        valsCompany1.add(point4);

        //Make a full Line Data set with the list and a String to descripe the
        //dataset (and to use as label)
        LineDataSet setCompany1 = new LineDataSet(valsCompany1, "Company 1");
        setCompany1.setAxisDependency(YAxis.AxisDependency.LEFT);
        // By calling setAxisDependency(...), the axis the
        // DataSet should be plotted against is specified.

        //Now we put all the datasets (lines) that we want on our chart
        //into a list of IDataSets
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setCompany1); //could add the other companies too here

        //now put it all into the final Chart data
        LineData data = new LineData(dataSets); //gathers all data together

        //put all data into line chart

        stockHistoryLineChart.setData(data); //puts all the data into chart

        //Style the chart
        // stockHistoryLineChart.setBackgroundColor(Color.WHITE); //sets background colour
        stockHistoryLineChart.setDescription(stockSymbolName + "   stock history for the past year");//Sets the Chart Description
        stockHistoryLineChart.setDescriptionColor(Color.YELLOW); //sets the graph description colour
        stockHistoryLineChart.setDescriptionTextSize(16f); //sets size of Description from 6f to 16f
        stockHistoryLineChart.setNoDataTextDescription("No Stock History");

        //Style the Axis
        YAxis leftAxis = stockHistoryLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = stockHistoryLineChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);

        XAxis xAxis = stockHistoryLineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);

        // Limi

        // stockHistoryLineChart.


        stockHistoryLineChart.invalidate(); //redraws chart


        // return inflater.inflate(R.layout.fragment_detail, container, false);
        return fragmentView;


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
      /*  if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    ///////////////////////////////End of Default Fragment stuff


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
         //LJG before returning result let the SwipreRefresh know that the refresh is done
            Receives call that API call is done
            Used to let UI refresh symbol know that refresh is done now.
            Credit: http://stackoverflow.com/users/574859/maximumgoat
            from this thread http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
         */
    private class StockHistoryReceiver extends BroadcastReceiver {
        private final String LOG_TAG = StockHistoryReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.stock_history_received_intent_key))) {
                // Do stuff - maybe update my view based on the changed DB contents

                Log.v(LOG_TAG, "LJG StockHistoryUpdate Received");
                //mSwipeLayout.setRefreshing(false);
                // stopRefresh();


            }
        }
    }
}
