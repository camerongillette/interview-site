package com.cam.interview.core.pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WeatherPOJO {
    long humidity;
    String conditions;
    long temperature;

    private static final Logger log = LoggerFactory.getLogger(WeatherPOJO.class);


    public WeatherPOJO(JSONObject jsonObject){
        if(jsonObject!=null){
            try {
                JSONObject main = jsonObject.getJSONObject("main");
                if(main!=null){
                    humidity=main.getLong("humidity");
                    temperature=main.getLong("temp");
                }
                JSONArray weatherArray = jsonObject.getJSONArray("weather");
                if(weatherArray!=null && weatherArray.length()>0){
                    conditions = weatherArray.getJSONObject(0).getString("description");
                }
            } catch (JSONException e) {
                log.error("JSON error");
            }
        }
    }
    
    public long getHumidity() {
        return humidity;
    }

    public String getConditions() {
        return conditions;
    }

    public long getTemperature() {
        return temperature;
    }    
}
