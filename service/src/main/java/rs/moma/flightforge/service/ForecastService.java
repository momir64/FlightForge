package rs.moma.flightforge.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.DependsOn;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import rs.moma.flightforge.model.ForecastHour;
import rs.moma.flightforge.model.DayPart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@DependsOn("cepService")
@RequiredArgsConstructor
public class ForecastService {
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final DateTimeFormatter ISO_HOUR = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CepService cepService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private volatile String location;
    private volatile double latitude;
    private volatile double longitude;
    private volatile boolean locationResolved = false;

    public synchronized void setLocation(String location) {
        if (location.equals(this.location)) return;
        this.location = location;
        this.locationResolved = false;
        refresh();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refresh() {
        if (location == null) return;
        try {
            if (!locationResolved) resolveCoordinates();
            List<ForecastHour> hours = fetchForecast();
            cepService.updateForecast(hours);
        } catch (Exception ex) {
            log.error("Forecast refresh failed for location '{}': {}", location, ex.getMessage());
        }
    }

    private void resolveCoordinates() throws Exception {
        String url = GEOCODING_URL + "?name=" + URLEncoder.encode(location, StandardCharsets.UTF_8) + "&count=1&language=en&format=json";
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode results = root.path("results");
        if (results.isEmpty()) throw new IllegalArgumentException("Location not found: " + location);

        JsonNode first = results.get(0);
        latitude = first.path("latitude").asDouble();
        longitude = first.path("longitude").asDouble();
        locationResolved = true;
        log.info("Resolved '{}' to ({}, {})", location, latitude, longitude);
    }

    private List<ForecastHour> fetchForecast() throws Exception {
        LocalDate apiLimit = LocalDate.now().plusDays(14);
        LocalDate start = cepService.getCurrentTime().toLocalDate();
        LocalDate end = start.plusDays(10);
        if (start.isAfter(apiLimit)) return List.of();
        if (end.isAfter(apiLimit)) end = apiLimit;
        String url = FORECAST_URL + "?latitude=" + latitude + "&longitude=" + longitude
                     + "&hourly=temperature_2m,wind_speed_10m,precipitation"
                     + "&daily=sunrise,sunset"
                     + "&start_date=" + start.format(ISO_DATE)
                     + "&end_date=" + end.format(ISO_DATE)
                     + "&wind_speed_unit=ms"
                     + "&timezone=auto";

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);

        JsonNode hourly = root.path("hourly");
        JsonNode times = hourly.path("time");
        JsonNode temperatures = hourly.path("temperature_2m");
        JsonNode windSpeeds = hourly.path("wind_speed_10m");
        JsonNode precipitations = hourly.path("precipitation");

        JsonNode daily = root.path("daily");
        JsonNode dates = daily.path("time");
        JsonNode sunrises = daily.path("sunrise");
        JsonNode sunsets = daily.path("sunset");

        Map<LocalDate, LocalTime> sunriseMap = new HashMap<>();
        Map<LocalDate, LocalTime> sunsetMap = new HashMap<>();
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = LocalDate.parse(dates.get(i).asText(), ISO_DATE);
            LocalTime sunrise = LocalDateTime.parse(sunrises.get(i).asText(), ISO_HOUR).toLocalTime();
            LocalTime sunset = LocalDateTime.parse(sunsets.get(i).asText(), ISO_HOUR).toLocalTime();
            sunriseMap.put(date, sunrise);
            sunsetMap.put(date, sunset);
        }

        List<ForecastHour> hours = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            LocalDateTime timestamp = LocalDateTime.parse(times.get(i).asText(), ISO_HOUR);
            double temperature = temperatures.get(i).asDouble();
            double windSpeed = windSpeeds.get(i).asDouble();
            double precipitation = precipitations.get(i).asDouble();
            DayPart dayPart = resolveDayPart(timestamp, sunriseMap, sunsetMap);
            hours.add(new ForecastHour(timestamp, temperature, windSpeed, precipitation, dayPart, null));
        }
        return hours;
    }

    private DayPart resolveDayPart(LocalDateTime timestamp, Map<LocalDate, LocalTime> sunriseMap, Map<LocalDate, LocalTime> sunsetMap) {
        LocalDate date = timestamp.toLocalDate();
        LocalTime time = timestamp.toLocalTime();
        LocalTime sunrise = sunriseMap.get(date);
        LocalTime sunset = sunsetMap.get(date);
        if (sunrise == null || sunset == null) return DayPart.NIGHT;

        LocalTime dawnStart = sunrise.minusMinutes(30);
        LocalTime dawnEnd = sunrise.plusMinutes(30);
        LocalTime duskStart = sunset.minusMinutes(30);
        LocalTime duskEnd = sunset.plusMinutes(30);

        if (time.isBefore(dawnStart) || !time.isBefore(duskEnd)) return DayPart.NIGHT;
        if (time.isBefore(dawnEnd)) return DayPart.DAWN;
        if (time.isBefore(duskStart)) return DayPart.DAY;
        return DayPart.DUSK;
    }
}
