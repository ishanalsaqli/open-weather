package com.kila.weather.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.kila.weather.constants.Constants;
import com.kila.weather.dto.WeatherDto;
import com.kila.weather.dto.WeatherResponse;
import com.kila.weather.exception.ErrorResponse;
import com.kila.weather.exception.WeatherStackApiException;
import com.kila.weather.mapper.WeatherMapper;
import com.kila.weather.model.Weather;
import com.kila.weather.repository.WeatherRepository;
import com.kila.weather.util.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeatherServiceTest extends TestSupport {

    @Mock
    private WeatherRepository repository;
    @Mock
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    @Mock
    private WeatherMapper mapper;
    @InjectMocks
    private WeatherService service;


    @BeforeEach
    public void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());

        Clock clock = mock(Clock.class);

        Constants constants = new Constants();
        constants.setApiUrl("weather-base-api-url");
        constants.setAccessKey("api-key");
        constants.setApiCallLimit(30);

        service = new WeatherService(repository, restTemplate, clock, mapper);

        when(clock.instant()).thenReturn(getCurrentInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
    }

    @Test
    public void testGetWeatherByCityName_whenFirstRequestForRequestedCity_shouldCallWeatherStackAPIAndSaveResponse()
            throws JsonProcessingException {

        String url = WEATHER_STACK_API_URL + requestedCity;
        String responseJson = getAmsterdamWeatherJson();
        WeatherResponse response = objectMapper.readValue(responseJson, WeatherResponse.class);
        Weather savedEntity = getSavedWeatherEntity(response.location().localTime());

        WeatherDto expected = new WeatherDto(
                savedEntity.getCityName(),
                savedEntity.getCountry(),
                savedEntity.getTemperature(),
                savedEntity.getUpdateTime());

        when(repository.findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(url, String.class)).thenReturn(ResponseEntity.ok(responseJson));
        when(mapper.mapToWeather(
                requestedCity,
                response, LocalDateTime.now(),
                LocalDateTime.parse(response.location().localTime(), formatter)))
                .thenReturn(savedEntity);
        when(repository.save(savedEntity)).thenReturn(savedEntity);
        when(mapper.mapWeatherToDto(any())).thenReturn(expected);

        WeatherDto actual = service.getWeatherByCityName(requestedCity);
        assertEquals(expected, actual);

        verify(restTemplate).getForEntity(url, String.class);
        verify(repository).save(any());

    }

    @Test
    public void testGetWeatherByCityName_whenWeatherStackReturnError_shouldThrowWeatherStackApiException()
            throws JsonProcessingException {

        String requestedCity = "xyz";
        String url = WEATHER_STACK_API_URL + requestedCity;
        String responseJson = getErrorResponseJson();
        ErrorResponse response = objectMapper.readValue(responseJson, ErrorResponse.class);

        when(repository.findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(url, String.class))
                .thenReturn(ResponseEntity.ok(responseJson));

        assertThatThrownBy(() -> service.getWeatherByCityName(requestedCity))
                .isInstanceOf(WeatherStackApiException.class)
                .isEqualTo(new WeatherStackApiException(response));

        verify(restTemplate).getForEntity(url, String.class);
        verify(repository).findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity);
        verifyNoMoreInteractions(repository);

    }

    @Test
    public void testGetWeather_whenWeatherStackReturnUnknownResponse_shouldThrowRuntimeException() {

        String url = WEATHER_STACK_API_URL + requestedCity;
        String responseJson = "UnknownResponse";

        when(repository.findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity))
                .thenReturn(Optional.empty());
        when(restTemplate.getForEntity(url, String.class)).thenReturn(ResponseEntity.ok(responseJson));

        assertThatThrownBy(() -> service.getWeatherByCityName(requestedCity))
                .isInstanceOf(RuntimeException.class);

        verify(restTemplate).getForEntity(url, String.class);
        verify(repository).findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void testGetWeatherByCityName_whenCityAlreadyExistsAndNotOlderThan30Minutes_returnWeatherAndNotCallWeatherStackAPI()
            throws JsonProcessingException {

        String url = WEATHER_STACK_API_URL + requestedCity;
        String responseJson = getAmsterdamWeatherJson();
        WeatherResponse response = objectMapper.readValue(responseJson, WeatherResponse.class);
        Weather savedEntity = getSavedWeatherEntity(response.location().localTime());

        WeatherDto weatherDto = new WeatherDto(
                savedEntity.getCityName(),
                savedEntity.getCountry(),
                savedEntity.getTemperature(),
                savedEntity.getUpdateTime());

        when(repository.findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity))
                .thenReturn(Optional.of(savedEntity));
        when(restTemplate.getForEntity(url,String.class)).thenReturn(ResponseEntity.ok(responseJson));
        when(mapper.mapToWeather(
                requestedCity,
                response, LocalDateTime.now(),
                LocalDateTime.parse(response.location().localTime(), formatter)))
                .thenReturn(savedEntity);
        when(repository.save(any())).thenReturn(savedEntity);
        when(mapper.mapWeatherToDto(any())).thenReturn(weatherDto);


        var actual = service.getWeatherByCityName(requestedCity);
        assertEquals(weatherDto, actual);

        verifyNoInteractions(restTemplate);
        verify(repository).findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity);
        verifyNoMoreInteractions(repository);

    }

    @Test
    public void testGetWeather_whenCityAlreadyExistsAndOlderThan30Minutes_shouldCallWeatherStackAPIAndSaveWeather()
            throws Exception {

        String url = WEATHER_STACK_API_URL + requestedCity;
        String responseJson = getAmsterdamWeatherJson();
        WeatherResponse response = objectMapper.readValue(responseJson, WeatherResponse.class);
        Weather oldEntity = new Weather("id",
                requestedCity,
                "Amsterdam",
                "Netherlands",
                2,
                LocalDateTime.parse("2023-03-05 12:35", formatter),
                LocalDateTime.parse(response.location().localTime(), formatter));

        Weather savedEntity = getSavedWeatherEntity(response.location().localTime());

        WeatherDto expected = new WeatherDto(
                savedEntity.getCityName(),
                savedEntity.getCountry(),
                savedEntity.getTemperature(),
                savedEntity.getUpdateTime());

        when(repository.findFirstByRequestedCityNameOrderByUpdateTimeDesc(requestedCity))
                .thenReturn(Optional.of(oldEntity));
        when(restTemplate.getForEntity(url, String.class)).thenReturn(ResponseEntity.ok(responseJson));
        when(mapper.mapToWeather(
                requestedCity,
                response, LocalDateTime.now(),
                LocalDateTime.parse(response.location().localTime(), formatter)))
                .thenReturn(savedEntity);
        when(repository.save(any())).thenReturn(savedEntity);
        when(mapper.mapWeatherToDto(any())).thenReturn(expected);

        WeatherDto result = service.getWeatherByCityName(requestedCity);

        assertEquals(expected, result);

        verify(restTemplate).getForEntity(url, String.class);
        verify(repository).save(any());

    }

    @Test
    public void testClearCache() {
        Logger logger = (Logger) LoggerFactory.getLogger(WeatherService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        service.clearCache();

        List<ILoggingEvent> logsList = listAppender.list;

        assertEquals("Caches are cleared", logsList.get(0).getMessage());
        assertEquals(Level.WARN, logsList.get(0).getLevel());
    }
}