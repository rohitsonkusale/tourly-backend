package com.tourly.notification.service;

import com.tourly.notification.enums.NotificationTargetType;
import com.tourly.notification.enums.NotificationType;

/**
 * Core notification service — creates and stores in-app notifications.
 * When an email/push provider is configured, this service will also
 * dispatch to those channels.
 */
public interface NotificationService {

    /**
     * Send a notification to a specific user.
     *
     * @param userId     recipient user ID
     * @param title      short notification title
     * @param message    detailed notification message
     * @param type       notification category (PAYMENT, BOOKING, etc.)
     * @param targetType what entity this notification links to
     * @param targetId   the ID of that entity (e.g., bookingId)
     */
    void send(Long userId, String title, String message,
              NotificationType type, NotificationTargetType targetType, Long targetId);
}
