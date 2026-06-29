package rs.moma.flightforge.model;

import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
public class SessionAlert {
    private SessionAlertType type;
    private String message;
    private ScheduledSession session;
    private ForecastHour triggeredBy;

    public SessionAlert(SessionAlertType type, String message, ScheduledSession session, ForecastHour triggeredBy) {
        this.type = type;
        this.message = message;
        this.session = session;
        this.triggeredBy = triggeredBy;
    }
}
