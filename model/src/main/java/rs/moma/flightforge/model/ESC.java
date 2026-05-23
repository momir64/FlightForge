package rs.moma.flightforge.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ESC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double continuousCurrent;
    private double burstCurrent;
    private int minCellCount;
    private int maxCellCount;
    private double becOutputVoltage;
    private double becMaxCurrent;
    private double weight;
    private double price;
    private boolean available;
    private String shopLink;
}
