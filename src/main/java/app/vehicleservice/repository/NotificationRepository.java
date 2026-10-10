
package app.vehicleservice.repository;

import app.vehicleservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(
            Long recipientId
    );

    Optional<Notification> findByIdAndRecipient_Id(
            Long id,
            Long recipientId
    );

    long countByRecipient_IdAndReadStatusFalse(
            Long recipientId
    );
}