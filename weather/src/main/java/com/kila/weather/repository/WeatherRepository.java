package com.kila.weather.repository;

import com.kila.weather.model.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeatherRepository extends JpaRepository<Weather, String> {

    /*
    * SELECT * FROM WEATHER
      WHERE REQUESTED_CITY_NAME = '${city}'
      ORDER BY UPDATE_TIME  DESC
      LIMIT 1*/
    Optional<Weather> findFirstByRequestedCityNameOrderByUpdateTimeDesc(String city);

}
