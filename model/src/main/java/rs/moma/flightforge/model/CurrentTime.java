package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CurrentTime {
    private LocalDateTime timestamp;
}
