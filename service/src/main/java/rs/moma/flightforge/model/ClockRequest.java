package rs.moma.flightforge.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClockRequest {
    private LocalDateTime time;
}
