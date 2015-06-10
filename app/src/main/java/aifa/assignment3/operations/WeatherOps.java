package aifa.assignment3.operations;

import aifa.assignment3.activities.WeatherActivity;
import android.view.View;

/**
 * This class defines all the weather-related operations.
 */
public interface WeatherOps {

    public void bindService();

    public void unbindService();

    public void getWeatherDataSync(String acronym);

    public void getWeatherDataAsync(String acronym);

    public void onConfigurationChange(WeatherActivity activity);
}
