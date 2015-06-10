package aifa.assignment3.operations;

import java.lang.ref.WeakReference;
import java.util.List;

import aifa.assignment3.activities.WeatherActivity;
import aifa.assignment3.aidl.WeatherCall;
import aifa.assignment3.aidl.WeatherData;
import aifa.assignment3.aidl.WeatherRequest;
import aifa.assignment3.aidl.WeatherResults;
import aifa.assignment3.services.WeatherServiceAsync;
import aifa.assignment3.services.WeatherServiceSync;
import aifa.assignment3.utils.GenericServiceConnection;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

/**
 * WeatherOps Implementation
 */
public class WeatherOpsImpl implements WeatherOps {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG = getClass().getSimpleName();

    /**
     * Used to enable garbage collection.
     */
    protected WeakReference<WeatherActivity> mActivity;

    private GenericServiceConnection<WeatherCall> mServiceConnectionSync;

    private GenericServiceConnection<WeatherRequest> mServiceConnectionAsync;

    /**
     * List of results to display (if any).
     */
    protected List<WeatherData> mResults;

    private final Handler mDisplayHandler = new Handler();

    private final WeatherResults.Stub mWeatherResults =
        new WeatherResults.Stub() {
            /**
             * This method is invoked by the AcronymServiceAsync to
             * return the results back to the AcronymActivity.
             */
            @Override
            public void sendResults(final List<WeatherData> acronymDataList)
                throws RemoteException {
                mDisplayHandler.post(new Runnable() {
                        public void run() {
                            mResults = acronymDataList;
                            mActivity.get().displayResults
                                (acronymDataList,
                                 null);
                        }
                    });
            }

            /**
             * This method is invoked by the AcronymServiceAsync to
             * return error results back to the AcronymActivity.
             */
            @Override
            public void sendError(final String reason)
                throws RemoteException {
                mDisplayHandler.post(new Runnable() {
                        public void run() {
                            mActivity.get().displayResults(null,
                                                           reason);
                        }
                    });
            }
	};

    /**
     * Constructor initializes the fields.
     */
    public WeatherOpsImpl(WeatherActivity activity) {
        // Initialize the WeakReference.
        mActivity = new WeakReference<>(activity);

        // Initialize the GenericServiceConnection objects.
        mServiceConnectionSync = 
            new GenericServiceConnection<WeatherCall>(WeatherCall.class);

        mServiceConnectionAsync =
            new GenericServiceConnection<WeatherRequest>(WeatherRequest.class);
    }

    /**
     * Called after a runtime configuration change occurs to finish
     * the initialization steps.
     */
    public void onConfigurationChange(WeatherActivity activity) {
        Log.d(TAG,
              "onConfigurationChange() called");

        // Reset the mActivity WeakReference.
        mActivity = new WeakReference<>(activity);

        updateResultsDisplay();
    }

    /**
     * Display results if any (due to runtime configuration change).
     */
    private void updateResultsDisplay() {
        if (mResults != null)
            mActivity.get().displayResults(mResults, 
                                           null);
    }

    /**
     * Initiate the service binding protocol.
     */
    @Override
    public void bindService() {
        Log.d(TAG,
              "calling bindService()");

        if (mServiceConnectionSync.getInterface() == null) 
            mActivity.get().getApplicationContext().bindService
                (WeatherServiceSync.makeIntent(mActivity.get()),
                 mServiceConnectionSync,
                 Context.BIND_AUTO_CREATE);

        if (mServiceConnectionAsync.getInterface() == null) 
            mActivity.get().getApplicationContext().bindService
                (WeatherServiceAsync.makeIntent(mActivity.get()),
                 mServiceConnectionAsync,
                 Context.BIND_AUTO_CREATE);
    }

    /**
     * Initiate the service unbinding protocol.
     */
    @Override
    public void unbindService() {
        if (mActivity.get().isChangingConfigurations()) 
            Log.d(TAG,
                  "just a configuration change - unbindService() not called");
        else {
            Log.d(TAG,
                  "calling unbindService()");

            // Unbind the Async Service if it is connected.
            if (mServiceConnectionAsync.getInterface() != null)
                mActivity.get().getApplicationContext().unbindService
                    (mServiceConnectionAsync);

            // Unbind the Sync Service if it is connected.
            if (mServiceConnectionSync.getInterface() != null)
                mActivity.get().getApplicationContext().unbindService
                    (mServiceConnectionSync);
        }
    }

    /*
     * Initiate the asynchronous acronym lookup when the user presses
     * the "Look Up Async" button.
     */
    @Override
    public void getWeatherDataAsync(String acronym) {
        final WeatherRequest weatherRequest =
            mServiceConnectionAsync.getInterface();

        if (weatherRequest != null) {
            try {
                weatherRequest.getCurrentWeather(acronym,
                        mWeatherResults);
            } catch (RemoteException e) {
                Log.e(TAG,
                      "RemoteException:" 
                      + e.getMessage());
            }
        } else {
            Log.d(TAG,
                  "weather Request was null.");
        }
    }

    @Override
    public void getWeatherDataSync(String cityState) {
        final WeatherCall weatherCall =
            mServiceConnectionSync.getInterface();

        if (weatherCall != null) {
            new AsyncTask<String, Void, List<WeatherData>> () {
                /**
                 * Acronym we're trying to expand.
                 */
                private String cityState;

                /**
                 * Retrieve the expanded acronym results via a
                 * synchronous two-way method call, which runs in a
                 * background thread to avoid blocking the UI thread.
                 */
                protected List<WeatherData> doInBackground(String... cityState) {
                    try {
                        this.cityState = cityState[0];
                        return weatherCall.getCurrentWeather(this.cityState);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                /**
                 * Display the results in the UI Thread.
                 */
                protected void onPostExecute(List<WeatherData> weatherDataList) {
                    mResults = weatherDataList;
                    mActivity.get().displayResults(weatherDataList,
                                                   "No Weather "
                                                   + cityState
                                                   + " found");
                }
                // Execute the AsyncTask to expand the acronym without
                // blocking the caller.
            }.execute(cityState);
        } else {
            Log.d(TAG, "cityState was null.");
        }
    }
}
