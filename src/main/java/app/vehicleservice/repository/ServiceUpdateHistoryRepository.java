package app.vehicleservice.repository;

import app.vehicleservice.model.ServiceBooking;
import app.vehicleservice.model.ServiceUpdateHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceUpdateHistoryRepository
        extends JpaRepository<ServiceUpdateHistory, Long> {

    // Get history for a booking, oldest update first
    List<ServiceUpdateHistory> findByBookingOrderByUpdatedAtAsc(
            ServiceBooking booking
    );
}

