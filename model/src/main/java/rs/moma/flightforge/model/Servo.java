package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Servo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double torque;

    @Enumerated(EnumType.STRING)
    private GearType gearType;

    private int sizeCategory;
    private double idleCurrent;
    private double noLoadCurrent;
    private double stallCurrent;
    private double weight;
    private double price;
    private boolean available;
    private String shopLink;
}
