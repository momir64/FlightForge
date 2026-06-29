package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReminderSent {
    private LocalDateTime sessionStartTime;
}
