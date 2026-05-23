package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MotorConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "motor_id")
    private Motor motor;

    @ManyToOne
    @JoinColumn(name = "propeller_id")
    private Propeller propeller;

    private int cellCount;
    private double thrust;
    private double maxCurrent;
}
