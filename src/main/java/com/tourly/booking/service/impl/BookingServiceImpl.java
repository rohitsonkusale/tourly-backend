package com.tourly.booking.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.dto.request.CancelBookingRequest;
import com.tourly.booking.dto.request.CreateBookingRequest;
import com.tourly.booking.dto.response.BookingDetailResponse;
import com.tourly.booking.dto.response.BookingResponse;
import com.tourly.booking.dto.response.HostBookingResponse;
import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.enums.PaymentStatus;
import com.tourly.booking.mapper.BookingMapper;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.booking.service.BookingService;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.ScheduleType;
import com.tourly.payment.repository.PaymentStageRepository;
import com.tourly.payment.service.PaymentStageCalculator;
import com.tourly.payment.service.RefundService;
import com.tourly.notification.service.PaymentNotificationService;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.entity.TripMedia;
import com.tourly.trip.repository.TripRepository;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final PaymentStageRepository paymentStageRepository;
    private final PaymentStageCalculator paymentStageCalculator;
    private final PaymentNotificationService paymentNotificationService;
    private final RefundService refundService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            TripRepository tripRepository,
            UserRepository userRepository,
            PaymentStageRepository paymentStageRepository,
            PaymentStageCalculator paymentStageCalculator,
            PaymentNotificationService paymentNotificationService,
            RefundService refundService) {

        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.paymentStageRepository = paymentStageRepository;
        this.paymentStageCalculator = paymentStageCalculator;
        this.paymentNotificationService = paymentNotificationService;
        this.refundService = refundService;
    }

    // =====================================
    // HELPER: GET CURRENT USER
    // =====================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =====================================
    // HELPER: CHECK BOOKING OWNERSHIP
    // =====================================
    private void validateBookingOwnership(Booking booking, User currentUser) {
        if (booking.getTraveler() == null || booking.getTraveler().getId() == null) {
            throw new ResourceNotFoundException("Booking traveler not found");
        }

        if (!booking.getTraveler().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to access this booking");
        }
    }

    // =====================================
    // HELPER: CHECK TRIP OWNERSHIP FOR PLANNER/HOST
    // =====================================
    private void validateTripOwnership(Trip trip, User currentUser) {
        if (trip.getPlanner() == null || trip.getPlanner().getId() == null) {
            throw new ResourceNotFoundException("Trip owner not found");
        }

        if (!trip.getPlanner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to view bookings for this trip");
        }
    }

    // =====================================
    // BOOK TRIP
    // =====================================
    @Override
    @Transactional
    public BookingResponse bookTrip(CreateBookingRequest request) {

        User traveler = getCurrentUser();

        Trip trip = tripRepository.findTripForUpdate(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + request.getTripId()));

        if (Boolean.TRUE.equals(trip.getDeleted()) || !Boolean.TRUE.equals(trip.getActive())) {
            throw new BadRequestException("Trip is not available for booking");
        }

        if (trip.getEndDate() != null && trip.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("This trip has already ended");
        }

        if (request.getSeats() == null || request.getSeats() <= 0) {
            throw new BadRequestException("Seats must be greater than 0");
        }

        if (trip.getTotalSeats() == null || trip.getBookedSeats() == null) {
            throw new BadRequestException("Trip seat configuration is invalid");
        }

        int availableSeats = trip.getTotalSeats() - trip.getBookedSeats();

        if (request.getSeats() > availableSeats) {
            throw new BadRequestException("Not enough seats available. Requested: "
                    + request.getSeats() + ", Available: " + availableSeats);
        }

        if (trip.getPlanner() != null
                && trip.getPlanner().getId() != null
                && trip.getPlanner().getId().equals(traveler.getId())) {
            throw new BadRequestException("You cannot book your own trip");
        }

        if (trip.getBasePrice() == null) {
            throw new BadRequestException("Trip price is not configured");
        }

        // ===== PAYMENT STAGE VALIDATION =====
        // Block bookings less than 7 days before departure
        LocalDate today = LocalDate.now();
        LocalDate departureDate = trip.getStartDate();

        if (departureDate == null) {
            throw new BadRequestException("Trip departure date is not configured");
        }

        if (!paymentStageCalculator.isBookingAllowed(today, departureDate)) {
            throw new BadRequestException(
                "Bookings are closed for this trip. Minimum 7 days before departure required.");
        }

        // Determine schedule type
        ScheduleType scheduleType = paymentStageCalculator.determineScheduleType(today, departureDate);

        trip.setBookedSeats(trip.getBookedSeats() + request.getSeats());
        tripRepository.save(trip);

        BigDecimal totalPrice =
                trip.getBasePrice().multiply(BigDecimal.valueOf(request.getSeats()));

        Booking booking = new Booking();
        booking.setTrip(trip);
        booking.setTraveler(traveler);
        booking.setSeatsBooked(request.getSeats());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setScheduleType(scheduleType);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        Booking savedBooking = bookingRepository.save(booking);

        // ===== GENERATE & PERSIST PAYMENT STAGES =====
        List<PaymentStage> stages = paymentStageCalculator.generateStages(today, departureDate, totalPrice);
        for (PaymentStage stage : stages) {
            stage.setBooking(savedBooking);
        }
        paymentStageRepository.saveAll(stages);

        log.info("Booking created: bookingId={}, tripId={}, travelerId={}, seats={}, scheduleType={}",
                savedBooking.getId(), trip.getId(), traveler.getId(), request.getSeats(), scheduleType);

        // Notify traveler — Event 1: Booking Created
        paymentNotificationService.notifyBookingCreated(
                traveler.getId(),
                savedBooking.getId(),
                trip.getTitle(),
                stages.get(0).getAmount()
        );

        return BookingMapper.toResponse(savedBooking);
    }

    // =====================================
    // TRAVELER BOOKINGS
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {

        User traveler = getCurrentUser();

        List<Booking> bookings = bookingRepository.findByTravelerId(traveler.getId());

        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================
    // BOOKINGS FOR A TRIP (OWNER ONLY)
    // =====================================
    @Override
    public List<BookingResponse> getTripBookings(Long tripId) {

        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new ResourceNotFoundException("Trip not found with id: " + tripId);
        }

        validateTripOwnership(trip, currentUser);

        List<Booking> bookings = bookingRepository.findByTripId(tripId);

        return bookings.stream()
                .map(BookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =====================================
    // CANCEL BOOKING (OWNER ONLY)
    // =====================================
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, CancelBookingRequest request) {

        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateBookingOwnership(booking, currentUser);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed booking cannot be cancelled");
        }

        Trip trip = booking.getTrip();

        if (trip == null) {
            throw new ResourceNotFoundException("Trip not found for this booking");
        }

        if (trip.getBookedSeats() == null) {
            throw new BadRequestException("Trip seat data is invalid");
        }

        if (booking.getSeatsBooked() == null) {
            throw new BadRequestException("Booking seat data is invalid");
        }

        int updatedSeats = trip.getBookedSeats() - booking.getSeatsBooked();
        trip.setBookedSeats(Math.max(updatedSeats, 0));
        tripRepository.save(trip);

        booking.setStatus(BookingStatus.CANCELLED);

        if (booking.getPaymentStatus() == PaymentStatus.FULLY_PAID
                || booking.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            booking.setPaymentStatus(PaymentStatus.PENDING);
        }

        booking.setCancellationReason(request.getReason());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Cancel all unpaid payment stages
        List<PaymentStage> unpaidStages = paymentStageRepository.findByBookingIdAndStatusIn(
                bookingId,
                List.of(
                    com.tourly.payment.enums.PaymentStageStatus.PENDING,
                    com.tourly.payment.enums.PaymentStageStatus.INVOICE_SENT,
                    com.tourly.payment.enums.PaymentStageStatus.OVERDUE
                )
        );
        for (PaymentStage stage : unpaidStages) {
            stage.setStatus(com.tourly.payment.enums.PaymentStageStatus.CANCELLED);
        }
        if (!unpaidStages.isEmpty()) {
            paymentStageRepository.saveAll(unpaidStages);
        }

        log.info("Booking cancelled: bookingId={}, userId={}, stagesCancelled={}",
                bookingId, currentUser.getId(), unpaidStages.size());

        // Initiate refund for previously paid stages per cancellation policy
        if (booking.getAmountPaid() != null && booking.getAmountPaid().compareTo(java.math.BigDecimal.ZERO) > 0) {
            refundService.initiateAutoCancellationRefund(bookingId,
                    request.getReason() != null ? request.getReason() : "Traveler-initiated cancellation");
        }
    }

    // =====================================
    // HOST — All bookings across host's trips
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public List<HostBookingResponse> getMyTripBookings() {
        User currentUser = getCurrentUser();
        List<Booking> bookings = bookingRepository.findAllBookingsByHostId(currentUser.getId());
        return bookings.stream().map(this::toHostBookingResponse).collect(Collectors.toList());
    }

    private HostBookingResponse toHostBookingResponse(Booking b) {
        HostBookingResponse r = new HostBookingResponse();
        r.setBookingId(b.getId());
        r.setBookingRef(b.getBookingRef());
        r.setTripTitle(b.getTrip() != null ? b.getTrip().getTitle() : null);
        if (b.getTraveler() != null) {
            r.setTravelerName(b.getTraveler().getFullName());
            r.setTravelerEmail(b.getTraveler().getEmail());
        }
        r.setSeatsBooked(b.getSeatsBooked());
        r.setTotalPrice(b.getTotalPrice());
        r.setBookingStatus(b.getStatus() != null ? b.getStatus().name() : null);
        r.setPaymentStatus(b.getPaymentStatus() != null ? b.getPaymentStatus().name() : null);
        r.setCreatedAt(b.getCreatedAt());
        r.setConfirmedAt(b.getConfirmedAt());
        return r;
    }

    // =====================================
    // BOOKING DETAIL (TRAVELER)
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingDetail(Long bookingId) {

        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateBookingOwnership(booking, currentUser);

        Trip trip = booking.getTrip();

        BookingDetailResponse response = new BookingDetailResponse();

        // Booking info
        response.setBookingId(booking.getId());
        response.setBookingRef(booking.getBookingRef());
        response.setBookingStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        response.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().name() : null);
        response.setSeatsBooked(booking.getSeatsBooked());
        response.setCancellationReason(booking.getCancellationReason());
        response.setCreatedAt(booking.getCreatedAt());
        response.setConfirmedAt(booking.getConfirmedAt());
        response.setCancelledAt(booking.getCancelledAt());

        // Financial
        response.setBaseAmount(booking.getBaseAmount());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setTaxAmount(booking.getTaxAmount());
        response.setTotalPrice(booking.getTotalPrice());
        response.setAmountPaid(booking.getAmountPaid());
        response.setAmountPending(booking.getAmountPending());

        // Trip info
        if (trip != null) {
            response.setTripId(trip.getId());
            response.setTripTitle(trip.getTitle());
            response.setTripDescription(trip.getDescription());
            response.setTripStartDate(trip.getStartDate());
            response.setTripEndDate(trip.getEndDate());
            response.setDurationDays(trip.getDurationDays());
            response.setDurationNights(trip.getDurationNights());
            response.setStartsFrom(trip.getStartsFrom());
            response.setEndsAt(trip.getEndsAt());
            response.setCancellationPolicy(
                    trip.getCancellationPolicy() != null ? trip.getCancellationPolicy().name() : null);

            // Cover image — first media with isCover=true, or first image
            List<TripMedia> media = trip.getMedia();
            if (media != null && !media.isEmpty()) {
                String coverUrl = media.stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsCover()))
                        .map(TripMedia::getUrl)
                        .findFirst()
                        .orElse(media.get(0).getUrl());
                response.setTripCoverImage(coverUrl);
            }

            // Destination
            Destination dest = trip.getDestination();
            if (dest != null) {
                String destStr = dest.getCity() != null ? dest.getCity() : "";
                if (dest.getState() != null) {
                    destStr = destStr.isEmpty() ? dest.getState() : destStr + ", " + dest.getState();
                }
                response.setDestination(destStr);
            }

            // Host info (prefer host, fallback to planner)
            User host = trip.getHost() != null ? trip.getHost() : trip.getPlanner();
            if (host != null) {
                response.setHostId(host.getId());
                response.setHostName(host.getFullName());
                response.setHostAvatar(host.getAvatar());
            }
        }

        // Payment stages
        List<PaymentStage> stages = paymentStageRepository.findByBookingIdOrderByStageNumberAsc(bookingId);
        List<BookingDetailResponse.PaymentStageInfo> stageInfos = stages.stream().map(stage -> {
            BookingDetailResponse.PaymentStageInfo info = new BookingDetailResponse.PaymentStageInfo();
            info.setStageId(stage.getId());
            info.setStageNumber(stage.getStageNumber());
            info.setLabel(stage.getLabel());
            info.setAmount(stage.getAmount());
            info.setPercentage(stage.getPercentage());
            info.setStatus(stage.getStatus() != null ? stage.getStatus().name() : null);
            info.setDueDate(stage.getDueDate());
            info.setInvoiceOpenDate(stage.getInvoiceOpenDate());
            info.setDeadlineAt(stage.getDeadlineAt());
            info.setInvoiceSentAt(stage.getInvoiceSentAt());
            info.setIsImmediate(stage.getIsImmediate());
            info.setPaidAt(stage.getPaidAt());
            return info;
        }).collect(Collectors.toList());
        response.setPaymentStages(stageInfos);

        return response;
    }
}
