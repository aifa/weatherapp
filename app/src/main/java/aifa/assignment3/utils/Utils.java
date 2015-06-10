package aifa.assignment3.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import aifa.assignment3.aidl.WeatherData;
import aifa.assignment3.jsonweather.JsonWeather;
import aifa.assignment3.jsonweather.Weather;
import aifa.assignment3.jsonweather.WeatherJSONParser;

/**
 * Connects to the weather service and returns a list of
 */
public class Utils {
    /**
     * Logging tag used by the debugger. 
     */
    private final static String TAG = Utils.class.getCanonicalName();

    /** 
     * URL to the Acronym web service.
     */
    private final static String open_weather_Service_URL =
        "http://api.openweathermap.org/data/2.5/weather?q=";

    /**
     *
     * @param cityState
     * @return
     */
    public static List<WeatherData> getResults(final String cityState) {
        // Create a List that will return the AcronymData obtained
        // from the Acronym Service web service.
        final List<WeatherData> returnList =
            new ArrayList<WeatherData>();
            
        // A List of JsonAcronym objects.
        List<JsonWeather> jsonWeatherList = null;

        try {
            // Append the location to create the full URL.
            final URL url =
                new URL(open_weather_Service_URL
                        + cityState);

            // Opens a connection to the Acronym Service.
            HttpURLConnection urlConnection =
                (HttpURLConnection) url.openConnection();
            
            // Sends the GET request and reads the Json results.
            try {
                InputStream in =
                        new BufferedInputStream(urlConnection.getInputStream());
                 // Create the parser.
                 final WeatherJSONParser parser =
                     new WeatherJSONParser();

                // Parse the Json results and create JsonAcronym data
                // objects.
                jsonWeatherList = parser.parseJsonStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // See if we parsed any valid data. cod needs to be 200
        if (jsonWeatherList != null && jsonWeatherList.size() > 0 && jsonWeatherList.get(0).getCod()==200) {
            for (JsonWeather jsonWeather : jsonWeatherList)
                    returnList.add(new WeatherData(jsonWeather.getName(), jsonWeather.getWind().getSpeed(), jsonWeather.getWind().getDeg(), jsonWeather.getMain().getTemp(),
                            jsonWeather.getMain().getHumidity(), jsonWeather.getSys().getSunrise(), jsonWeather.getSys().getSunset()));

             // Return the List of AcronymData.
             return returnList;
        }  else
            return null;
    }
    /**
     * This method is used to hide a keyboard after a user has
     * finished typing the url.
     */
    public static void hideKeyboard(Activity activity,
                                    IBinder windowToken) {
        InputMethodManager mgr =
           (InputMethodManager) activity.getSystemService
            (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken,
                                    0);
    }

    /**
     * Show a toast message.
     */
    public static void showToast(Context context,
                                 String message) {
        Toast.makeText(context,
                       message,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * Ensure this class is only used as a utility.
     */
    private Utils() {
        throw new AssertionError();
    } 
}
