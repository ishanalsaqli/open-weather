
#FROM openjdk:17-jdk-slim AS build
#COPY build.gradle .
#COPY settings.gradle .
#COPY gradle ./gradle
#COPY gradlew .
#COPY .env .
##RUN ./gradlew resolveDependencies
##COPY build/libs/weather-0.0.1.jar /app/
##RUN chmod +x ./gradlew
##RUN ./gradlew
#COPY src ./src
##ENTRYPOINT ["java", "-jar", "weather-0.0.1.jar"]
#RUN ./gradlew

FROM openjdk:17-jdk-slim AS build

# Copy the Gradle Wrapper scripts and properties file into the container
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle

# Copy the project files (build.gradle, settings.gradle, src) into the container
COPY build.gradle .
COPY settings.gradle .
COPY gradle ./gradle
COPY .env .

#RUN ./gradlew

COPY src ./src

RUN ./gradlew

# Make the Gradle Wrapper scripts executable (for Unix-based systems)
RUN chmod +x gradlew

# Build the Gradle project using the Gradle Wrapper
RUN ./gradlew clean build


FROM openjdk:17-jdk-slim
WORKDIR weather
COPY --from=build /build/libs/weather-0.0.1.jar open-weather.jar
ENTRYPOINT ["java","-jar","open-weather.jar"]

#FROM openjdk:17-jdk-slim
#WORKDIR open-weather
#COPY --from=build target/*.jar weather-0.0.1.jar
#ENTRYPOINT ["java", "-jar", "weather-0.0.1.jar"]
