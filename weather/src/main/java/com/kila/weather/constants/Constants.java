package com.kila.weather.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Constants {

    public static String WEATHER_STACK_API_BASE_URL;
    public static String WEATHER_STACK_API_ACCESS_KEY_PARAM = "?access_key=";
    public static String WEATHER_STACK_API_QUERY_PARAM = "&query=";

    public static String API_KEY;
    public static Integer API_CALL_LIMIT;
    public static String WEATHER_CACHE_NAME;

    @Value("${weather-stack.api.url}")
    public void setApiUrl(String apiUrl) {
        WEATHER_STACK_API_BASE_URL = apiUrl;
    }

    @Value("${weather-stack.api.access-key}")
    public void setAccessKey(String apiKey) {
        API_KEY = apiKey;
    }

    @Value("${weather-stack.api.cache-name}")
    public void setWeatherCacheName(String cacheName) {
        Constants.WEATHER_CACHE_NAME = cacheName;
    }

    @Value("${weather-stack.api.call-limit}")
    public void setApiCallLimit(Integer apiCallLimit) {
        API_CALL_LIMIT = apiCallLimit;
    }

}
