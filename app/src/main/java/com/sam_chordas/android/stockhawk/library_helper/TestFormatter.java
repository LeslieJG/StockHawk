package com.sam_chordas.android.stockhawk.library_helper;


import android.util.Log;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.text.SimpleDateFormat;

/**
 * Created by Leslie on 2016-09-05.
 * <p/>
 * X-Axis Formatter for stock detail line chart
 */
public class TestFormatter implements AxisValueFormatter {
    String mStartDate = null;
    private static final String LOG_TAG = TestFormatter.class.getSimpleName();

    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MMM-dd");


    BarLineChartBase<?> mLineChart = null; //used for refreencing the chart

    //Constructor
    public TestFormatter(BarLineChartBase<?> lineChartBase, String initialDate) {
        mLineChart = lineChartBase;
        mStartDate = "2001-01-01";
        if (initialDate != null) {
            mStartDate = initialDate;
        }

        Log.v(LOG_TAG, "In Constructor - the initial date passed in is " + initialDate);
    }

    //constructor
    public TestFormatter() {
        mStartDate = "2015-01-01"; //default initial date if needed - if none provided
    }

    public void setInitialDate(String initialDate) {
        mStartDate = initialDate;
    }

    //Value represents the actual number graphed (in my case it starts with 0 and goes up)
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Log.v(LOG_TAG, "BLA  getFormattedValue - the x-value passed in is " + value + " and the start date is " + mStartDate);

        //dislay the values as dates (as strings)
        String dateToReturn = Utils.getDateOffset(mStartDate, (int) value, mFormat);
        ;

        //check how many items displayed
        /*
        Use the below to change the xAxis labels as you zoom in
        Currently not used
         */
       /* if (mLineChart != null && mLineChart.getVisibleXRange() < 30 ) {
           // mLineChart.getXAxis().setGranularity(2f);

            dateToReturn = "30";
        } else {
            // String dateToReturn = Utils.getDateOffset(mStartDate, (int) value);
             dateToReturn = Utils.getDateOffset(mStartDate, (int) value, mFormat);
            // String dateToReturn = String.valueOf(mLineChart.getMaxVisibleCount());

        }*/

        return dateToReturn;
    }


    /**
     * this is only needed if numbers are returned - set to 1, else return 0
     */
    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
