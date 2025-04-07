package com.cam.interview.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "OpenWeather Configuration",
    description = "Configuration for OpenWeather API"
)
public @interface OpenWeatherConfig {

    @AttributeDefinition(
        name = "API Key",
        description = "API Key for accessing OpenWeather"
    )
    String api_key();
}
