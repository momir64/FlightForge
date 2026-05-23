package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AirplaneSpecs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double length;
    private double wingspan;
    private double wingArea;
    private double wingLoading;
    private double wingCubicLoading;
    private double cg;
    private double dryWeight;
    private double allUpWeight;

    @Enumerated(EnumType.STRING)
    private ControlSurfaceType controlSurfaceType;

    private double recommendedTwFactor;
}
