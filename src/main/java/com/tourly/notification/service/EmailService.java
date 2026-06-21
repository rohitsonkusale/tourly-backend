package com.tourly.notification.service;

/**
 * Transactional email delivery service.
 * Sends HTML emails for booking confirmations, payment reminders, etc.
 */
public interface EmailService {

    /**
     * Send a transactional email.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    plain text or HTML body
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send an HTML email using a styled template.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param title   heading inside the email template
     * @param message body content (supports HTML)
     */
    void sendStyledEmail(String to, String subject, String title, String message);
}
