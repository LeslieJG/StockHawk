package com.sam_chordas.android.stockhawk.data;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Leslie on 2016-08-19.
 *
 * Trying to test all columns of Database to ensure that I can add columns to Schematic Database
 */
public class DatabaseTester {



    public static void testColumns(Context context){

        Cursor testCursor;

        testCursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                null, null);






    }







}
