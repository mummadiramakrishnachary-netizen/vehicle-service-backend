package app.vehicleservice.controller;

import app.vehicleservice.model.Notification;
import app.vehicleservice.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('CUSTOMER')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Map<String, Object> getMyNotifications(
            Authentication authentication) {

        String email = authentication.getName();

        List<Notification> notifications =
                notificationService.getMyNotifications(email);

        long unreadCount =
                notificationService.getUnreadCount(email);

        List<Map<String, Object>> items = notifications.stream()
                .map(notification -> Map.<String, Object>of(
                        "id", notification.getId(),
                        "bookingId", notification.getBooking() == null
                                ? 0L
                                : notification.getBooking().getId(),
                        "message", notification.getMessage(),
                        "read", notification.isReadStatus(),
                        "createdAt", notification.getCreatedAt() == null
                                ? LocalDateTime.now()
                                : notification.getCreatedAt()
                ))
                .toList();

        return Map.of(
                "notifications", items,
                "unreadCount", unreadCount
        );
    }

    @PutMapping("/{id}/read")
    public Map<String, String> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        notificationService.markAsRead(id, authentication.getName());

        return Map.of("message", "Notification marked as read");
    }

    @PutMapping("/read-all")
    public Map<String, String> markAllAsRead(
            Authentication authentication) {

        notificationService.markAllAsRead(authentication.getName());

        return Map.of("message", "All notifications marked as read");
    }
}