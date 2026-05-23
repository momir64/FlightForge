package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionAlert {
    private SessionAlertType type;
    private String message;
    private ScheduledSession session;
    private ForecastHour triggeredBy;
}
