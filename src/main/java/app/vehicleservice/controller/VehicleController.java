
package app.vehicleservice.controller;

import app.vehicleservice.dto.VehicleRequest;
import app.vehicleservice.model.User;
import app.vehicleservice.model.Vehicle;
import app.vehicleservice.service.UserService;
import app.vehicleservice.service.VehicleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserService userService;

    public VehicleController(
            VehicleService vehicleService,
            UserService userService) {

        this.vehicleService = vehicleService;
        this.userService = userService;
    }

    // Add a new vehicle
    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(
            @Valid @RequestBody VehicleRequest request,
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        Vehicle vehicle = vehicleService.addVehicle(
                request,
                customer
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicle);
    }

    // Get vehicles belonging to the logged-in customer
    @GetMapping
    public ResponseEntity<List<Vehicle>> getMyVehicles(
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        List<Vehicle> vehicles =
                vehicleService.getCustomerVehicles(customer);

        return ResponseEntity.ok(vehicles);
    }

    // Update a vehicle
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request,
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        Vehicle updatedVehicle =
                vehicleService.updateVehicle(
                        id,
                        request,
                        customer
                );

        return ResponseEntity.ok(updatedVehicle);
    }

    // Delete a vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(
            @PathVariable Long id,
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        vehicleService.deleteVehicle(id, customer);

        return ResponseEntity.noContent().build();
    }
}