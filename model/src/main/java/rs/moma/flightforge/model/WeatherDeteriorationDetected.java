package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDeteriorationDetected {
    private SessionAlertType type;
    private ScheduledSession session;
    private ForecastHour hour;
}
