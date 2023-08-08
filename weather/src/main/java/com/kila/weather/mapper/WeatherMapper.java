package com.kila.weather.mapper;

import com.kila.weather.dto.WeatherDto;
import com.kila.weather.dto.WeatherResponse;
import com.kila.weather.model.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface WeatherMapper {

    @Mapping(source = "city",target = "requestedCityName")
    @Mapping(source = "response.location.name",target = "cityName")
    @Mapping(source = "response.location.country",target = "country")
    @Mapping(source = "response.current.temperature",target = "temperature")
    @Mapping(source = "now",target = "updateTime")
    @Mapping(source = "responseTime",target = "responseLocalTime")
    Weather mapToWeather(String city, WeatherResponse response, LocalDateTime now, LocalDateTime responseTime);

    WeatherDto mapWeatherToDto(Weather from);

}
