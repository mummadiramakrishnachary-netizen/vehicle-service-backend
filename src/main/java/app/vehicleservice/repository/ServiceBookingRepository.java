package app.vehicleservice.repository;

import app.vehicleservice.model.Mechanic;
import app.vehicleservice.model.ServiceBooking;
import app.vehicleservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceBookingRepository
        extends JpaRepository<ServiceBooking, Long> {

    // Find all bookings belonging to a customer
    List<ServiceBooking> findByCustomer(User customer);

    // Find all bookings assigned to a mechanic
    List<ServiceBooking> findByMechanic(Mechanic mechanic);
}