package app.vehicleservice.service;

import app.vehicleservice.dto.ServiceBookingRequest;
import app.vehicleservice.dto.ServiceBookingUpdateRequest;
import app.vehicleservice.model.BookingStatus;
import app.vehicleservice.model.Mechanic;
import app.vehicleservice.model.Notification;
import app.vehicleservice.model.ServiceBooking;
import app.vehicleservice.model.ServiceUpdateHistory;
import app.vehicleservice.model.User;
import app.vehicleservice.model.Vehicle;
import app.vehicleservice.repository.MechanicRepository;
import app.vehicleservice.repository.NotificationRepository;
import app.vehicleservice.repository.ServiceBookingRepository;
import app.vehicleservice.repository.ServiceUpdateHistoryRepository;
import app.vehicleservice.repository.VehicleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceBookingService {

    private final ServiceBookingRepository serviceBookingRepository;
    private final VehicleRepository vehicleRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceUpdateHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    public ServiceBookingService(
            ServiceBookingRepository serviceBookingRepository,
            VehicleRepository vehicleRepository,
            MechanicRepository mechanicRepository,
            ServiceUpdateHistoryRepository historyRepository,
            NotificationRepository notificationRepository) {

        this.serviceBookingRepository = serviceBookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.mechanicRepository = mechanicRepository;
        this.historyRepository = historyRepository;
        this.notificationRepository = notificationRepository;
    }

    // =========================================================
    // CUSTOMER: CREATE BOOKING
    // =========================================================

    @Transactional
    public ServiceBooking createBooking(
            ServiceBookingRequest request,
            User customer) {

        Vehicle vehicle = vehicleRepository.findById(
                request.getVehicleId()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found"
                )
        );

        if (!Objects.equals(
                vehicle.getCustomer().getId(),
                customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to book service for this vehicle"
            );
        }

        ServiceBooking booking = new ServiceBooking();
        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setBookingDate(request.getBookingDate());
        booking.setProblemDescription(request.getProblemDescription());

        ServiceBooking savedBooking =
                serviceBookingRepository.save(booking);

        createHistory(
                savedBooking,
                BookingStatus.BOOKED,
                null,
                null,
                customer.getEmail()
        );

        return savedBooking;
    }

    // =========================================================
    // CUSTOMER: GET OWN BOOKINGS
    // =========================================================

    public List<ServiceBooking> getCustomerBookings(User customer) {
        return serviceBookingRepository.findByCustomer(customer);
    }

    // =========================================================
    // ADMIN: GET ALL BOOKINGS
    // =========================================================

    public List<ServiceBooking> getAllBookings() {
        return serviceBookingRepository.findAll();
    }

    // =========================================================
    // ADMIN / SERVICE ADVISOR: GET BOOKING BY ID
    // =========================================================

    public ServiceBooking getBookingById(Long bookingId) {
        return serviceBookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Booking not found"
                        )
                );
    }

    // =========================================================
    // CUSTOMER: GET BOOKING PROGRESS
    // =========================================================

    public ServiceBooking getCustomerBookingProgress(
            Long bookingId,
            User customer) {

        ServiceBooking booking = getBookingById(bookingId);

        if (!Objects.equals(
                booking.getCustomer().getId(),
                customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view this booking"
            );
        }

        return booking;
    }

    // =========================================================
    // CUSTOMER: GET BOOKING HISTORY
    // =========================================================

    public List<ServiceUpdateHistory> getCustomerBookingHistory(
            Long bookingId,
            User customer) {

        ServiceBooking booking = getBookingById(bookingId);

        if (!Objects.equals(
                booking.getCustomer().getId(),
                customer.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view this booking history"
            );
        }

        return historyRepository.findByBookingOrderByUpdatedAtAsc(booking);
    }

    // =========================================================
    // MECHANIC: GET ASSIGNED BOOKINGS
    // =========================================================

    public List<ServiceBooking> getMechanicBookings(String email) {

        Mechanic mechanic = mechanicRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Mechanic profile not found"
                        )
                );

        return serviceBookingRepository.findByMechanic(mechanic);
    }

    // =========================================================
    // ADMIN / SERVICE ADVISOR: ASSIGN MECHANIC
    // =========================================================

    @Transactional
    public ServiceBooking assignMechanic(
            Long bookingId,
            Long mechanicId,
            String assignedBy) {

        ServiceBooking booking = getBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot assign a mechanic to a completed booking"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot assign a mechanic to a cancelled booking"
            );
        }

        if (booking.getMechanic() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A mechanic is already assigned to this booking"
            );
        }

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Mechanic not found"
                        )
                );

        if (!mechanic.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mechanic is currently unavailable"
            );
        }

        booking.setMechanic(mechanic);
        booking.setStatus(BookingStatus.CONFIRMED);

        mechanic.setAvailable(false);
        mechanicRepository.save(mechanic);

        ServiceBooking savedBooking =
                serviceBookingRepository.save(booking);

        createHistory(
                savedBooking,
                BookingStatus.CONFIRMED,
                savedBooking.getProblemsFound(),
                savedBooking.getEstimatedCost(),
                assignedBy
        );

        sendCustomerNotification(
                savedBooking,
                "Mechanic " + mechanic.getName()
                        + " has been assigned to booking #"
                        + savedBooking.getId()
                        + ". Your booking is confirmed."
        );

        return savedBooking;
    }

    // =========================================================
    // ADMIN / SERVICE ADVISOR / MECHANIC: UPDATE BOOKING
    // =========================================================

    @Transactional
    public ServiceBooking updateBooking(
            Long bookingId,
            ServiceBookingUpdateRequest request,
            String email,
            String role) {

        ServiceBooking booking = getBookingById(bookingId);

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus newStatus = request.getStatus();

        if (currentStatus == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed booking cannot be modified"
            );
        }

        if (currentStatus == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cancelled booking cannot be modified"
            );
        }

        validateStatusTransition(currentStatus, newStatus);

        // Save old values before applying the update.
        String oldProblems = booking.getProblemsFound();
        BigDecimal oldCost = booking.getEstimatedCost();

        boolean statusChanged = currentStatus != newStatus;
        boolean problemsChanged =
                !Objects.equals(oldProblems, request.getProblemsFound());
        boolean costChanged =
                !Objects.equals(oldCost, request.getEstimatedCost());

        // =====================================================
        // AUTHORIZE USER
        // =====================================================

        Mechanic assignedMechanic = null;

        if ("MECHANIC".equals(role)) {

            assignedMechanic = mechanicRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Mechanic profile not found"
                            )
                    );

            if (booking.getMechanic() == null
                    || !Objects.equals(
                            booking.getMechanic().getId(),
                            assignedMechanic.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to update this booking"
                );
            }

        } else if (!"ADMIN".equals(role)
                && !"SERVICE_ADVISOR".equals(role)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to update this booking"
            );
        }

        // =====================================================
        // APPLY UPDATE
        // =====================================================

        booking.setStatus(newStatus);
        booking.setProblemsFound(request.getProblemsFound());
        booking.setEstimatedCost(request.getEstimatedCost());

        ServiceBooking savedBooking =
                serviceBookingRepository.save(booking);

        createHistory(
                savedBooking,
                newStatus,
                request.getProblemsFound(),
                request.getEstimatedCost(),
                email
        );

        // Release the mechanic after job completion.
        if (newStatus == BookingStatus.COMPLETED
                && savedBooking.getMechanic() != null) {

            Mechanic mechanic = savedBooking.getMechanic();
            mechanic.setAvailable(true);
            mechanicRepository.save(mechanic);
        }

        // =====================================================
        // NOTIFY CUSTOMER WHEN SOMETHING CHANGES
        // =====================================================

        if (statusChanged || problemsChanged || costChanged) {

            StringBuilder message = new StringBuilder();

            message.append("Update for booking #")
                    .append(savedBooking.getId())
                    .append(": ");

            if (statusChanged) {
                message.append("Status changed to ")
                        .append(newStatus)
                        .append(". ");
            }

            if (problemsChanged
                    && request.getProblemsFound() != null
                    && !request.getProblemsFound().isBlank()) {

                message.append("Problems found: ")
                        .append(request.getProblemsFound())
                        .append(". ");
            }

            if (costChanged) {
                message.append("Estimated cost updated to ")
                        .append(request.getEstimatedCost() == null
                                ? "not estimated"
                                : "₹" + request.getEstimatedCost())
                        .append(". ");
            }

            sendCustomerNotification(
                    savedBooking,
                    message.toString().trim()
            );
        }

        return savedBooking;
    }

    // =========================================================
    // CUSTOMER / ADMIN / SERVICE ADVISOR: CANCEL BOOKING
    // =========================================================

    @Transactional
    public ServiceBooking cancelBooking(
            Long bookingId,
            String email,
            String role) {

        ServiceBooking booking = getBookingById(bookingId);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed booking cannot be cancelled"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Booking is already cancelled"
            );
        }

        if ("CUSTOMER".equals(role)) {

            if (!Objects.equals(
                    booking.getCustomer().getEmail(),
                    email)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to cancel this booking"
                );
            }

            if (booking.getStatus() != BookingStatus.BOOKED) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Customer can cancel only a BOOKED service"
                );
            }

        } else if (!"ADMIN".equals(role)
                && !"SERVICE_ADVISOR".equals(role)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to cancel this booking"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        if (booking.getMechanic() != null) {
            Mechanic mechanic = booking.getMechanic();
            mechanic.setAvailable(true);
            mechanicRepository.save(mechanic);
        }

        ServiceBooking savedBooking =
                serviceBookingRepository.save(booking);

        createHistory(
                savedBooking,
                BookingStatus.CANCELLED,
                savedBooking.getProblemsFound(),
                savedBooking.getEstimatedCost(),
                email
        );

        sendCustomerNotification(
                savedBooking,
                "Booking #" + savedBooking.getId()
                        + " has been cancelled."
        );

        return savedBooking;
    }

    // =========================================================
    // VALIDATE BOOKING STATUS TRANSITIONS
    // =========================================================

    private void validateStatusTransition(
            BookingStatus currentStatus,
            BookingStatus newStatus) {

        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = false;

        switch (currentStatus) {
            case BOOKED:
                valid = newStatus == BookingStatus.CONFIRMED
                        || newStatus == BookingStatus.CANCELLED;
                break;

            case CONFIRMED:
                valid = newStatus == BookingStatus.IN_PROGRESS
                        || newStatus == BookingStatus.WAITING_FOR_PARTS
                        || newStatus == BookingStatus.CANCELLED;
                break;

            case IN_PROGRESS:
                valid = newStatus == BookingStatus.WAITING_FOR_PARTS
                        || newStatus == BookingStatus.COMPLETED
                        || newStatus == BookingStatus.CANCELLED;
                break;

            case WAITING_FOR_PARTS:
                valid = newStatus == BookingStatus.IN_PROGRESS
                        || newStatus == BookingStatus.COMPLETED
                        || newStatus == BookingStatus.CANCELLED;
                break;

            case COMPLETED:
            case CANCELLED:
                valid = false;
                break;
        }

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid booking status transition: "
                            + currentStatus + " -> " + newStatus
            );
        }
    }

    // =========================================================
    // CREATE SERVICE UPDATE HISTORY
    // =========================================================

    private void createHistory(
            ServiceBooking booking,
            BookingStatus status,
            String problemsFound,
            BigDecimal estimatedCost,
            String updatedBy) {

        ServiceUpdateHistory history = new ServiceUpdateHistory();

        history.setBooking(booking);
        history.setStatus(status);
        history.setProblemsFound(problemsFound);
        history.setEstimatedCost(estimatedCost);
        history.setUpdatedBy(updatedBy);

        historyRepository.save(history);
    }

    // =========================================================
    // CREATE CUSTOMER NOTIFICATION
    // =========================================================

    private void sendCustomerNotification(
            ServiceBooking booking,
            String message) {

        Notification notification = new Notification();

        notification.setRecipient(booking.getCustomer());
        notification.setBooking(booking);
        notification.setMessage(message);
        notification.setReadStatus(false);

        notificationRepository.save(notification);
    }
}
