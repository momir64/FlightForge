package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledSession {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BuildConfig build;

    public long getStartTimeMs() {
        return startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public long getDurationMillis() {
        return Duration.between(startTime, endTime).toMillis();
    }
}
