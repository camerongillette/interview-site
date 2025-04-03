package com.cam.interview.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cam.interview.core.pojo.WeatherPOJO;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class OpenWeather {

    private static final Logger log = LoggerFactory.getLogger(OpenWeather.class);
    private static final String WEATHER_API_KEY = "a41c90a3b8d1a4113328e36c7d0746a7";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final long CACHE_DURATION = 15 * 60 * 1000; // 15 minutes in milliseconds
    private long lastFetchedTime = 0;
    private WeatherPOJO weatherData;

    @ValueMapValue
    private String location = "London";  // Default location

    // Method to return all weather data as a single object
    public WeatherPOJO getWeatherData() {
        long currentTime = System.currentTimeMillis();
        if (weatherData == null || (currentTime - lastFetchedTime) > CACHE_DURATION) {
            try {
                String apiUrl = String.format("%s?q=%s&appid=%s&units=metric", WEATHER_API_URL, location, WEATHER_API_KEY);
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
    
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                int data;
                while ((data = reader.read()) != -1) {
                    response.append((char) data);
                }
    
                weatherData = new WeatherPOJO(new JSONObject(response.toString()));
                lastFetchedTime = System.currentTimeMillis();
            } catch (Exception e) {
                log.error("Error fetching weather data", e);
                weatherData = new WeatherPOJO(new JSONObject());  // Return empty data in case of failure
            };
        }
        return weatherData != null ? weatherData : new WeatherPOJO(new JSONObject());
    }

    public String getLocation(){
        return location;
    }
}
