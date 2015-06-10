package aifa.assignment3.services;

import java.util.List;

import aifa.assignment3.aidl.WeatherData;
import aifa.assignment3.aidl.WeatherRequest;
import aifa.assignment3.aidl.WeatherResults;
import aifa.assignment3.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 *
 */
public class WeatherServiceAsync extends LifecycleLoggingService {
    /**
     * Factory method that makes an Intent used to start the
     * AcronymServiceAsync when passed to bindService().
     * 
     * @param context
     *            The context of the calling component.
     */
    public static Intent makeIntent(Context context) {
        return new Intent(context,
                          WeatherServiceAsync.class);
    }

    /**
     * Called when a client (e.g., AcronymActivity) calls
     * bindService() with the proper Intent.  Returns the
     * implementation of AcronymRequest, which is implicitly cast as
     * an IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAcronymRequestImpl;
    }

    /**
     * The concrete implementation of the AIDL Interface
     * AcronymRequest, which extends the Stub class that implements
     * AcronymRequest, thereby allowing Android to handle calls across
     * process boundaries.  This method runs in a separate Thread as
     * part of the Android Binder framework.
     * 
     * This implementation plays the role of Invoker in the Broker
     * Pattern.
     */
    private final WeatherRequest.Stub mAcronymRequestImpl =
        new WeatherRequest.Stub() {
            /**
             * Implement the AIDL AcronymRequest expandAcronym()
             * method, which forwards to DownloadUtils getResults() to
             * obtain the results from the Acronym Web service and
             * then sends the results back to the Activity via a
             * callback.
             */
            @Override
            public void getCurrentWeather(String cityState,
                                      WeatherResults callback)
                throws RemoteException {

                // Call the Acronym Web service to get the list of
                // possible expansions of the designated acronym.
                final List<WeatherData> weatherDataList =
                    Utils.getResults(cityState);

                if (weatherDataList != null) {
                    Log.d(TAG, "" 
                          + weatherDataList.size()
                          + " results for cityState: "
                          + cityState);
                    // Invoke a one-way callback to send list of
                    // acronym expansions back to the AcronymActivity.
                    callback.sendResults(weatherDataList);
                } else
                    // Invoke a one-way callback to send an error
                    // message back to the AcronymActivity.
                    callback.sendError("No weather for "
                                       + cityState
                                       + " found");
            }
	};
}
