package aifa.assignment3.activities;

import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import aifa.assignment3.R;
import aifa.assignment3.aidl.WeatherData;
import aifa.assignment3.operations.WeatherOps;
import aifa.assignment3.operations.WeatherOpsImpl;
import aifa.assignment3.utils.WeatherDataArrayAdapter;
import aifa.assignment3.utils.RetainedFragmentManager;
import aifa.assignment3.utils.Utils;

/**
 * The main Activity that prompts the user for Acronyms to expand via
 * various implementations of AcronymServiceSync and
 * AcronymServiceAsync and view via the results.  Extends
 * LifecycleLoggingActivity so its lifecycle hook methods are logged
 * automatically.
 */
public class WeatherActivity extends LifecycleLoggingActivity {
    /**
     * Used to retain the ImageOps state between runtime configuration
     * changes.
     */
    protected final RetainedFragmentManager mRetainedFragmentManager =
        new RetainedFragmentManager(this.getFragmentManager(),
                                    TAG);

    /**
     * Provides weather-related operations.
     */
    private WeatherOps weatherOps;

    /**
     * The ListView that will display the results to the user.
     */
    protected ListView mListView;

    /**
     * Acronym entered by the user.
     */
    protected EditText mEditText;

    /**
     * A custom ArrayAdapter used to display the list of AcronymData
     * objects.
     */
    protected WeatherDataArrayAdapter mAdapter;


    private Long lastCallTime;

    private String lastRequest;
    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., runtime
     * configuration changes.
     *
     * @param Bundle object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Get references to the UI components.
        setContentView(R.layout.weather_activity);

        // Store the EditText that holds the urls entered by the user
        // (if any).
        mEditText = (EditText) findViewById(R.id.editText1);

        // Store the ListView for displaying the results entered.
        mListView = (ListView) findViewById(R.id.listView1);

        // Create a local instance of our custom Adapter for our
        // ListView.
        mAdapter = new WeatherDataArrayAdapter(this);

        // Set the adapter to the ListView.
        mListView.setAdapter(mAdapter);


        // Handle any configuration change.
        handleConfigurationChanges();
    }

    /**
     * Hook method called by Android when this Activity is
     * destroyed.
     */
    @Override
    protected void onDestroy() {
        // Unbind from the Service.
        weatherOps.unbindService();

        // Always call super class for necessary operations when an
        // Activity is destroyed.
        super.onDestroy();
    }

    /**
     * Handle hardware reconfigurations, such as rotating the display.
     */
    protected void handleConfigurationChanges() {
        // If this method returns true then this is the first time the
        // Activity has been created.
        if (mRetainedFragmentManager.firstTimeIn()) {
            Log.d(TAG,
                  "First time onCreate() call");

            // Create the AcronymOps object one time.  The "true"
            // parameter instructs AcronymOps to use the
            // DownloadImagesBoundService.
            weatherOps = new WeatherOpsImpl(this) ;

            // Store the AcronymOps into the RetainedFragmentManager.
            mRetainedFragmentManager.put("WEATHER_STATE",
                    weatherOps);
            
            // Initiate the service binding protocol (which may be a
            // no-op, depending on which type of DownloadImages*Service is
            // used).
            weatherOps.bindService();

            lastCallTime=null;
            mRetainedFragmentManager.put("LAST_CALL",
                    lastCallTime);

            mRetainedFragmentManager.put("LAST_REQUEST",
                    lastRequest);
        } else {
            // The RetainedFragmentManager was previously initialized,
            // which means that a runtime configuration change
            // occured.

            Log.d(TAG,
                  "Second or subsequent onCreate() call");

            // Obtain the AcronymOps object from the
            // RetainedFragmentManager.
            weatherOps =
                mRetainedFragmentManager.get("WEATHER_STATE");

            lastCallTime=mRetainedFragmentManager.get("LAST_CALL");
            lastRequest = mRetainedFragmentManager.get("LAST_REQUEST");
            // This check shouldn't be necessary under normal
            // circumtances, but it's better to lose state than to
            // crash!
            if (weatherOps == null) {
                // Create the AcronymOps object one time.  The "true"
                // parameter instructs AcronymOps to use the
                // DownloadImagesBoundService.
                weatherOps = new WeatherOpsImpl(this);

                // Store the AcronymOps into the RetainedFragmentManager.
                mRetainedFragmentManager.put("WEATHER_STATE",
                        weatherOps);

                // Initiate the service binding protocol (which may be
                // a no-op, depending on which type of
                // DownloadImages*Service is used).
                weatherOps.bindService();
            } else
                // Inform it that the runtime configuration change has
                // completed.
                weatherOps.onConfigurationChange(this);
        }
    }

    /*
     * Initiate the synchronous acronym lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void expandWeatherSync(View v) {

        // Get the acronym entered by the user.
        final String cityState =
                mEditText.getText().toString();

        if (cityState.equals(lastRequest) && !enoughTimePassed()){
            Utils.showToast(this, "Request made too soon, current data is up to date");
            return;
        }

        lastRequest = cityState;
        mRetainedFragmentManager.put("LAST_REQUEST",
                lastRequest);
        // Reset the display for the next acronym expansion.
        resetDisplay();

        // Asynchronously expand the acronym. 
        weatherOps.getWeatherDataSync(cityState);
    }

    /*
     * Initiate the asynchronous acronym lookup when the user presses
     * the "Look Up Async" button.
     */
    public void expandWeatherAsync(View v) {

        // Get the acronym entered by the user.
        final String cityState =
            mEditText.getText().toString();

        if (cityState.equals(lastRequest) && !enoughTimePassed()){
            Utils.showToast(this, "Request made too soon, current data is up to date");
            return;
        }

        lastRequest = cityState;
        mRetainedFragmentManager.put("LAST_REQUEST",
                lastRequest);
        // Reset the display for the next acronym expansion.
        resetDisplay();
        
        // Asynchronously expand the acronym. 
        weatherOps.getWeatherDataAsync(cityState);
    }

    /**
     * Display the results to the screen.
     * 
     * @param results
     *            List of Results to be displayed.
     */
    public void displayResults(List<WeatherData> results,
                               String errorMessage) {
        if (results == null || results.size() == 0)
            Utils.showToast(this,
                    errorMessage);
        else {
            Log.d(TAG,
                  "displayResults() with weather results = "
                  + results.size());

            // Set/change data set.
            mAdapter.clear();
            mAdapter.addAll(results);
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean enoughTimePassed(){
        Long currentTime = System.currentTimeMillis();
        if (lastCallTime==null){
            lastCallTime = currentTime;
            return true;
        }
        boolean result = currentTime -  lastCallTime>10000?true:false;
        if (result) {
            lastCallTime = currentTime;
            mRetainedFragmentManager.put("LAST_CALL",
                    lastCallTime);
        }
        return result;
    }

    /**
     * Reset the display prior to attempting to expand a new acronym.
     */
    private void resetDisplay() {
        Utils.hideKeyboard(this,
                           mEditText.getWindowToken());
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }
}
