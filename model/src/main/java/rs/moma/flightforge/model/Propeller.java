package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Propeller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double diameter;
    private double pitch;
    private int bladeCount;
    private double weight;
    private double price;
    private boolean available;
    private String shopLink;
}
