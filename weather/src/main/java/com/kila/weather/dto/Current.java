package com.kila.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Current(
        @JsonProperty("observation_time")
        String observationTime,
        Integer temperature,
        @JsonProperty("weather_code")
        String weatherCode,
        @JsonProperty("weather_icons")
        List<String> weatherIcons,
        @JsonProperty("weather_descriptions")
        List<String> weatherDescriptions,
        @JsonProperty("wind_speed")
        String windSpeed,
        @JsonProperty("wind_degree")
        String windDegree,
        @JsonProperty("wind_dir")
        String windDir,
        String pressure,
        @JsonProperty("precip")
        String precipitate,
        String humidity,
        @JsonProperty("cloudcover")
        String cloudCover,
        @JsonProperty("feelslike")
        String feelsLike,
        @JsonProperty("uv_index")
        String uvIndex,
        String visibility,
        @JsonProperty("is_day")
        String isDay
) {
}
