package rs.moma.flightforge.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.Motor;

import java.util.List;

public interface MotorRepository extends JpaRepository<Motor, Long> {
    List<Motor> findByAvailableTrue();
}
