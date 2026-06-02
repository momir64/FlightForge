package rs.moma.flightforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.Propeller;

import java.util.List;

public interface PropellerRepository extends JpaRepository<Propeller, Long> {
    List<Propeller> findByAvailableTrue();
}
