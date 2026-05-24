package rs.moma.flightforge.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.Battery;

import java.util.List;

public interface BatteryRepository extends JpaRepository<Battery, Long> {
    List<Battery> findByAvailableTrue();
}
