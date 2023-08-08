package com.kila.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Location(
      String name,
      String country,
      String region,
      String lat,
      String lon,
      @JsonProperty("timezone_id")
      String timezoneId,
      @JsonProperty("localtime")
      String localTime,
      @JsonProperty("localtime_epoch")
      String localtimeEpoch,
      @JsonProperty("utc_offset")
      String utcOffset
) {
}
