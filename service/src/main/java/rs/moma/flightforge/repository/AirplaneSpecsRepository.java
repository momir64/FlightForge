package rs.moma.flightforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.AirplaneSpecs;

import java.util.Optional;

public interface AirplaneSpecsRepository extends JpaRepository<AirplaneSpecs, Long> {
    Optional<AirplaneSpecs> findByName(String name);
}
