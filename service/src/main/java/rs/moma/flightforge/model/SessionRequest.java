package rs.moma.flightforge.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
