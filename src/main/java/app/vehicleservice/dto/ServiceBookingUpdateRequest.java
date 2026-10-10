
package app.vehicleservice.dto;

import app.vehicleservice.model.BookingStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ServiceBookingUpdateRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;

    @Size(max = 1000, message = "Problem details must be at most 1000 characters")
    private String problemsFound;

    @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative")
    private BigDecimal estimatedCost;

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getProblemsFound() {
        return problemsFound;
    }

    public void setProblemsFound(String problemsFound) {
        this.problemsFound = problemsFound;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
}