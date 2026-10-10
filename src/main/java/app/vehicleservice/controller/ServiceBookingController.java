package app.vehicleservice.controller;

import app.vehicleservice.dto.ServiceBookingRequest;
import app.vehicleservice.dto.ServiceBookingUpdateRequest;
import app.vehicleservice.model.ServiceBooking;
import app.vehicleservice.model.ServiceUpdateHistory;
import app.vehicleservice.model.User;
import app.vehicleservice.service.ServiceBookingService;
import app.vehicleservice.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class ServiceBookingController {

    private final ServiceBookingService serviceBookingService;
    private final UserService userService;

    public ServiceBookingController(
            ServiceBookingService serviceBookingService,
            UserService userService) {
        this.serviceBookingService = serviceBookingService;
        this.userService = userService;
    }

    // CUSTOMER: CREATE BOOKING
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ServiceBooking> createBooking(
            @Valid @RequestBody ServiceBookingRequest request,
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        ServiceBooking booking = serviceBookingService.createBooking(
                request, customer
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    // CUSTOMER: GET OWN BOOKINGS
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ServiceBooking>> getMyBookings(
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        return ResponseEntity.ok(
                serviceBookingService.getCustomerBookings(customer)
        );
    }

    // ADMIN / SERVICE ADVISOR: GET ALL BOOKINGS
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<List<ServiceBooking>> getAllBookings() {

        return ResponseEntity.ok(
                serviceBookingService.getAllBookings()
        );
    }

    // ADMIN / SERVICE ADVISOR: GET BOOKING BY ID
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<ServiceBooking> getBookingById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                serviceBookingService.getBookingById(id)
        );
    }

    // CUSTOMER: GET BOOKING PROGRESS
    @GetMapping("/{id}/progress")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ServiceBooking> getBookingProgress(
            @PathVariable Long id,
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        return ResponseEntity.ok(
                serviceBookingService.getCustomerBookingProgress(
                        id, customer
                )
        );
    }

    // CUSTOMER: GET SERVICE HISTORY
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ServiceUpdateHistory>> getBookingHistory(
            @PathVariable Long id,
            Authentication authentication) {

        User customer = userService.getUserByEmail(
                authentication.getName()
        );

        return ResponseEntity.ok(
                serviceBookingService.getCustomerBookingHistory(
                        id, customer
                )
        );
    }

    // MECHANIC: GET ASSIGNED BOOKINGS
    @GetMapping("/mechanic")
    @PreAuthorize("hasRole('MECHANIC')")
    public ResponseEntity<List<ServiceBooking>> getMechanicBookings(
            Authentication authentication) {

        return ResponseEntity.ok(
                serviceBookingService.getMechanicBookings(
                        authentication.getName()
                )
        );
    }

    // ADMIN / SERVICE ADVISOR: ASSIGN MECHANIC
    @PutMapping("/{id}/assign-mechanic/{mechanicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<ServiceBooking> assignMechanic(
            @PathVariable Long id,
            @PathVariable Long mechanicId,
            Authentication authentication) {

        ServiceBooking updatedBooking =
                serviceBookingService.assignMechanic(
                        id, mechanicId, authentication.getName()
                );

        return ResponseEntity.ok(updatedBooking);
    }

    // ADMIN / SERVICE ADVISOR / MECHANIC: UPDATE BOOKING
    @PutMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR', 'MECHANIC')")
    public ResponseEntity<ServiceBooking> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody ServiceBookingUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow()
                .getAuthority()
                .replace("ROLE_", "");

        ServiceBooking updatedBooking =
                serviceBookingService.updateBooking(
                        id, request, email, role
                );

        return ResponseEntity.ok(updatedBooking);
    }

    // CUSTOMER / ADMIN / SERVICE ADVISOR: CANCEL BOOKING
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SERVICE_ADVISOR')")
    public ResponseEntity<ServiceBooking> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow()
                .getAuthority()
                .replace("ROLE_", "");

        ServiceBooking cancelledBooking =
                serviceBookingService.cancelBooking(
                        id, email, role
                );

        return ResponseEntity.ok(cancelledBooking);
    }
}
