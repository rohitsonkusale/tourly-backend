package com.tourly.messaging.service;

import com.tourly.auth.entity.User;
import com.tourly.messaging.util.ContactMaskingUtil;

/**
 * Tracks contact-sharing violations and enforces the 3-strike system.
 */
public interface ContactViolationService {

    /**
     * Records a violation and returns the user's updated violation count.
     *
     * @param user           the user who attempted to share contact info
     * @param messageId      the ID of the saved (masked) message
     * @param originalContent the original unmasked content
     * @param maskResult     the masking result with detected type
     * @return the total violation count for this user (within 24h window)
     */
    long recordViolation(User user, Long messageId, String originalContent,
                         ContactMaskingUtil.MaskResult maskResult);

    /**
     * Checks if the user is currently blocked from sending messages
     * due to exceeding the violation threshold.
     *
     * @param user the user to check
     * @return true if user is blocked (3+ violations in 24 hours)
     */
    boolean isUserBlocked(User user);

    /**
     * Gets the violation count for a user in the last 24 hours.
     */
    long getRecentViolationCount(User user);
}
