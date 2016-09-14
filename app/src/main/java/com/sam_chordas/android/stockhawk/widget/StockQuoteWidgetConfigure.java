package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Leslie on 2016-09-12.
 * <p>
 * For Handling the One-time configuration of each widget.
 * <p>
 * This allows the users to pick which stock the widget displays,
 * allowing for many widgets each displaying their own stock information.
 * <p>
 * This Activity is created by an intent which passes in the widget ID
 * <p>
 * This Activity must return a result that included with widget ID
 * This Activity should also call the widget's onUpdate() method to update the data for
 * the first time after being created
 * <p>
 * <p>
 * <p>
 * Modelled after https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/appwidget/ExampleAppWidgetConfigure.java
 */
public class StockQuoteWidgetConfigure extends AppCompatActivity {
    private static final String LOG_TAG = StockQuoteWidgetConfigure.class.getSimpleName();
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID; //for storing the widget Id of the widget we are making

    //default constructor
    /*StockQuoteWidgetConfigure(){
       super();
    }
*/

    //TODO: Make readio buttons be symbol list from app
    //Store widget ID and App SYmbol Name as key value pair in pref
    //ensure this key value pair is deleted when widget is deleted!
    //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);


        // Set the view layout resource to use for this configure class
        setContentView(R.layout.widget_configure);

/*

        ////////////////////SPinner/////////////////
        Spinner spinner = (Spinner) findViewById(R.id.widget_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);



        //allow spinner to handle selection events
        spinner.setOnItemSelectedListener(this);
////////////////////////Spinner//////////////////////
*/


        //Radio Buttons
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.widget_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) { //TODO set a memeber variable wit the actual radio button pressed - deal with the widget when final make widget button pressed
                switch (checkedId) {
                    case 99:
                        Log.v(LOG_TAG, "Button 1 pressed");
                        break;
                    case 100:
                        Log.v(LOG_TAG, "Button 2 pressed");
                        break;

                }


            }
        });


        //make a redio button (will be in a loop later on
        RadioButton radioButton = new RadioButton(getApplicationContext());
        radioButton.setText("Button 1");
        //int testID = 99;
        radioButton.setId(99); //use the ID from the database to make a unique ID. THen just look up that ID for populating the database
        //  radioButton.setOnClickListener(onRadioButtonClicked);
        radioGroup.addView(radioButton);


        radioButton = new RadioButton(getApplicationContext());
        radioButton.setText("Button 2");
        // int testID = 99;
        radioButton.setId(100);
        //  radioButton.setOnClickListener(onRadioButtonClicked);
        radioGroup.addView(radioButton);


        //Get the Make widget Button
        Button button = (Button) findViewById(R.id.widget_configure_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                Toast.makeText(getBaseContext(), "Button Clicked"
                        , Toast.LENGTH_SHORT).show();

                //pass the info to Shared Pref
                //or get info from shared pref


                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();

            }
        });


//other stuff here

/*

        // Find the EditText
        mAppWidgetPrefix = (EditText)findViewById(R.id.appwidget_prefix);
        // Bind the action for the save button.
        findViewById(R.id.save_button).setOnClickListener(mOnClickListener);


*/


        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID); //default is invalid widget id
        }


        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        /*
        mAppWidgetPrefix.setText(loadTitlePref(ExampleAppWidgetConfigure.this, mAppWidgetId));
*/

    }

/*


    public void onRadioButtonClicked (View view){
        // Is the button now checked?

        Log.v(LOG_TAG, "Radio Button Checked");
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_pirates:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radio_ninjas:
                if (checked)
                    // Ninjas rule
                    break;
            case 99:
                Log.v(LOG_TAG, "Button 1 ID 99 clicked");
                break;
        }
    }*/
/*

    */
/*
    To Listen for Item Selected in Spinner
     *//*

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(getBaseContext(),"The position clicked is " + position
                , Toast.LENGTH_SHORT).show();

    }

    */
/*
    What to do if nothing selected
     *//*

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
*/


    //other methods like click listers here

    //save this widget ID and the stock symbol to update it with to shared pref, so it can be
    // looked up when the widgets are being updated later on.
}
