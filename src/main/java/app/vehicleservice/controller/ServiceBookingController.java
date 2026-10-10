package app.vehicleservice.controller;

import app.vehicleservice.dto.ServiceBookingRequest;
import app.vehicleservice.dto.ServiceBookingResponse;
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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ResponseEntity<ServiceBookingResponse> createBooking(
            @Valid @RequestBody ServiceBookingRequest request,
            Authentication authentication) {

        User customer = userService.getUserByEmail(authentication.getName());

        ServiceBooking booking =
                serviceBookingService.createBooking(request, customer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ServiceBookingResponse.from(booking));
    }

    // CUSTOMER: GET OWN BOOKINGS
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ServiceBookingResponse>> getMyBookings(
            Authentication authentication) {

        User customer = userService.getUserByEmail(authentication.getName());

        List<ServiceBookingResponse> bookings =
                serviceBookingService.getCustomerBookings(customer)
                        .stream()
                        .map(ServiceBookingResponse::from)
                        .toList();

        return ResponseEntity.ok(bookings);
    }

    // ADMIN / SERVICE ADVISOR: GET ALL BOOKINGS
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ServiceBookingResponse>> getAllBookings() {

        List<ServiceBookingResponse> bookings =
                serviceBookingService.getAllBookings()
                        .stream()
                        .map(ServiceBookingResponse::from)
                        .toList();

        return ResponseEntity.ok(bookings);
    }

    // ADMIN / SERVICE ADVISOR: GET BOOKING BY ID
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<ServiceBookingResponse> getBookingById(
            @PathVariable Long id) {

        ServiceBooking booking = serviceBookingService.getBookingById(id);

        return ResponseEntity.ok(ServiceBookingResponse.from(booking));
    }

    // CUSTOMER: GET BOOKING PROGRESS
    @GetMapping("/{id}/progress")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ServiceBookingResponse> getBookingProgress(
            @PathVariable Long id,
            Authentication authentication) {

        User customer = userService.getUserByEmail(authentication.getName());

        ServiceBooking booking =
                serviceBookingService.getCustomerBookingProgress(id, customer);

        return ResponseEntity.ok(ServiceBookingResponse.from(booking));
    }

    // CUSTOMER: GET SERVICE HISTORY
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ServiceUpdateHistory>> getBookingHistory(
            @PathVariable Long id,
            Authentication authentication) {

        User customer = userService.getUserByEmail(authentication.getName());

        return ResponseEntity.ok(
                serviceBookingService.getCustomerBookingHistory(id, customer)
        );
    }

    // MECHANIC: GET ASSIGNED BOOKINGS
    @GetMapping("/mechanic")
    @PreAuthorize("hasRole('MECHANIC')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ServiceBookingResponse>> getMechanicBookings(
            Authentication authentication) {

        List<ServiceBookingResponse> bookings =
                serviceBookingService.getMechanicBookings(authentication.getName())
                        .stream()
                        .map(ServiceBookingResponse::from)
                        .toList();

        return ResponseEntity.ok(bookings);
    }

    // ADMIN / SERVICE ADVISOR: ASSIGN MECHANIC
    @PutMapping("/{id}/assign-mechanic/{mechanicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR')")
    @Transactional
    public ResponseEntity<ServiceBookingResponse> assignMechanic(
            @PathVariable Long id,
            @PathVariable Long mechanicId,
            Authentication authentication) {

        ServiceBooking booking = serviceBookingService.assignMechanic(
                id, mechanicId, authentication.getName()
        );

        return ResponseEntity.ok(ServiceBookingResponse.from(booking));
    }

    // ADMIN / SERVICE ADVISOR / MECHANIC: UPDATE BOOKING
    @PutMapping("/{id}/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_ADVISOR', 'MECHANIC')")
    @Transactional
    public ResponseEntity<ServiceBookingResponse> updateBooking(
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

        ServiceBooking booking = serviceBookingService.updateBooking(
                id, request, email, role
        );

        return ResponseEntity.ok(ServiceBookingResponse.from(booking));
    }

    // CUSTOMER / ADMIN / SERVICE ADVISOR: CANCEL BOOKING
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SERVICE_ADVISOR')")
    @Transactional
    public ResponseEntity<ServiceBookingResponse> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .orElseThrow()
                .getAuthority()
                .replace("ROLE_", "");

        ServiceBooking booking =
                serviceBookingService.cancelBooking(id, email, role);

        return ResponseEntity.ok(ServiceBookingResponse.from(booking));
    }
}