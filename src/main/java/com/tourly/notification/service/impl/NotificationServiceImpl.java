package com.tourly.notification.service.impl;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.notification.entity.Notification;
import com.tourly.notification.enums.NotificationTargetType;
import com.tourly.notification.enums.NotificationType;
import com.tourly.notification.repository.NotificationRepository;
import com.tourly.notification.service.EmailService;
import com.tourly.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public void send(Long userId, String title, String message,
                     NotificationType type, NotificationTargetType targetType, Long targetId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for notification"));

        // 1. Save in-app notification
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);

        notificationRepository.save(notification);

        log.info("Notification sent: userId={}, type={}, title={}", userId, type, title);

        // 2. Dispatch transactional email (async, non-blocking)
        try {
            emailService.sendStyledEmail(user.getEmail(), title, title, message);
        } catch (Exception e) {
            log.warn("Email dispatch failed for userId={}: {}", userId, e.getMessage());
        }
    }
}
