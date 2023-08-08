package com.kila.weather.util;

import com.kila.weather.constants.Constants;

public class ConstantsTestUtil {

    public static String WEATHER_STACK_API_BASE_URL = "";
    public static String WEATHER_STACK_API_ACCESS_KEY_PARAM = "?access_key=";
    public static String WEATHER_STACK_API_QUERY_PARAM = "&query=";

    public static String API_KEY = "";
    public static Integer API_CALL_LIMIT = 30;
    public static String WEATHER_CACHE_NAME = "weather";
    private static Constants constants;

    public ConstantsTestUtil() {
    }

    public static Constants buildConstants() {
        constants.WEATHER_STACK_API_BASE_URL = WEATHER_STACK_API_BASE_URL;
        constants.WEATHER_CACHE_NAME = WEATHER_CACHE_NAME;
        constants.API_KEY = API_KEY;
        constants.API_CALL_LIMIT = API_CALL_LIMIT;
        return constants;
    }

}
