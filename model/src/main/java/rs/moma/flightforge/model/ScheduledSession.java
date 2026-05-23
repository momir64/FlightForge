package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledSession {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BuildConfig build;
}
