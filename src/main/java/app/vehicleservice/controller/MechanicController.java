package app.vehicleservice.controller;

import app.vehicleservice.model.Mechanic;
import app.vehicleservice.service.MechanicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanic")
public class MechanicController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    // Mechanic dashboard
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MECHANIC')")
    public String dashboard() {
        return "Mechanic dashboard access granted";
    }

    // Add mechanic
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Mechanic> addMechanic(
            @RequestBody Mechanic mechanic) {

        Mechanic savedMechanic = mechanicService.addMechanic(mechanic);

        return new ResponseEntity<>(
                savedMechanic,
                HttpStatus.CREATED
        );
    }

    // Get all mechanics
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<List<Mechanic>> getAllMechanics() {
        return ResponseEntity.ok(
                mechanicService.getAllMechanics()
        );
    }

    // Get available mechanics
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<List<Mechanic>> getAvailableMechanics() {
        return ResponseEntity.ok(
                mechanicService.getAvailableMechanics()
        );
    }

    // Get mechanic by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR', 'MECHANIC')")
    public ResponseEntity<Mechanic> getMechanicById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                mechanicService.getMechanicById(id)
        );
    }

    // Update mechanic availability
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
    public ResponseEntity<Mechanic> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {

        return ResponseEntity.ok(
                mechanicService.updateAvailability(id, available)
        );
    }
}