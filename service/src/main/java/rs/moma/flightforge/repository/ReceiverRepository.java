package rs.moma.flightforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.moma.flightforge.model.Receiver;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
}
