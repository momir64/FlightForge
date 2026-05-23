package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildConfig {
    private UserPreferences userPreferences;
    private AirplaneSpecs airplane;

    private MotorConfiguration motorConfiguration;
    private ESC esc;
    private Battery battery;
    private List<Servo> servos;
    private Receiver receiver;

    private Double correctedDryWeight;
    private Double allUpWeight;
    private Double totalMaxConsumption;
    private Double twFactor;
    private Double wingLoading;
    private Double wclFactor;
    private Double estimatedFlightTime;
    private Double totalPrice;
    private Maneuverability maneuverability;
    private Double windSpeedLowerThreshold;
    private Double windSpeedUpperThreshold;
}
