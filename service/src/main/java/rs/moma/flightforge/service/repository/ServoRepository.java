package rs.moma.flightforge.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.GearType;
import rs.moma.flightforge.model.Servo;

import java.util.List;

public interface ServoRepository extends JpaRepository<Servo, Long> {
    List<Servo> findByAvailableTrue();
    List<Servo> findByAvailableTrueAndGearType(GearType gearType);
}
