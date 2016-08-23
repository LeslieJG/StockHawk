package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Leslie on 2016-08-22.
 */
public class StockHistoryColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String SYMBOL = "symbol_history";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String CLOSEPRICE = "close_price_histoty";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DATE = "date_history";

/*

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String PERCENT_CHANGE = "percent_change";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String NAME = "name";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String CHANGE = "change";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String BIDPRICE = "bid_price";
    @DataType(DataType.Type.TEXT)
    public static final String CREATED = "created";
    @DataType(DataType.Type.INTEGER) @NotNull
    public static final String ISUP = "is_up";
    @DataType(DataType.Type.INTEGER) @NotNull
    public static final String ISCURRENT = "is_current";
*/


}