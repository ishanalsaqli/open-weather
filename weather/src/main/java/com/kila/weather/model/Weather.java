package com.kila.weather.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Weather {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private String requestedCityName;
    private String cityName;
    private String country;
    private Integer temperature;
    private LocalDateTime updateTime;
    private LocalDateTime responseLocalTime;

    public Weather(String city,
                   String name,
                   String country,
                   Integer temperature,
                   LocalDateTime updateTime,
                   LocalDateTime responseLocalTime) {
        this.requestedCityName = city;
        this.cityName = name;
        this.country = country;
        this.temperature = temperature;
        this.updateTime = updateTime;
        this.responseLocalTime = responseLocalTime;
    }

}
