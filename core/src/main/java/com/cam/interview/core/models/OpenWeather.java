package com.cam.interview.core.models;

import com.cam.interview.core.pojo.WeatherPOJO;
import com.cam.interview.core.services.OpenWeatherService;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class OpenWeather {

    private static final Logger log = LoggerFactory.getLogger(OpenWeather.class);
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final Map<String, CachedWeatherData> weatherCache = new ConcurrentHashMap<>();

    @ValueMapValue
    private String location;

    @ValueMapValue
    @Default(intValues = 15)
    private int cacheTime;

    private WeatherPOJO weatherData;
    private String apiKey;

    @OSGiService
    private OpenWeatherService openWeatherService;

    @PostConstruct
    protected void init() {
        apiKey = openWeatherService.getApiKey();
        if(apiKey==null || apiKey.equals("")){
            log.error("MISSING OPEN WEATHER API KEY");
        }
        else if (location != null) {
            weatherData = getWeatherDataFromCacheOrApi(location);
        }
    }

    private WeatherPOJO getWeatherDataFromCacheOrApi(String cacheKey) {
        long currentTime = System.currentTimeMillis();
        CachedWeatherData cachedData = weatherCache.get(cacheKey);
        long cacheDuration = cacheTime * 60 * 1000; // Calculate milliseconds
        if (cachedData != null && (currentTime - cachedData.getTimestamp()) < cacheDuration) {
            log.debug("Retrieved weather data from cache for location: {}", location);
            return cachedData.getWeatherData();
        } else {
            log.debug("Fetching fresh weather data for location: {}", location);
            //WeatherPOJO fetchedData = fetchWeatherDataFromApi();
            WeatherPOJO fetchedData;
            try {
                if (apiKey == null || apiKey.isEmpty()) {
                    log.error("Weather API Key is not configured.");
                    fetchedData = new WeatherPOJO(new JSONObject());
                }
                String apiUrl = String.format("%s?q=%s&appid=%s&units=metric", WEATHER_API_URL, location, apiKey);
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
    
                return new WeatherPOJO(new JSONObject(response.toString()));
    
            } catch (Exception e) {
                log.error("Error fetching weather data from API", e);
                fetchedData = new WeatherPOJO(new JSONObject()); // Return empty data in case of failure
            }


            if (fetchedData != null) {
                weatherCache.put(cacheKey, new CachedWeatherData(fetchedData, currentTime));
            }
            return fetchedData;
        }
    }


    private static class CachedWeatherData {
        private final WeatherPOJO weatherData;
        private final long timestamp;

        public CachedWeatherData(WeatherPOJO weatherData, long timestamp) {
            this.weatherData = weatherData;
            this.timestamp = timestamp;
        }

        public WeatherPOJO getWeatherData() {
            return weatherData;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public WeatherPOJO getWeatherData() {
        return weatherData != null ? weatherData : new WeatherPOJO(new JSONObject());
    }

    public String getLocation() {
        return location;
    }
}
