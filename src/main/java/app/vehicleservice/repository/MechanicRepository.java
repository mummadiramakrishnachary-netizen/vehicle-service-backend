package app.vehicleservice.repository;

import app.vehicleservice.model.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MechanicRepository extends JpaRepository<Mechanic, Long> {

    Optional<Mechanic> findByEmail(String email);

    List<Mechanic> findByAvailableTrue();
}