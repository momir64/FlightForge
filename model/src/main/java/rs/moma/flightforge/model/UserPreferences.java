package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    private double foamboardWeight;
    private double scaleFactor;
    private Double minTWRatio;
    private Double minFlightTime;
    private Priority priority;
    private boolean metalGearsPreference;
    private String location;
}
