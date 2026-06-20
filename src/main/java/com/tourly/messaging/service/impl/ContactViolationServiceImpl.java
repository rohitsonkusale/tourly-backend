package com.tourly.messaging.service.impl;

import com.tourly.auth.entity.User;
import com.tourly.messaging.entity.ContactViolation;
import com.tourly.messaging.repository.ContactViolationRepository;
import com.tourly.messaging.service.ContactViolationService;
import com.tourly.messaging.util.ContactMaskingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ContactViolationServiceImpl implements ContactViolationService {

    private static final Logger log = LoggerFactory.getLogger(ContactViolationServiceImpl.class);

    /** Number of violations within the time window before chat is blocked */
    private static final int VIOLATION_THRESHOLD = 3;

    /** Time window for counting violations (24 hours) */
    private static final int VIOLATION_WINDOW_HOURS = 24;

    private final ContactViolationRepository violationRepository;

    public ContactViolationServiceImpl(ContactViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    @Override
    public long recordViolation(User user, Long messageId, String originalContent,
                                ContactMaskingUtil.MaskResult maskResult) {
        // Create and save the violation record (audit log)
        ContactViolation violation = new ContactViolation();
        violation.setUser(user);
        violation.setMessageId(messageId);
        violation.setOriginalContent(originalContent);
        violation.setMaskedContent(maskResult.getMaskedContent());
        violation.setDetectedType(
                maskResult.getDetectedType() != null ? maskResult.getDetectedType() : "UNKNOWN"
        );
        violationRepository.save(violation);

        // Get the updated count within the time window
        long recentCount = getRecentViolationCount(user);

        // Log the violation for monitoring
        log.warn("Contact violation #{} by user {} (type: {}). Message ID: {}",
                recentCount, user.getId(), violation.getDetectedType(), messageId);

        return recentCount;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserBlocked(User user) {
        return getRecentViolationCount(user) >= VIOLATION_THRESHOLD;
    }

    @Override
    @Transactional(readOnly = true)
    public long getRecentViolationCount(User user) {
        LocalDateTime since = LocalDateTime.now().minusHours(VIOLATION_WINDOW_HOURS);
        return violationRepository.countByUserSince(user, since);
    }
}
