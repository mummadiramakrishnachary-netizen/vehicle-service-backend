package app.vehicleservice.service;

import app.vehicleservice.model.Mechanic;
import app.vehicleservice.repository.MechanicRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MechanicService {

    private final MechanicRepository mechanicRepository;

    public MechanicService(MechanicRepository mechanicRepository) {
        this.mechanicRepository = mechanicRepository;
    }

    public Mechanic addMechanic(Mechanic mechanic) {

        if (mechanicRepository.findByEmail(mechanic.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Mechanic with this email already exists"
            );
        }

        return mechanicRepository.save(mechanic);
    }

    public boolean existsByEmail(String email) {
        return mechanicRepository.findByEmail(email).isPresent();
    }

    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }

    public List<Mechanic> getAvailableMechanics() {
        return mechanicRepository.findByAvailableTrue();
    }

    public Mechanic getMechanicById(Long id) {

        return mechanicRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Mechanic not found"
                        )
                );
    }

    public Mechanic updateAvailability(Long id, boolean available) {

        Mechanic mechanic = getMechanicById(id);

        mechanic.setAvailable(available);

        return mechanicRepository.save(mechanic);
    }
}