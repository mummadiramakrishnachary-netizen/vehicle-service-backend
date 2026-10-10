package app.vehicleservice.dto;

import app.vehicleservice.model.ServiceBooking;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceBookingResponse(
        Long id,
        LocalDateTime bookingDate,
        String problemDescription,
        String problemsFound,
        String status,
        BigDecimal estimatedCost,
        LocalDateTime createdAt,
        VehicleInfo vehicle,
        MechanicInfo mechanic,
        CustomerInfo customer
) {
    public record VehicleInfo(
            Long id,
            String registrationNumber,
            String brand,
            String model,
            Integer manufacturingYear,
            String fuelType
    ) {}

    public record MechanicInfo(
            Long id,
            String name,
            String email
    ) {}

    public record CustomerInfo(
            Long id,
            String name,
            String email
    ) {}

    public static ServiceBookingResponse from(ServiceBooking booking) {
        VehicleInfo vehicleInfo = null;
        if (booking.getVehicle() != null) {
            var vehicle = booking.getVehicle();
            vehicleInfo = new VehicleInfo(
                    vehicle.getId(),
                    vehicle.getRegistrationNumber(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getManufacturingYear(),
                    vehicle.getFuelType()
            );
        }

        MechanicInfo mechanicInfo = null;
        if (booking.getMechanic() != null) {
            var mechanic = booking.getMechanic();
            mechanicInfo = new MechanicInfo(
                    mechanic.getId(),
                    mechanic.getName(),
                    mechanic.getEmail()
            );
        }

        CustomerInfo customerInfo = null;
        if (booking.getCustomer() != null) {
            var customer = booking.getCustomer();
            customerInfo = new CustomerInfo(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail()
            );
        }

        return new ServiceBookingResponse(
                booking.getId(),
                booking.getBookingDate(),
                booking.getProblemDescription(),
                booking.getProblemsFound(),
                booking.getStatus() == null ? null : booking.getStatus().name(),
                booking.getEstimatedCost(),
                booking.getCreatedAt(),
                vehicleInfo,
                mechanicInfo,
                customerInfo
        );
    }
}