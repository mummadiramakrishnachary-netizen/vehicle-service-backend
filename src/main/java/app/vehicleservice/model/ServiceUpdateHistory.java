package app.vehicleservice.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_update_history")
public class ServiceUpdateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private ServiceBooking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(length = 1000)
    private String problemsFound;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ServiceUpdateHistory() {
    }

    public Long getId() {
        return id;
    }

    public ServiceBooking getBooking() {
        return booking;
    }

    public void setBooking(ServiceBooking booking) {
        this.booking = booking;
    }

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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
}
