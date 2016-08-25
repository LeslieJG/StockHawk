package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 * Credit to skyfishjy gist:
 * https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private static Context mContext;
    private static Typeface robotoLight;
    private boolean isPercent;

    private final static String LOG_TAG = QuoteCursorAdapter.class.getSimpleName();


    public QuoteCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_quote, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        String stockSymbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
        String stockName = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME));
        viewHolder.symbol.setText(stockSymbol);
        viewHolder.symbol.setContentDescription(stockName);

        String stockBidPrice = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
        viewHolder.bidPrice.setText(stockBidPrice);
        viewHolder.bidPrice.setContentDescription(mContext.getString(R.string.bid_price_content_description, stockBidPrice));

        int sdk = Build.VERSION.SDK_INT;
        if (cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            } else {
                viewHolder.change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else {
                viewHolder.change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }
        if (Utils.showPercent) {
            String stockPercentChange = cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            viewHolder.change.setText(stockPercentChange);
            viewHolder.change.setContentDescription(mContext
                    .getString(R.string.percent_change_content_description
                            , stockPercentChange
                            , stockChangeUpOrDown(stockPercentChange)));


        } else {

            String stockChange = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
            viewHolder.change.setText(stockChange);
            viewHolder.change.setContentDescription(mContext
                    .getString(R.string.stock_change_content_description
                            , stockChange
                            , stockChangeUpOrDown(stockChange)));
        }
    }

    @Override
    public void onItemDismiss(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        //delete the stock from Quotes table
        mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
        Log.v(LOG_TAG, "LJG Deleted "+ symbol + " from Quotes table");
        //delete the stcok from Histories table


        mContext.getContentResolver().delete(QuoteProvider.Histories.withSymbol(symbol), null, null);

       //TODO Delete below lines
        //Test that it has been deleted
        Uri uriForSymbol = QuoteProvider.Histories.withSymbol(symbol);
        Cursor stockHistoryCursor = mContext.getContentResolver().query(
                uriForSymbol //Uri
                , null //projection (columns to return) (use nyll for no projection)
                , null // //selection Clause
                , null//selection Arguments
                , null); //poosibly have sort order date ascending


        Log.v(LOG_TAG, "LJG Deleted "+ symbol + " from Histories table"
                + " There are " + stockHistoryCursor.getCount() + " Entries still in Histories Table for this stock");
        stockHistoryCursor.close();

        //Test Entire Database TODO Delete this
       Utils.reportNumberOfRowsInHistoriesDatabase(mContext);




        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView symbol;
        public final TextView bidPrice;
        public final TextView change;


        public ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            symbol.setTypeface(robotoLight);
            bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            change = (TextView) itemView.findViewById(R.id.change);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {

        }
    }

    /*
    Private helper method to determine if the stock change displayed is a positive
    number
    This is used to set the content description
     */
    private String stockChangeUpOrDown(String number) {
        return (number.contains("+"))
                ? mContext.getString(R.string.stock_value_up)
                : mContext.getString(R.string.stock_value_down);
    }

}
