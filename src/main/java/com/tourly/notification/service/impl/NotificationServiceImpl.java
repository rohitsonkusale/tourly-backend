package com.tourly.notification.service.impl;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.notification.entity.Notification;
import com.tourly.notification.enums.NotificationTargetType;
import com.tourly.notification.enums.NotificationType;
import com.tourly.notification.repository.NotificationRepository;
import com.tourly.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void send(Long userId, String title, String message,
                     NotificationType type, NotificationTargetType targetType, Long targetId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for notification"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);

        notificationRepository.save(notification);

        log.info("Notification sent: userId={}, type={}, title={}", userId, type, title);

        // TODO: When email provider is configured, dispatch email here.
        // Example: emailService.sendTransactional(user.getEmail(), title, message);

        // TODO: When push notifications are configured, dispatch push here.
        // Example: pushService.send(userId, title, message);
    }
}
