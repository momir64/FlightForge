package rs.moma.flightforge.model;

import lombok.Data;

@Data
public class BuildRequest {
    private Long airplaneId;
    private double foamboardWeight;
    private double scaleFactor;
    private Double minTWRatio;
    private Integer minFlightTime;
    private Priority priority;
    private boolean metalGearsPreference;
    private String location;
    private int sessionDuration;

    private Long motorConfigurationId;
    private Long escId;
    private Long batteryId;
    private Long servoId;
    private double receiverWeight;
    private double receiverPowerConsumption;
}
