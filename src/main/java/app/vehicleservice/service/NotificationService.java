package app.vehicleservice.service;

import app.vehicleservice.model.Notification;
import app.vehicleservice.model.ServiceBooking;
import app.vehicleservice.model.User;
import app.vehicleservice.repository.NotificationRepository;
import app.vehicleservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(
            User recipient,
            ServiceBooking booking,
            String message) {

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setBooking(booking);
        notification.setMessage(message);
        notification.setReadStatus(false);

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return notificationRepository
                .countByRecipient_IdAndReadStatusFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Notification notification = notificationRepository
                .findByIdAndRecipient_Id(notificationId, user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found or access denied"));

        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository
                .findByRecipient_IdOrderByCreatedAtDesc(user.getId());

        for (Notification notification : notifications) {
            notification.setReadStatus(true);
        }

        notificationRepository.saveAll(notifications);
    }
}