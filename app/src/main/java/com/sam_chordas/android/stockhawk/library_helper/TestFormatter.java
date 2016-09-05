package com.sam_chordas.android.stockhawk.library_helper;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

/**
 * Created by Leslie on 2016-09-05.
 */
public class TestFormatter implements AxisValueFormatter {

    //Can create a sonstructor that passes all the data in if needed
    //or at least the first date of the graph, so the other dates can be created




    //Value represents the actual number graphed (in my case it starts with 0 and goes up)
    @Override
    public String getFormattedValue(float value, AxisBase axis) {

       //Return the value to be displayed instead of value
      //  return null;
        return "a" + value;
    }




    /** this is only needed if numbers are returned, else return 0 */
    @Override
    public int getDecimalDigits() {
        return 0;
    }
}
