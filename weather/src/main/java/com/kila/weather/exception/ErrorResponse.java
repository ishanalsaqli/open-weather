package com.kila.weather.exception;

public record ErrorResponse (
        String success,
        Error error
) { }
