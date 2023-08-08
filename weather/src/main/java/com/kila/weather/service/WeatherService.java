package com.kila.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kila.weather.dto.WeatherDto;
import com.kila.weather.dto.WeatherResponse;
import com.kila.weather.exception.ErrorResponse;
import com.kila.weather.exception.WeatherStackApiException;
import com.kila.weather.mapper.WeatherMapper;
import com.kila.weather.model.Weather;
import com.kila.weather.repository.WeatherRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.kila.weather.constants.Constants.*;

@Service
@CacheConfig(cacheNames = {"weathers"})
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private final WeatherRepository repository;
    private final RestTemplate restTemplate;
    private final Clock clock;
    private final WeatherMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherService(WeatherRepository repository, RestTemplate restTemplate, Clock clock, WeatherMapper mapper) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.clock = clock;
        this.mapper = mapper;
    }

    @Cacheable(key = "#city")
    public WeatherDto getWeatherByCityName(String city) {

        Optional<Weather> weatherOpt = repository.findFirstByRequestedCityNameOrderByUpdateTimeDesc(city);

        return weatherOpt.map(weather -> {
            if (weather.getUpdateTime().isBefore(getLocalDateTimeNow().minusMinutes(API_CALL_LIMIT))) {
                logger.info(String.format("Creating a new city weather from weather stack api " +
                        "for %s due to the current one is not up-to-date", city));
                return getCityWeather(city);
            }
            logger.info(String.format("Getting weather from database for %s due to it is already up-to-date", city));
            return mapper.mapWeatherToDto(weather);
        }).orElseGet(() -> getCityWeather(city));

    }

    @Cacheable(key = "#city")
    public WeatherDto getCityWeather(String city) {

        logger.info("Requesting weather stack api for city: " + city);
        String url = getWeatherStackApiUrl(city);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        try {
            WeatherResponse weatherResponse = objectMapper.readValue(responseEntity.getBody(), WeatherResponse.class);
            return mapper.mapWeatherToDto(saveWeather(city, weatherResponse));
        } catch (JsonProcessingException e) {
            try {
                ErrorResponse errorResponse = objectMapper.readValue(responseEntity.getBody(), ErrorResponse.class);
                throw new WeatherStackApiException(errorResponse);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }

    }

    @CacheEvict(allEntries = true)
    @PostConstruct
    @Scheduled(fixedRateString = "${weather-stack.api.cache-ttl}")
    public void clearCache() {
        logger.warn("Caches are cleared");
    }

    private String getWeatherStackApiUrl(String city) {
        return WEATHER_STACK_API_BASE_URL + WEATHER_STACK_API_ACCESS_KEY_PARAM + API_KEY +
                WEATHER_STACK_API_QUERY_PARAM + city;
    }

    private Weather saveWeather(String city, WeatherResponse weatherResponse) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        Weather weather = mapper.mapToWeather(
                city,
                weatherResponse,
                LocalDateTime.now(),
                LocalDateTime.parse(weatherResponse.location().localTime(), dateTimeFormatter));

        return repository.save(weather);
    }

    private LocalDateTime getLocalDateTimeNow() {
        Instant instant = clock.instant();
        return LocalDateTime.ofInstant(
                instant,
                Clock.systemDefaultZone().getZone());
    }

}
