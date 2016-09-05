package com.sam_chordas.android.stockhawk.library_helper;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by Leslie on 2016-09-06.
 * To show the selected stock price and date
 *
 */
public class MyMarkerView extends MarkerView {
    private TextView tvContent;
    private String startDate;


    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public MyMarkerView(Context context, int layoutResource, String startDate) {
        super(context, layoutResource);

        // this markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tvContent);
        this.startDate = startDate;

    }






    @Override
    public void refreshContent(Entry e, Highlight highlight) {
       // tvContent.setText("BiteMe");

        //Calculate Date
        int dateOffset = (int) e.getX();
        String currentDate = Utils.getDateOffset(startDate, dateOffset);

        tvContent.setText(currentDate +" $" + e.getY()); // set the entry-value as the display text
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }
}

/*

import android.content.Context;
        import android.widget.TextView;

        import com.github.mikephil.charting.components.MarkerView;
        import com.github.mikephil.charting.data.Entry;
        import com.github.mikephil.charting.highlight.Highlight;
        import com.tank.water.level.R;

        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Locale;

public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    private long startDateTimestamp;

    public MyMarkerView (Context context, int layoutResource, long startDateTimestamp) {
        super(context, layoutResource);
        // this markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tvContent);
        this.startDateTimestamp = startDateTimestamp;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        long currentTimestamp = (int)e.getX() + startDateTimestamp;

        tvContent.setText(e.getY() + "% at " + getTimedate(currentTimestamp)); // set the entry-value as the display text
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }

    private String getTimedate(long timeStamp){

        try{
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date netDate = (new Date(timeStamp*1000));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "xx";
        }
    }}
*/
