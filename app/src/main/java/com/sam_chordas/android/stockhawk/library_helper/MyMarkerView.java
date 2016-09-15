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
 * To show the selected stock price and date in a popout window
 * In MPAndroidChart used for stock history
 */
public class MyMarkerView extends MarkerView {
    private TextView tvStockDate;
    private TextView tvStockPrice;
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
        tvStockDate = (TextView) findViewById(R.id.tvStockDate);
        tvStockPrice = (TextView) findViewById(R.id.tvStockPrice);

        this.startDate = startDate;

    }


    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        //Calculate Date
        int dateOffset = (int) e.getX();
        String currentDate = Utils.getDateOffset(startDate, dateOffset);
        tvStockDate.setText(currentDate);
        // tvStockPrice.setText("$" + e.getY());
        //format price to 2 decimal places only
        tvStockPrice.setText(getContext().getString(R.string.stock_history_marker_stock_price, String.format("%.2f", e.getY())));
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
