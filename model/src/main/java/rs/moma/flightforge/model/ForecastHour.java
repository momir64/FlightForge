package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastHour {
    private LocalDateTime timestamp;
    private double temperature;
    private double windSpeed;
    private double precipitation;
    private DayPart dayPart;
}
