package com.tourly.notification.controller;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.dto.ApiResponse;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.notification.dto.NotificationResponse;
import com.tourly.notification.entity.Notification;
import com.tourly.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notification APIs")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =====================================
    // GET MY NOTIFICATIONS (paginated)
    // =====================================
    @GetMapping
    @Operation(summary = "Get notifications", description = "Returns paginated notifications for the current user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = getCurrentUser();
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));

        List<NotificationResponse> response = notifications.getContent().stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", response));
    }

    // =====================================
    // GET UNREAD COUNT
    // =====================================
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Returns the number of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        User user = getCurrentUser();
        long count = notificationRepository.countByUserIdAndReadAtIsNull(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Unread count", Map.of("count", count)));
    }

    // =====================================
    // MARK AS READ
    // =====================================
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        User user = getCurrentUser();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("Not your notification");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }

    // =====================================
    // MARK ALL AS READ
    // =====================================
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User user = getCurrentUser();

        List<Notification> unread = notificationRepository
                .findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(user.getId());

        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unread) {
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);

        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType() != null ? n.getType().name() : null);
        dto.setTargetType(n.getTargetType() != null ? n.getTargetType().name() : null);
        dto.setTargetId(n.getTargetId());
        dto.setRead(n.getReadAt() != null);
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
