package com.sam_chordas.android.stockhawk.library_helper;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Leslie on 2016-08-26.
 *
 * To Format the Stock LineChart showing months and days of year
 *
 * The linechart is actually displayed with  the first date as value 0, and all subsiquent days
 * an integer value of days after that. (i.e. 0,1,2,3,4,7) for Mond-Fri, then Monday again
 *
 *
 */
public class DayMonthYearAxisValueFormatter implements AxisValueFormatter {

    private BarLineChartBase<?> chart;
    private long startDateTimestamp; //timestamp at t=0

    public DayMonthYearAxisValueFormatter(BarLineChartBase<?> chart, long startDateTimestamp) {
        this.chart = chart;
        this.startDateTimestamp = startDateTimestamp;
    }


    // value is the time difference between current x value and x = 0, called timestampDetla
    // populate chart xAxis with timestampDetla instead of whole timestamps. timestampDelta is defined as:
    // timestampDetla = currentValueTimestamp - startDateTimestamp
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long timestampDelta = (long) value;
        // retrieve actual timestamp
        long currentValueTimestamp = startDateTimestamp + timestampDelta;
        // convert timestamp to hour:minute, change getHour() format to show dates if desired
        return getHour(currentValueTimestamp);
    }


/*
        //Original overrid method
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return null;
    }
*/

    @Override
    public int getDecimalDigits() {
        return 0;
    }



    private String getHour(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            Date netDate = (new Date(timeStamp*1000));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }
/*

    package ....;

    import com.github.mikephil.charting.charts.BarLineChartBase;
    import com.github.mikephil.charting.components.AxisBase;
    import com.github.mikephil.charting.formatter.AxisValueFormatter;

    import java.text.DateFormat;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Locale;

    */
/**
     * Created by Yasir on 02/06/16.
     *//*

    public class HourAxisValueFormatter implements AxisValueFormatter
    {

        private BarLineChartBase<?> chart;
        private long startDateTimestamp; //timestamp at t=0

        public HourAxisValueFormatter(BarLineChartBase<?> chart, long startDateTimestamp) {
            this.chart = chart;
            this.startDateTimestamp = startDateTimestamp;
        }


        // value is the time difference between current x value and x = 0, called timestampDetla
        // populate chart xAxis with timestampDetla instead of whole timestamps. timestampDelta is defined as:
        // timestampDetla = currentValueTimestamp - startDateTimestamp
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long timestampDelta = (long) value;
            // retrieve actual timestamp
            long currentValueTimestamp = startDateTimestamp + timestampDelta;
            // convert timestamp to hour:minute, change getHour() format to show dates if desired
            return getHour(currentValueTimestamp);
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }

        private String getHour(long timeStamp){

            try{
                DateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                Date netDate = (new Date(timeStamp*1000));
                return sdf.format(netDate);
            }
            catch(Exception ex){
                return "xx";
            }
        }
    }

*/

}
