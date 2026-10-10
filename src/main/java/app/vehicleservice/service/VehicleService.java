
package app.vehicleservice.service;

import app.vehicleservice.dto.VehicleRequest;
import app.vehicleservice.model.User;
import app.vehicleservice.model.Vehicle;
import app.vehicleservice.repository.VehicleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Add a new vehicle
    public Vehicle addVehicle(VehicleRequest request, User customer) {

        if (vehicleRepository.existsByRegistrationNumber(
                request.getRegistrationNumber())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle with this registration number already exists"
            );
        }

        Vehicle vehicle = new Vehicle();

        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setManufacturingYear(request.getManufacturingYear());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setCustomer(customer);

        return vehicleRepository.save(vehicle);
    }

    // Get vehicles belonging to a customer
    public List<Vehicle> getCustomerVehicles(User customer) {
        return vehicleRepository.findByCustomer(customer);
    }

    // Update an existing vehicle
    public Vehicle updateVehicle(
            Long id,
            VehicleRequest request,
            User customer) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Vehicle not found"
                        )
                );

        // Ensure the vehicle belongs to the logged-in customer
        if (!Objects.equals(
                vehicle.getCustomer().getId(),
                customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to update this vehicle"
            );
        }

        // Check registration number if it is being changed
        if (!vehicle.getRegistrationNumber().equals(
                request.getRegistrationNumber())
                && vehicleRepository.existsByRegistrationNumber(
                        request.getRegistrationNumber())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Vehicle with this registration number already exists"
            );
        }

        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setManufacturingYear(request.getManufacturingYear());
        vehicle.setFuelType(request.getFuelType());

        return vehicleRepository.save(vehicle);
    }

    // Delete a vehicle belonging to the logged-in customer
    public void deleteVehicle(Long id, User customer) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Vehicle not found"
                        )
                );

        // Ensure the vehicle belongs to the logged-in customer
        if (!Objects.equals(
                vehicle.getCustomer().getId(),
                customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to delete this vehicle"
            );
        }

        vehicleRepository.delete(vehicle);
    }
}