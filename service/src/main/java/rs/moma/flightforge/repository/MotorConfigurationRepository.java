package rs.moma.flightforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.MotorConfiguration;

import java.util.List;

public interface MotorConfigurationRepository extends JpaRepository<MotorConfiguration, Long> {
    List<MotorConfiguration> findByCellCount(int cellCount);
}
