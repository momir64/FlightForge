package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SessionSuggestion {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long idealHours;
    private List<ForecastHour> hours;
}
