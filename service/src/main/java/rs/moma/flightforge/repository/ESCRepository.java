package rs.moma.flightforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.ESC;

import java.util.List;

public interface ESCRepository extends JpaRepository<ESC, Long> {
    List<ESC> findByAvailableTrue();
}
