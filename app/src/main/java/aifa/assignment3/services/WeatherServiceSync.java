package aifa.assignment3.services;

import java.util.ArrayList;
import java.util.List;


import aifa.assignment3.aidl.WeatherCall;
import aifa.assignment3.aidl.WeatherData;
import aifa.assignment3.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 *
 */
public class WeatherServiceSync extends LifecycleLoggingService {

    public static Intent makeIntent(Context context) {
        return new Intent(context,
                          WeatherServiceSync.class);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mAcronymCallImpl;
    }

    private final WeatherCall.Stub mAcronymCallImpl =
        new WeatherCall.Stub() {

            @Override
            public List<WeatherData> getCurrentWeather(String cityState)
                throws RemoteException {

                final List<WeatherData> weatherDataList =
                    Utils.getResults(cityState);

                if (weatherDataList != null) {
                    Log.d(TAG, "" 
                          + weatherDataList.size()
                          + " results for city, State: "
                          + cityState);

                    return weatherDataList;
                } else {

                    return new ArrayList<WeatherData>();
                }
            }
	};
}
