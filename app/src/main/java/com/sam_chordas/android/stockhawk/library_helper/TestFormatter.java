package com.sam_chordas.android.stockhawk.library_helper;


import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.text.SimpleDateFormat;

/**
 * Created by Leslie on 2016-09-05.
 */
public class TestFormatter implements AxisValueFormatter {
    String mStartDate = null;

    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MMM-dd");
    // private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MMM");

    BarLineChartBase<?> mLineChart = null; //used for refreencing the chart
    // with the values to be displayed to allow for changing xAxis formatting


    //Can create a sonstructor that passes all the data in if needed
    //or at least the first date of the graph, so the other dates can be created

    //Constructor
    public TestFormatter(BarLineChartBase<?> lineChartBase, String initialDate) {
        mLineChart = lineChartBase;
        mStartDate = initialDate;
    }

    public TestFormatter() {
        mStartDate = "2015-01-01"; //default initial date if needed - if none provided
    }

    public void setInitialDate(String initialDate) {
        mStartDate = initialDate;
    }

    //Value represents the actual number graphed (in my case it starts with 0 and goes up)
    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        //Return the value to be displayed instead of value


        //  return null;
        // return "a" + value;
        // return mStartDate + value;


        //dislay the values as dates (as strings)

        String dateToReturn = Utils.getDateOffset(mStartDate, (int) value, mFormat);;
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


        // return  mStartDate;
    }


    /**
     * this is only needed if numbers are returned, else return 0
     */
    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
