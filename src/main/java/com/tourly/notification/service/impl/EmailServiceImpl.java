package com.tourly.notification.service.impl;

import com.tourly.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email delivery using Spring Mail (SMTP).
 * Runs async to avoid blocking the main thread during notification dispatch.
 * Gracefully fails if mail credentials are not configured (dev environments).
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:Roamaya}")
    private String fromName;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled || fromEmail == null || fromEmail.isBlank()) {
            log.debug("Email disabled or not configured — skipping send to: {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML
            mailSender.send(message);
            log.info("Email sent to: {} | subject: {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", to, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendStyledEmail(String to, String subject, String title, String message) {
        String html = buildTemplate(title, message);
        sendEmail(to, subject, html);
    }

    /**
     * Builds a clean, branded HTML email template matching Roamaya's design system.
     */
    private String buildTemplate(String title, String content) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#EEE8DF; font-family:'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#EEE8DF; padding:40px 20px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px; width:100%%;">
                      <!-- Header -->
                      <tr>
                        <td style="background-color:#2C365A; padding:24px 32px; border-radius:16px 16px 0 0;">
                          <h1 style="margin:0; color:#FFC107; font-size:22px; font-weight:700; font-style:italic;">
                            Roamaya
                          </h1>
                        </td>
                      </tr>
                      <!-- Content -->
                      <tr>
                        <td style="background-color:#ffffff; padding:32px; border-left:1px solid #e5e0d8; border-right:1px solid #e5e0d8;">
                          <h2 style="margin:0 0 16px; color:#2C365A; font-size:20px; font-weight:700;">
                            %s
                          </h2>
                          <div style="color:#2C365A; font-size:14px; line-height:1.7; opacity:0.75;">
                            %s
                          </div>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="background-color:#2C365A; padding:20px 32px; border-radius:0 0 16px 16px; text-align:center;">
                          <p style="margin:0; color:rgba(255,255,255,0.4); font-size:11px;">
                            &copy; 2026 Roamaya. All rights reserved.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(title, content);
    }
}
