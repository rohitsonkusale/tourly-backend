package com.tourly.payment.service.impl;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.enums.PaymentStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.payment.dto.request.AdminPayoutActionRequest;
import com.tourly.payment.dto.request.RequestPayoutRequest;
import com.tourly.payment.dto.response.AdminPayoutDetailResponse;
import com.tourly.payment.dto.response.BankAccountResponse;
import com.tourly.payment.dto.response.PayoutResponse;
import com.tourly.payment.dto.response.TripPayoutSummaryResponse;
import com.tourly.payment.entity.BankAccount;
import com.tourly.payment.entity.Payout;
import com.tourly.payment.enums.PayeeType;
import com.tourly.payment.enums.PayoutStatus;
import com.tourly.payment.enums.PayoutTranche;
import com.tourly.payment.repository.BankAccountRepository;
import com.tourly.payment.repository.PayoutRepository;
import com.tourly.payment.service.PayoutService;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.repository.TripRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayoutServiceImpl implements PayoutService {

    private final PayoutRepository payoutRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    // Platform commission rates
    // Standard: 15%, Founding Host (first 10 hosts, until 31 Dec 2026): 12%
    private static final BigDecimal STANDARD_COMMISSION_RATE = new BigDecimal("0.15");
    private static final BigDecimal FOUNDING_HOST_COMMISSION_RATE = new BigDecimal("0.12");
    private static final int FOUNDING_HOST_LIMIT = 10;
    private static final LocalDateTime FOUNDING_HOST_EXPIRY = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

    public PayoutServiceImpl(PayoutRepository payoutRepository,
                              BankAccountRepository bankAccountRepository,
                              BookingRepository bookingRepository,
                              UserRepository userRepository,
                              TripRepository tripRepository) {
        this.payoutRepository = payoutRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedActionException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =========================================
    // HOST OPERATIONS
    // =========================================

    @Override
    @Transactional
    public PayoutResponse requestPayout(RequestPayoutRequest request) {
        User host = getCurrentUser();

        // Find the trip
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Verify this trip belongs to this host
        boolean isOwner = (trip.getHost() != null && trip.getHost().getId().equals(host.getId()))
                || (trip.getPlanner() != null && trip.getPlanner().getId().equals(host.getId()));
        if (!isOwner) {
            throw new UnauthorizedActionException("This trip does not belong to you");
        }

        PayoutTranche tranche = request.getTranche();

        // Check no active pending payout request for this trip (any tranche)
        long activePending = payoutRepository.countActivePendingByTripIdAndHostId(trip.getId(), host.getId());
        if (activePending > 0) {
            throw new BadRequestException("A payout request is already pending for this trip. Please wait until it is approved or rejected.");
        }

        // Check that this tranche hasn't already been released
        long alreadyReleased = payoutRepository.countReleasedByTripIdAndTranche(trip.getId(), tranche);
        if (alreadyReleased > 0) {
            throw new BadRequestException("This payout tranche has already been released.");
        }

        // Validate tranche sequence: ADVANCE_1 must be released before ADVANCE_2, etc.
        if (tranche == PayoutTranche.ADVANCE_2) {
            long adv1Released = payoutRepository.countReleasedByTripIdAndTranche(trip.getId(), PayoutTranche.ADVANCE_1);
            if (adv1Released == 0) {
                throw new BadRequestException("Payout 1 (Advance) must be completed before requesting Payout 2.");
            }
        } else if (tranche == PayoutTranche.FINAL) {
            // Final can be requested once ADVANCE_1 is released (ADVANCE_2 is optional if no bookings in that window)
            long adv1Released = payoutRepository.countReleasedByTripIdAndTranche(trip.getId(), PayoutTranche.ADVANCE_1);
            if (adv1Released == 0) {
                // Check if advance_1 had zero eligible bookings (skip it)
                LocalDate departureDate = getDepartureDate(trip);
                List<Booking> adv1Bookings = getBookingsForTranche(trip, PayoutTranche.ADVANCE_1, departureDate);
                if (!adv1Bookings.isEmpty()) {
                    throw new BadRequestException("Payout 1 (Advance) must be completed before requesting Final Payout.");
                }
            }
        }

        // Get departure date and eligible bookings for this tranche
        LocalDate departureDate = getDepartureDate(trip);
        List<Booking> eligibleBookings = getBookingsForTranche(trip, tranche, departureDate);

        // For FINAL tranche: include ALL remaining revenue (including bookings from earlier tranches that weren't fully paid)
        BigDecimal grossAmount;
        BigDecimal commissionDeducted;
        BigDecimal netAmount;
        BigDecimal commissionRate = getCommissionRateForHost(host);

        if (tranche == PayoutTranche.FINAL) {
            // Final = total revenue - already released gross - commission on total
            List<Booking> allConfirmedBookings = getAllConfirmedPaidBookings(trip);
            BigDecimal totalTripRevenue = allConfirmedBookings.stream()
                    .map(Booking::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal alreadyReleasedGross = payoutRepository.sumGrossReleasedByTripId(trip.getId());
            grossAmount = totalTripRevenue.subtract(alreadyReleasedGross);

            if (grossAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("No remaining balance for this trip.");
            }

            // Commission is deducted from the final payout on the TOTAL trip revenue
            BigDecimal totalCommission = totalTripRevenue.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
            // Subtract any commission already deducted in earlier tranches (should be 0 for advance tranches)
            BigDecimal alreadyDeductedCommission = payoutRepository.findByTripIdAndHostId(trip.getId(), host.getId())
                    .stream()
                    .filter(p -> p.getStatus() == PayoutStatus.RELEASED)
                    .map(Payout::getCommissionDeducted)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            commissionDeducted = totalCommission.subtract(alreadyDeductedCommission);
            netAmount = grossAmount.subtract(commissionDeducted);
        } else {
            // ADVANCE_1 and ADVANCE_2: no commission deducted
            if (eligibleBookings.isEmpty()) {
                throw new BadRequestException("No eligible bookings found for this payout window.");
            }
            grossAmount = eligibleBookings.stream()
                    .map(Booking::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            commissionDeducted = BigDecimal.ZERO;
            netAmount = grossAmount;
        }

        // Validate eligibility date
        validateTrancheEligibility(tranche, departureDate);

        // Get bank account
        BankAccount bankAccount = null;
        if (request.getBankAccountId() != null) {
            bankAccount = bankAccountRepository.findByIdAndUserId(request.getBankAccountId(), host.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));
        } else {
            bankAccount = bankAccountRepository.findByUserIdAndIsPrimaryTrue(host.getId()).orElse(null);
        }

        // Create payout
        Payout payout = new Payout();
        payout.setTrip(trip);
        payout.setPayee(host);
        payout.setBankAccount(bankAccount);
        payout.setPayeeType(PayeeType.HOST);
        payout.setGrossAmount(grossAmount);
        payout.setCommissionDeducted(commissionDeducted);
        payout.setTdsDeducted(BigDecimal.ZERO);
        payout.setNetAmount(netAmount);
        payout.setStatus(PayoutStatus.REQUESTED);
        payout.setTranche(tranche);
        payout.setRequestedAt(LocalDateTime.now());

        Payout saved = payoutRepository.save(payout);
        return mapToResponse(saved, true);
    }

    // =========================================
    // TRIP PAYOUT SUMMARY (Host View)
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public TripPayoutSummaryResponse getTripPayoutSummary(Long tripId) {
        User host = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        boolean isOwner = (trip.getHost() != null && trip.getHost().getId().equals(host.getId()))
                || (trip.getPlanner() != null && trip.getPlanner().getId().equals(host.getId()));
        if (!isOwner) {
            throw new UnauthorizedActionException("This trip does not belong to you");
        }

        LocalDate departureDate = getDepartureDate(trip);
        BigDecimal commissionRate = getCommissionRateForHost(host);

        // Get all confirmed paid bookings
        List<Booking> allBookings = getAllConfirmedPaidBookings(trip);
        BigDecimal totalRevenue = allBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionAmount = totalRevenue.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalHostPayout = totalRevenue.subtract(commissionAmount);

        // Get existing payouts for this trip
        List<Payout> existingPayouts = payoutRepository.findByTripIdAndHostId(trip.getId(), host.getId());
        boolean hasActiveRequest = existingPayouts.stream()
                .anyMatch(p -> List.of(PayoutStatus.REQUESTED, PayoutStatus.PENDING, PayoutStatus.APPROVED, PayoutStatus.ON_HOLD).contains(p.getStatus()));

        // Build tranche details
        TripPayoutSummaryResponse.TrancheDetail advance1 = buildTrancheDetail(
                trip, PayoutTranche.ADVANCE_1, departureDate, existingPayouts, commissionRate, hasActiveRequest);
        TripPayoutSummaryResponse.TrancheDetail advance2 = buildTrancheDetail(
                trip, PayoutTranche.ADVANCE_2, departureDate, existingPayouts, commissionRate, hasActiveRequest);
        TripPayoutSummaryResponse.TrancheDetail finalPayout = buildFinalTrancheDetail(
                trip, departureDate, existingPayouts, commissionRate, totalRevenue, hasActiveRequest);

        // Build payout history
        List<TripPayoutSummaryResponse.PayoutHistoryItem> history = existingPayouts.stream()
                .filter(p -> p.getStatus() == PayoutStatus.RELEASED)
                .map(p -> {
                    TripPayoutSummaryResponse.PayoutHistoryItem item = new TripPayoutSummaryResponse.PayoutHistoryItem();
                    item.setPayoutId(p.getId());
                    item.setTranche(p.getTranche());
                    item.setTrancheLabel(getTrancheLabel(p.getTranche()));
                    item.setGrossAmount(p.getGrossAmount());
                    item.setCommissionDeducted(p.getCommissionDeducted());
                    item.setNetAmount(p.getNetAmount());
                    item.setReleasedAt(p.getReleasedAt());
                    item.setUtrNumber(p.getUtrNumber());
                    return item;
                })
                .collect(Collectors.toList());

        // Assemble response
        TripPayoutSummaryResponse response = new TripPayoutSummaryResponse();
        response.setTripId(trip.getId());
        response.setTripTitle(trip.getTitle());
        response.setDepartureDate(departureDate);
        response.setEndDate(trip.getEndDate());
        response.setTotalConfirmedBookings(allBookings.size());
        response.setTotalRevenue(totalRevenue);
        response.setCommissionRate(commissionRate);
        response.setCommissionAmount(commissionAmount);
        response.setTotalHostPayout(totalHostPayout);
        response.setAdvance1(advance1);
        response.setAdvance2(advance2);
        response.setFinalPayout(finalPayout);
        response.setPayoutHistory(history);
        response.setHasActiveRequest(hasActiveRequest);

        return response;
    }

    // =========================================
    // TRANCHE HELPER METHODS
    // =========================================

    /**
     * Get departure date from trip's batch (if booking has batch) or trip's startDate.
     */
    private LocalDate getDepartureDate(Trip trip) {
        // If trip has batches, use the earliest upcoming batch start date
        if (trip.getBatches() != null && !trip.getBatches().isEmpty()) {
            return trip.getBatches().stream()
                    .map(b -> b.getStartDate())
                    .filter(d -> d != null)
                    .min(LocalDate::compareTo)
                    .orElse(trip.getStartDate());
        }
        return trip.getStartDate();
    }

    /**
     * Get bookings eligible for a specific tranche based on confirmation date relative to departure.
     */
    private List<Booking> getBookingsForTranche(Trip trip, PayoutTranche tranche, LocalDate departureDate) {
        List<Booking> allBookings = getAllConfirmedPaidBookings(trip);

        return allBookings.stream().filter(b -> {
            LocalDate confirmedDate = b.getConfirmedAt() != null
                    ? b.getConfirmedAt().toLocalDate()
                    : (b.getCreatedAt() != null ? b.getCreatedAt().toLocalDate() : null);

            if (confirmedDate == null || departureDate == null) return false;

            long daysBeforeDeparture = ChronoUnit.DAYS.between(confirmedDate, departureDate);

            switch (tranche) {
                case ADVANCE_1:
                    // Bookings confirmed ≥30 days before departure
                    return daysBeforeDeparture >= 30;
                case ADVANCE_2:
                    // Bookings confirmed between 15-29 days before departure
                    return daysBeforeDeparture >= 15 && daysBeforeDeparture < 30;
                case FINAL:
                    // All remaining (confirmed <15 days before departure)
                    return daysBeforeDeparture < 15;
                default:
                    return false;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Get all confirmed and paid bookings for a trip.
     */
    private List<Booking> getAllConfirmedPaidBookings(Trip trip) {
        return bookingRepository.findByTripId(trip.getId()).stream()
                .filter(b -> (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED))
                .filter(b -> (b.getPaymentStatus() == PaymentStatus.FULLY_PAID || b.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID))
                .collect(Collectors.toList());
    }

    /**
     * Validate that the tranche can be requested based on current date vs departure.
     */
    private void validateTrancheEligibility(PayoutTranche tranche, LocalDate departureDate) {
        if (departureDate == null) return;

        LocalDate today = LocalDate.now();
        long daysUntilDeparture = ChronoUnit.DAYS.between(today, departureDate);

        switch (tranche) {
            case ADVANCE_1:
                // Eligible when ≤30 days until departure (i.e., the 30-day mark has passed or is today)
                if (daysUntilDeparture > 30) {
                    throw new BadRequestException("Payout 1 (Advance) is not yet eligible. It becomes available 30 days before departure.");
                }
                break;
            case ADVANCE_2:
                // Eligible when ≤15 days until departure
                if (daysUntilDeparture > 15) {
                    throw new BadRequestException("Payout 2 (Mid Advance) is not yet eligible. It becomes available 15 days before departure.");
                }
                break;
            case FINAL:
                // Eligible 48 hours after departure (departure date + 2 days)
                if (today.isBefore(departureDate.plusDays(2))) {
                    throw new BadRequestException("Final Payout is not yet eligible. It becomes available 48 hours after trip departure.");
                }
                break;
        }
    }

    /**
     * Build tranche detail for ADVANCE_1 or ADVANCE_2.
     */
    private TripPayoutSummaryResponse.TrancheDetail buildTrancheDetail(
            Trip trip, PayoutTranche tranche, LocalDate departureDate,
            List<Payout> existingPayouts, BigDecimal commissionRate, boolean hasActiveRequest) {

        TripPayoutSummaryResponse.TrancheDetail detail = new TripPayoutSummaryResponse.TrancheDetail();
        detail.setTranche(tranche);
        detail.setLabel(getTrancheLabel(tranche));

        // Release date
        if (tranche == PayoutTranche.ADVANCE_1) {
            detail.setReleaseDate(departureDate != null ? departureDate.minusDays(30) : null);
        } else {
            detail.setReleaseDate(departureDate != null ? departureDate.minusDays(15) : null);
        }

        // Get bookings for this tranche
        List<Booking> trancheBookings = getBookingsForTranche(trip, tranche, departureDate);
        detail.setBookingsCount(trancheBookings.size());

        BigDecimal grossAmount = trancheBookings.stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        detail.setGrossAmount(grossAmount);
        detail.setCommissionDeducted(BigDecimal.ZERO); // No commission on advance tranches
        detail.setNetAmount(grossAmount);

        // Booking summaries
        List<TripPayoutSummaryResponse.BookingSummary> bookingSummaries = trancheBookings.stream()
                .map(b -> {
                    TripPayoutSummaryResponse.BookingSummary bs = new TripPayoutSummaryResponse.BookingSummary();
                    bs.setBookingId(b.getId());
                    bs.setBookingRef(b.getBookingRef());
                    bs.setTravelerName(b.getTraveler() != null ? b.getTraveler().getFullName() : "Unknown");
                    bs.setAmount(b.getTotalPrice());
                    bs.setConfirmedAt(b.getConfirmedAt());
                    return bs;
                }).collect(Collectors.toList());
        detail.setBookings(bookingSummaries);

        // Determine status from existing payouts
        Payout existingPayout = existingPayouts.stream()
                .filter(p -> p.getTranche() == tranche)
                .findFirst().orElse(null);

        if (existingPayout != null) {
            detail.setPayoutId(existingPayout.getId());
            detail.setUtrNumber(existingPayout.getUtrNumber());
            detail.setReleasedAt(existingPayout.getReleasedAt());
            detail.setRequestedAt(existingPayout.getRequestedAt());
            detail.setAdminMessage(existingPayout.getAdminMessage());

            if (existingPayout.getStatus() == PayoutStatus.RELEASED) {
                detail.setStatus("RELEASED");
            } else if (existingPayout.getStatus() == PayoutStatus.REJECTED) {
                detail.setStatus("REJECTED");
            } else {
                detail.setStatus("REQUESTED");
            }
        } else if (trancheBookings.isEmpty()) {
            detail.setStatus("NO_BOOKINGS");
        } else {
            // Check if eligible by date
            LocalDate today = LocalDate.now();
            LocalDate releaseDate = detail.getReleaseDate();
            if (releaseDate != null && !today.isBefore(releaseDate) && !hasActiveRequest) {
                detail.setStatus("ELIGIBLE");
            } else {
                detail.setStatus("LOCKED");
            }
        }

        return detail;
    }

    /**
     * Build final tranche detail (commission deducted here).
     */
    private TripPayoutSummaryResponse.TrancheDetail buildFinalTrancheDetail(
            Trip trip, LocalDate departureDate, List<Payout> existingPayouts,
            BigDecimal commissionRate, BigDecimal totalRevenue, boolean hasActiveRequest) {

        TripPayoutSummaryResponse.TrancheDetail detail = new TripPayoutSummaryResponse.TrancheDetail();
        detail.setTranche(PayoutTranche.FINAL);
        detail.setLabel("Final Payout");
        detail.setReleaseDate(departureDate != null ? departureDate.plusDays(2) : null);

        // Gross = total revenue - already released gross
        BigDecimal alreadyReleasedGross = existingPayouts.stream()
                .filter(p -> p.getStatus() == PayoutStatus.RELEASED)
                .map(Payout::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossAmount = totalRevenue.subtract(alreadyReleasedGross);

        // Commission on total revenue, minus what was already deducted
        BigDecimal totalCommission = totalRevenue.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal alreadyDeductedCommission = existingPayouts.stream()
                .filter(p -> p.getStatus() == PayoutStatus.RELEASED)
                .map(Payout::getCommissionDeducted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commissionDeducted = totalCommission.subtract(alreadyDeductedCommission);
        BigDecimal netAmount = grossAmount.subtract(commissionDeducted);

        detail.setGrossAmount(grossAmount);
        detail.setCommissionDeducted(commissionDeducted);
        detail.setNetAmount(netAmount.compareTo(BigDecimal.ZERO) > 0 ? netAmount : BigDecimal.ZERO);

        // Bookings that fall into the final window (late bookings with no advance)
        List<Booking> finalBookings = getBookingsForTranche(trip, PayoutTranche.FINAL, departureDate);
        detail.setBookingsCount(finalBookings.size());
        List<TripPayoutSummaryResponse.BookingSummary> bookingSummaries = finalBookings.stream()
                .map(b -> {
                    TripPayoutSummaryResponse.BookingSummary bs = new TripPayoutSummaryResponse.BookingSummary();
                    bs.setBookingId(b.getId());
                    bs.setBookingRef(b.getBookingRef());
                    bs.setTravelerName(b.getTraveler() != null ? b.getTraveler().getFullName() : "Unknown");
                    bs.setAmount(b.getTotalPrice());
                    bs.setConfirmedAt(b.getConfirmedAt());
                    return bs;
                }).collect(Collectors.toList());
        detail.setBookings(bookingSummaries);

        // Status
        Payout existingPayout = existingPayouts.stream()
                .filter(p -> p.getTranche() == PayoutTranche.FINAL)
                .findFirst().orElse(null);

        if (existingPayout != null) {
            detail.setPayoutId(existingPayout.getId());
            detail.setUtrNumber(existingPayout.getUtrNumber());
            detail.setReleasedAt(existingPayout.getReleasedAt());
            detail.setRequestedAt(existingPayout.getRequestedAt());
            detail.setAdminMessage(existingPayout.getAdminMessage());

            if (existingPayout.getStatus() == PayoutStatus.RELEASED) {
                detail.setStatus("RELEASED");
            } else if (existingPayout.getStatus() == PayoutStatus.REJECTED) {
                detail.setStatus("REJECTED");
            } else {
                detail.setStatus("REQUESTED");
            }
        } else {
            LocalDate today = LocalDate.now();
            if (departureDate != null && !today.isBefore(departureDate.plusDays(2)) && !hasActiveRequest) {
                detail.setStatus("ELIGIBLE");
            } else {
                detail.setStatus("LOCKED");
            }
        }

        return detail;
    }

    private String getTrancheLabel(PayoutTranche tranche) {
        if (tranche == null) return "Unknown";
        switch (tranche) {
            case ADVANCE_1: return "Advance (Payout 1)";
            case ADVANCE_2: return "Mid Advance (Payout 2)";
            case FINAL: return "Final Payout";
            default: return "Unknown";
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getMyPayouts(Pageable pageable) {
        User host = getCurrentUser();
        return payoutRepository.findByPayeeIdOrderByRequestedAtDesc(host.getId(), pageable)
                .map(p -> mapToResponse(p, false));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getMyBankAccounts() {
        User user = getCurrentUser();
        return bankAccountRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToBankResponse)
                .collect(Collectors.toList());
    }

    // =========================================
    // ADMIN OPERATIONS
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getAllPayouts(Pageable pageable) {
        return payoutRepository.findAllByOrderByRequestedAtDesc(pageable)
                .map(p -> mapToResponse(p, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayoutResponse> getPayoutsByStatus(PayoutStatus status, Pageable pageable) {
        return payoutRepository.findByStatusOrderByRequestedAtDesc(status, pageable)
                .map(p -> mapToResponse(p, true));
    }

    @Override
    @Transactional
    public PayoutResponse processPayoutAction(Long payoutId, AdminPayoutActionRequest request) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout not found with ID: " + payoutId));

        payout.setStatus(request.getStatus());

        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            payout.setAdminMessage(request.getMessage());
        }

        if (request.getUtrNumber() != null && !request.getUtrNumber().isBlank()) {
            payout.setUtrNumber(request.getUtrNumber());
        }

        // Set timestamps based on status
        if (request.getStatus() == PayoutStatus.APPROVED || request.getStatus() == PayoutStatus.REJECTED) {
            payout.setProcessedAt(LocalDateTime.now());
        }
        if (request.getStatus() == PayoutStatus.RELEASED) {
            payout.setReleasedAt(LocalDateTime.now());
            if (payout.getProcessedAt() == null) {
                payout.setProcessedAt(LocalDateTime.now());
            }
        }

        Payout updated = payoutRepository.save(payout);
        return mapToResponse(updated, true);
    }

    // =========================================
    // ADMIN ENRICHED DETAIL
    // =========================================

    @Override
    @Transactional(readOnly = true)
    public AdminPayoutDetailResponse getPayoutDetail(Long payoutId) {
        Payout payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Payout not found with ID: " + payoutId));

        User host = payout.getPayee();

        // Determine tripId from trip or booking
        Long tripId;
        String tripTitle;
        if (payout.getTrip() != null) {
            tripId = payout.getTrip().getId();
            tripTitle = payout.getTrip().getTitle();
        } else if (payout.getBooking() != null) {
            tripId = payout.getBooking().getTrip().getId();
            tripTitle = payout.getBooking().getTrip().getTitle();
        } else {
            tripId = null;
            tripTitle = "Unknown";
        }

        AdminPayoutDetailResponse response = new AdminPayoutDetailResponse();

        // === Current Payout Info ===
        response.setId(payout.getId());
        if (payout.getBooking() != null) {
            response.setBookingId(payout.getBooking().getId());
            response.setBookingRef(payout.getBooking().getBookingRef());
        }
        response.setTripTitle(tripTitle);
        response.setTripId(tripId);
        response.setGrossAmount(payout.getGrossAmount());
        response.setCommissionDeducted(payout.getCommissionDeducted());
        response.setTdsDeducted(payout.getTdsDeducted());
        response.setNetAmount(payout.getNetAmount());
        response.setStatus(payout.getStatus());
        response.setTranche(payout.getTranche());
        response.setAdminMessage(payout.getAdminMessage());
        response.setUtrNumber(payout.getUtrNumber());
        response.setRequestedAt(payout.getRequestedAt());
        response.setProcessedAt(payout.getProcessedAt());
        response.setReleasedAt(payout.getReleasedAt());

        // Commission rate info
        BigDecimal commissionRate = getCommissionRateForHost(host);
        response.setCommissionRate(commissionRate);

        // === Bank Info ===
        if (payout.getBankAccount() != null) {
            BankAccount ba = payout.getBankAccount();
            response.setBankName(ba.getBankName());
            response.setAccountNumber(maskAccountNumber(ba.getAccountNumber()));
            response.setIfscCode(ba.getIfscCode());
            response.setAccountHolderName(ba.getAccountHolderName());
            response.setUpiId(ba.getUpiId());
        }

        // === Host Info ===
        response.setHostId(host.getId());
        response.setHostName(host.getFullName());
        response.setHostEmail(host.getEmail());
        response.setHostRegisteredAt(host.getCreatedAt());

        boolean isFounding = isFoundingHost(host);
        response.setFoundingHost(isFounding);
        response.setCommissionTier(isFounding ? "FOUNDING_12" : "STANDARD_15");

        // === Host Payout History ===
        long releasedCount = payoutRepository.countByPayeeIdAndStatus(host.getId(), PayoutStatus.RELEASED);
        BigDecimal totalReleased = payoutRepository.sumNetAmountByPayeeIdAndStatus(host.getId(), PayoutStatus.RELEASED);

        response.setTotalPayoutsForHost((int) releasedCount);
        response.setTotalReleasedAmountForHost(totalReleased);
        response.setFirstPayoutForHost(releasedCount == 0);

        // Previous payout (last released, excluding current)
        List<Payout> previousPayouts = payoutRepository.findLastReleasedPayoutForHost(host.getId(), payout.getId());
        if (!previousPayouts.isEmpty()) {
            Payout prev = previousPayouts.get(0);
            AdminPayoutDetailResponse.PayoutHistoryEntry entry = new AdminPayoutDetailResponse.PayoutHistoryEntry();
            entry.setPayoutId(prev.getId());
            // Get trip title from trip or booking
            if (prev.getTrip() != null) {
                entry.setTripTitle(prev.getTrip().getTitle());
            } else if (prev.getBooking() != null) {
                entry.setTripTitle(prev.getBooking().getTrip().getTitle());
                entry.setBookingRef(prev.getBooking().getBookingRef());
            }
            entry.setNetAmount(prev.getNetAmount());
            entry.setReleasedAt(prev.getReleasedAt());
            entry.setUtrNumber(prev.getUtrNumber());
            response.setPreviousPayout(entry);
        }

        // === Trip Stats ===
        if (tripId != null) {
            long tripBookings = bookingRepository.countByTripIdAndStatusIn(tripId,
                    List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED));
            BigDecimal tripRevenue = bookingRepository.sumRevenueByTripId(tripId);
            long tripPayoutCount = payoutRepository.countByTripId(tripId);
            BigDecimal tripReleasedAmount = payoutRepository.sumReleasedByTripId(tripId);

            response.setTotalBookingsForTrip((int) tripBookings);
            response.setTotalRevenueForTrip(tripRevenue);
            response.setTotalPayoutsForTrip((int) tripPayoutCount);
            response.setTotalReleasedForTrip(tripReleasedAmount);
        }

        return response;
    }

    // =========================================
    // HELPER: Commission Rate Logic
    // =========================================

    /**
     * Determines the commission rate for a host:
     * - First 10 registered hosts AND within 3 months of registration: 12%
     * - All others: 15%
     */
    private BigDecimal getCommissionRateForHost(User host) {
        if (isFoundingHost(host)) {
            return FOUNDING_HOST_COMMISSION_RATE;
        }
        return STANDARD_COMMISSION_RATE;
    }

    /**
     * Checks if host is a founding host (among first 10 hosts registered)
     * AND the current date is before 31 Dec 2026.
     */
    private boolean isFoundingHost(User host) {
        // Check if promotional period has expired (after 31 Dec 2026)
        if (LocalDateTime.now().isAfter(FOUNDING_HOST_EXPIRY)) {
            return false;
        }

        // Check if among first 10 hosts by registration order
        long hostsRegisteredBefore = userRepository.countHostsRegisteredBefore(host.getCreatedAt());
        return hostsRegisteredBefore < FOUNDING_HOST_LIMIT;
    }

    // =========================================
    // MAPPERS
    // =========================================

    private PayoutResponse mapToResponse(Payout payout, boolean includeHostInfo) {
        PayoutResponse response = new PayoutResponse();
        response.setId(payout.getId());

        // Trip-level payouts use trip directly; legacy payouts use booking's trip
        if (payout.getTrip() != null) {
            response.setTripTitle(payout.getTrip().getTitle());
            response.setTripId(payout.getTrip().getId());
        } else if (payout.getBooking() != null) {
            response.setTripTitle(payout.getBooking().getTrip().getTitle());
            response.setTripId(payout.getBooking().getTrip().getId());
        }

        if (payout.getBooking() != null) {
            response.setBookingId(payout.getBooking().getId());
            response.setBookingRef(payout.getBooking().getBookingRef());
        }

        response.setGrossAmount(payout.getGrossAmount());
        response.setCommissionDeducted(payout.getCommissionDeducted());
        response.setTdsDeducted(payout.getTdsDeducted());
        response.setNetAmount(payout.getNetAmount());
        response.setStatus(payout.getStatus());
        response.setTranche(payout.getTranche());
        response.setAdminMessage(payout.getAdminMessage());
        response.setUtrNumber(payout.getUtrNumber());
        response.setRequestedAt(payout.getRequestedAt());
        response.setProcessedAt(payout.getProcessedAt());
        response.setReleasedAt(payout.getReleasedAt());

        // Commission rate
        if (payout.getPayee() != null) {
            response.setCommissionRate(getCommissionRateForHost(payout.getPayee()));
        }

        if (payout.getBankAccount() != null) {
            BankAccount ba = payout.getBankAccount();
            response.setBankName(ba.getBankName());
            response.setAccountNumber(maskAccountNumber(ba.getAccountNumber()));
            response.setIfscCode(ba.getIfscCode());
            response.setAccountHolderName(ba.getAccountHolderName());
            response.setUpiId(ba.getUpiId());
        }

        if (includeHostInfo && payout.getPayee() != null) {
            response.setHostName(payout.getPayee().getFullName());
            response.setHostEmail(payout.getPayee().getEmail());
            response.setHostId(payout.getPayee().getId());
        }

        return response;
    }

    private BankAccountResponse mapToBankResponse(BankAccount ba) {
        BankAccountResponse response = new BankAccountResponse();
        response.setId(ba.getId());
        response.setAccountHolderName(ba.getAccountHolderName());
        response.setBankName(ba.getBankName());
        response.setAccountNumber(maskAccountNumber(ba.getAccountNumber()));
        response.setIfscCode(ba.getIfscCode());
        response.setUpiId(ba.getUpiId());
        response.setIsPrimary(ba.getIsPrimary());
        response.setIsVerified(ba.getIsVerified());
        response.setCreatedAt(ba.getCreatedAt());
        return response;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) return accountNumber;
        return "XXXX" + accountNumber.substring(accountNumber.length() - 4);
    }
}
