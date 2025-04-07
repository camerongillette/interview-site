package com.cam.interview.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.cam.interview.core.config.OpenWeatherConfig;

import org.osgi.service.component.annotations.ConfigurationPolicy;

@Component(
    service = OpenWeatherService.class,
    immediate = true,
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = OpenWeatherConfig.class)
public class OpenWeatherServiceImpl implements OpenWeatherService {

    private String apiKey;

    @Activate
    @Modified
    protected void activate(OpenWeatherConfig config) {
        this.apiKey = config.api_key();
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }
}
