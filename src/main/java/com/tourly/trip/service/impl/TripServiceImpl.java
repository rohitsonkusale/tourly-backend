package com.tourly.trip.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.AccountStatus;
import com.tourly.auth.entity.RoleName;
import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.entity.HostVerification;
import com.tourly.common.exception.BadRequestException;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.trip.dto.request.CreateTripRequest;
import com.tourly.trip.dto.request.UpdateTripRequest;
import com.tourly.trip.dto.response.HostAnalyticsResponse;
import com.tourly.trip.dto.response.HostAnalyticsResponse.TripBookingSummary;
import com.tourly.trip.dto.response.HostStatsResponse;
import com.tourly.trip.dto.response.TripResponse;
import com.tourly.trip.entity.Destination;
import com.tourly.trip.entity.Trip;
import com.tourly.trip.entity.TripBatch;
import com.tourly.trip.entity.TripBadge;
import com.tourly.trip.entity.TripHighlight;
import com.tourly.trip.entity.TripItem;
import com.tourly.trip.entity.TripItineraryDay;
import com.tourly.trip.entity.TripMedia;
import com.tourly.trip.entity.TripPriceBreakdown;
import com.tourly.trip.entity.TripStay;
import com.tourly.trip.entity.TripStayAmenity;
import com.tourly.trip.entity.TripStayImage;
import com.tourly.trip.entity.TripStop;
import com.tourly.trip.enums.ApprovalStatus;
import com.tourly.trip.enums.MediaType;
import com.tourly.trip.enums.TripStatus;
import com.tourly.trip.mapper.TripMapper;
import com.tourly.trip.entity.TripEditLog;
import com.tourly.trip.repository.DestinationRepository;
import com.tourly.trip.repository.TripBatchRepository;
import com.tourly.trip.repository.TripHighlightRepository;
import com.tourly.trip.repository.TripItemRepository;
import com.tourly.trip.repository.TripItineraryDayRepository;
import com.tourly.trip.repository.TripMediaRepository;
import com.tourly.trip.repository.TripPriceBreakdownRepository;
import com.tourly.trip.repository.TripEditLogRepository;
import com.tourly.trip.repository.TripRepository;
import com.tourly.trip.repository.TripStayRepository;
import com.tourly.trip.repository.TripStopRepository;
import com.tourly.trip.service.TripService;
import com.tourly.verification.entity.PlannerVerification;
import com.tourly.verification.enums.VerificationStatus;
import com.tourly.verification.repository.HostVerificationRepository;
import com.tourly.verification.repository.PlannerVerificationRepository;

@Service
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;
    private final UserRepository userRepository;
    private final PlannerVerificationRepository plannerVerificationRepository;
    private final HostVerificationRepository hostVerificationRepository;
    private final BookingRepository bookingRepository;
    private final TripHighlightRepository tripHighlightRepository;
    private final TripItemRepository tripItemRepository;
    private final TripItineraryDayRepository tripItineraryDayRepository;
    private final TripMediaRepository tripMediaRepository;
    private final TripStayRepository tripStayRepository;
    private final TripStopRepository tripStopRepository;
    private final TripPriceBreakdownRepository tripPriceBreakdownRepository;
    private final TripBatchRepository tripBatchRepository;
    private final TripEditLogRepository tripEditLogRepository;

    public TripServiceImpl(
            TripRepository tripRepository,
            DestinationRepository destinationRepository,
            UserRepository userRepository,
            PlannerVerificationRepository plannerVerificationRepository,
            HostVerificationRepository hostVerificationRepository,
            BookingRepository bookingRepository,
            TripHighlightRepository tripHighlightRepository,
            TripItemRepository tripItemRepository,
            TripItineraryDayRepository tripItineraryDayRepository,
            TripMediaRepository tripMediaRepository,
            TripStayRepository tripStayRepository,
            TripStopRepository tripStopRepository,
            TripPriceBreakdownRepository tripPriceBreakdownRepository,
            TripBatchRepository tripBatchRepository,
            TripEditLogRepository tripEditLogRepository) {
        this.tripRepository = tripRepository;
        this.destinationRepository = destinationRepository;
        this.userRepository = userRepository;
        this.plannerVerificationRepository = plannerVerificationRepository;
        this.hostVerificationRepository = hostVerificationRepository;
        this.bookingRepository = bookingRepository;
        this.tripHighlightRepository = tripHighlightRepository;
        this.tripItemRepository = tripItemRepository;
        this.tripItineraryDayRepository = tripItineraryDayRepository;
        this.tripMediaRepository = tripMediaRepository;
        this.tripStayRepository = tripStayRepository;
        this.tripStopRepository = tripStopRepository;
        this.tripPriceBreakdownRepository = tripPriceBreakdownRepository;
        this.tripBatchRepository = tripBatchRepository;
        this.tripEditLogRepository = tripEditLogRepository;
    }

    // ========================================
    // CREATE TRIP
    // Only HOST (verified) or ADMIN
    // NOTE:
    // - HOST can create trip
    // - PLANNER cannot create trip (planner can only plan/suggest)
    // - HOST and PLANNER both require verification in your platform,
    //   but only HOST creation is enforced here
    // ========================================
    @Override
    public TripResponse createTrip(CreateTripRequest request) {
        User currentUser = getCurrentUser();
        RoleName roleName = getRoleName(currentUser);

        if (roleName != RoleName.HOST && roleName != RoleName.ADMIN) {
            throw new BadRequestException("Only verified hosts can create trips.");
        }
        if (roleName != RoleName.ADMIN) {
            validateVerifiedUser(currentUser);
        }

        // ── 1. Find or create destination ─────────────────────
        String city = request.getDestinationCity().trim();
        String state = request.getDestinationState() != null ? request.getDestinationState().trim() : "";

        Destination destination = destinationRepository.findByCityIgnoreCase(city)
                .orElseGet(() -> {
                    Destination d = new Destination();
                    d.setCity(city);
                    d.setState(state.isEmpty() ? null : state);
                    d.setCountry("India");
                    d.setIsActive(true);
                    return destinationRepository.save(d);
                });

        validateTripDates(request.getStartDate(), request.getEndDate());

        // ── 2. Calculate pricing ──────────────────────────────
        BigDecimal basePrice = request.getBasePrice();
        BigDecimal discountPct = request.getMaxDiscountPercent() != null
                ? request.getMaxDiscountPercent() : BigDecimal.ZERO;
        BigDecimal increasePct = request.getMaxIncreasePercent() != null
                ? request.getMaxIncreasePercent() : BigDecimal.ZERO;

        BigDecimal minPrice = basePrice.multiply(
                BigDecimal.ONE.subtract(discountPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
        ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal maxPrice = basePrice.multiply(
                BigDecimal.ONE.add(increasePct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
        ).setScale(2, RoundingMode.HALF_UP);

        validateTripPricing(basePrice, minPrice, maxPrice);

        // ── 3. Save core trip ─────────────────────────────────
        Trip trip = new Trip();
        trip.setTitle(request.getTitle().trim());
        trip.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        // Set host_id for HOST users, planner_id for PLANNER users
        if (roleName == RoleName.HOST || roleName == RoleName.ADMIN) {
            trip.setHost(currentUser);
        } else {
            trip.setPlanner(currentUser);
        }
        trip.setDestination(destination);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setBasePrice(basePrice);
        trip.setMinPrice(minPrice);
        trip.setMaxPrice(maxPrice);
        trip.setCurrentPrice(basePrice);
        trip.setMaxDiscountPercent(discountPct);
        trip.setMaxIncreasePercent(increasePct);
        trip.setTotalSeats(request.getTotalSeats());
        trip.setMinGroupSize(request.getMinGroupSize());
        trip.setBookedSeats(0);
        trip.setCategory(request.getCategory());
        trip.setCancellationPolicy(request.getCancellationPolicy());
        trip.setStatus(TripStatus.DRAFT);
        trip.setApprovalStatus(ApprovalStatus.PENDING);
        trip.setActive(false); // hidden until admin approves
        trip.setDeleted(false);
        trip.setShowPriceBifurcation(request.getShowPriceBifurcation() != null ? request.getShowPriceBifurcation() : true);

        // Rich fields
        trip.setStartsFrom(request.getStartsFrom());
        trip.setEndsAt(request.getEndsAt());
        trip.setTripType(request.getTripType());
        trip.setDifficulty(request.getDifficulty());
        trip.setBestTime(request.getBestTime());
        trip.setDurationDays(request.getDurationDays());
        trip.setDurationNights(request.getDurationNights());
        trip.setAboutDescription(request.getAboutDescription());

        // Parse badges JSON string → List<TripBadge>
        if (request.getBadges() != null && !request.getBadges().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> badgeNames = mapper.readValue(request.getBadges(),
                        mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                java.util.List<TripBadge> badgeEntities = new java.util.ArrayList<>();
                for (String name : badgeNames) {
                    TripBadge badge = new TripBadge();
                    badge.setTrip(trip);
                    badge.setBadgeName(name);
                    badgeEntities.add(badge);
                }
                trip.setBadges(badgeEntities);
            } catch (Exception e) {
                // If parsing fails, ignore badges
            }
        }

        Trip savedTrip = tripRepository.save(trip);

        // ── 4. Save highlights ────────────────────────────────
        if (request.getHighlights() != null) {
            for (int i = 0; i < request.getHighlights().size(); i++) {
                CreateTripRequest.HighlightItem h = request.getHighlights().get(i);
                if (h.getTitle() == null || h.getTitle().trim().isEmpty()) continue;
                TripHighlight highlight = new TripHighlight();
                highlight.setTrip(savedTrip);
                highlight.setIcon(h.getIcon() != null ? h.getIcon() : "star");
                highlight.setTitle(h.getTitle().trim());
                highlight.setSortOrder(i);
                tripHighlightRepository.save(highlight);
            }
        }

        // ── 5. Save gallery media (Cloudinary URLs) ───────────
        if (request.getGalleryUrls() != null) {
            for (int i = 0; i < request.getGalleryUrls().size(); i++) {
                String url = request.getGalleryUrls().get(i);
                if (url == null || url.trim().isEmpty()) continue;
                TripMedia media = new TripMedia();
                media.setTrip(savedTrip);
                media.setUrl(url.trim());
                media.setMediaType(MediaType.IMAGE);
                media.setIsCover(false);
                media.setSortOrder(i);
                tripMediaRepository.save(media);
            }
        }
        if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().trim().isEmpty()) {
            TripMedia cover = new TripMedia();
            cover.setTrip(savedTrip);
            cover.setUrl(request.getCoverImageUrl().trim());
            cover.setMediaType(MediaType.IMAGE);
            cover.setIsCover(true);
            cover.setSortOrder(0);
            tripMediaRepository.save(cover);
        }

        // ── 6. Save itinerary ─────────────────────────────────
        if (request.getItinerary() != null) {
            for (CreateTripRequest.ItineraryDayItem day : request.getItinerary()) {
                if (day.getTitle() == null || day.getTitle().trim().isEmpty()) continue;
                TripItineraryDay itDay = new TripItineraryDay();
                itDay.setTrip(savedTrip);
                itDay.setDayNumber(day.getDay() != null ? day.getDay() : 1);
                itDay.setTitle(day.getTitle().trim());
                itDay.setDescription(day.getDescription());
                itDay.setStay(day.getStay());
                itDay.setMeals(day.getMeals());
                itDay.setSortOrder(day.getDay() != null ? day.getDay() : 0);
                tripItineraryDayRepository.save(itDay);
            }
        }

        // ── 7. Save inclusions ────────────────────────────────
        if (request.getInclusions() != null) {
            for (int i = 0; i < request.getInclusions().size(); i++) {
                String desc = request.getInclusions().get(i);
                if (desc == null || desc.trim().isEmpty()) continue;
                TripItem item = new TripItem();
                item.setTrip(savedTrip);
                item.setType("INCLUSION");
                item.setDescription(desc.trim());
                item.setSortOrder(i);
                tripItemRepository.save(item);
            }
        }

        // ── 8. Save exclusions ────────────────────────────────
        if (request.getExclusions() != null) {
            for (int i = 0; i < request.getExclusions().size(); i++) {
                String desc = request.getExclusions().get(i);
                if (desc == null || desc.trim().isEmpty()) continue;
                TripItem item = new TripItem();
                item.setTrip(savedTrip);
                item.setType("EXCLUSION");
                item.setDescription(desc.trim());
                item.setSortOrder(i);
                tripItemRepository.save(item);
            }
        }

        // ── 9. Save stops ─────────────────────────────────────
        if (request.getStops() != null) {
            for (int i = 0; i < request.getStops().size(); i++) {
                String stopName = request.getStops().get(i);
                if (stopName == null || stopName.trim().isEmpty()) continue;
                TripStop stop = new TripStop();
                stop.setTrip(savedTrip);
                stop.setStopName(stopName.trim());
                stop.setSortOrder(i);
                tripStopRepository.save(stop);
            }
        }

        // ── 10. Save stays ────────────────────────────────────
        if (request.getStays() != null) {
            for (int i = 0; i < request.getStays().size(); i++) {
                CreateTripRequest.StayItem s = request.getStays().get(i);
                if (s.getName() == null || s.getName().trim().isEmpty()) continue;
                TripStay stay = new TripStay();
                stay.setTrip(savedTrip);
                stay.setName(s.getName().trim());
                stay.setLocation(s.getLocation());
                stay.setDescription(s.getDescription());
                stay.setSortOrder(i);
                TripStay savedStay = tripStayRepository.save(stay);

                // Amenities
                if (s.getAmenities() != null) {
                    for (String amenity : s.getAmenities()) {
                        if (amenity == null || amenity.trim().isEmpty()) continue;
                        TripStayAmenity a = new TripStayAmenity();
                        a.setStay(savedStay);
                        a.setAmenity(amenity.trim());
                        savedStay.getAmenities().add(a);
                    }
                }

                // Stay images (Cloudinary URLs)
                if (s.getImages() != null) {
                    for (int j = 0; j < s.getImages().size(); j++) {
                        String imgUrl = s.getImages().get(j);
                        if (imgUrl == null || imgUrl.trim().isEmpty()) continue;
                        TripStayImage img = new TripStayImage();
                        img.setStay(savedStay);
                        img.setImageUrl(imgUrl.trim());
                        img.setSortOrder(j);
                        savedStay.getImages().add(img);
                    }
                }
                tripStayRepository.save(savedStay);
            }
        }

        // ── 11. Save price breakdown ──────────────────────────
        if (request.getPriceBreakdown() != null) {
            for (int i = 0; i < request.getPriceBreakdown().size(); i++) {
                CreateTripRequest.PriceBreakdownItem pb = request.getPriceBreakdown().get(i);
                if (pb.getCategory() == null || pb.getAmount() == null) continue;
                TripPriceBreakdown breakdown = new TripPriceBreakdown();
                breakdown.setTrip(savedTrip);
                breakdown.setCategory(pb.getCategory().trim());
                breakdown.setAmount(pb.getAmount());
                breakdown.setDescription(pb.getDescription());
                breakdown.setSortOrder(i);
                tripPriceBreakdownRepository.save(breakdown);
            }
        }

        // ── 12. Save batches ──────────────────────────────────
        if (request.getBatches() != null) {
            for (CreateTripRequest.BatchItem b : request.getBatches()) {
                if (b.getStartDate() == null || b.getEndDate() == null) continue;
                TripBatch batch = new TripBatch();
                batch.setTrip(savedTrip);
                batch.setStartDate(b.getStartDate());
                batch.setEndDate(b.getEndDate());
                batch.setPrice(b.getPrice() != null ? b.getPrice() : basePrice);
                batch.setSeatsAvailable(request.getTotalSeats());
                batch.setStatus("OPEN");
                tripBatchRepository.save(batch);
            }
        }

        return TripMapper.mapToResponse(savedTrip);
    }

    // ========================================
    // GET ALL TRIPS (PUBLIC)
    // Only published + active + not deleted
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getAllTrips(Pageable pageable) {
        return tripRepository
                .findByDeletedFalseAndActiveTrueAndStatus(TripStatus.PUBLISHED, pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // GET TRIP BY ID
    // Public can only see published + active + not deleted
    // Owner/Admin can see own trip even if draft/inactive
    // Deleted trips remain hidden from public
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        // Deleted trips are hidden from public
        if (Boolean.TRUE.equals(trip.getDeleted())) {
            User currentUser = getCurrentUserOptional();

            if (currentUser == null) {
                throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
            }

            boolean isAdmin = getRoleName(currentUser) == RoleName.ADMIN;
            boolean isOwner = trip.getPlanner() != null && trip.getPlanner().getId().equals(currentUser.getId());

            if (!isAdmin && !isOwner) {
                throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
            }

            return TripMapper.mapToResponse(trip);
        }

        boolean isPublicVisible = Boolean.TRUE.equals(trip.getActive())
                && TripStatus.PUBLISHED.equals(trip.getStatus());

        if (isPublicVisible) {
            return TripMapper.mapToResponse(trip);
        }

        // If not public visible, allow only owner/admin
        User currentUser = getCurrentUserOptional();
        if (currentUser == null) {
            throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
        }

        boolean isAdmin = getRoleName(currentUser) == RoleName.ADMIN;
        boolean isOwner = trip.getPlanner() != null && trip.getPlanner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ResourceNotFoundException("Trip not found with ID: " + tripId);
        }

        return TripMapper.mapToResponse(trip);
    }

    // ========================================
    // SEARCH TRIPS (PUBLIC)
    // Only published + active + not deleted
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> searchTrips(
            String destination,
            String host,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }

        return tripRepository
                .searchPublishedTrips(destination, host, startDate, endDate, TripStatus.PUBLISHED, pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - ALL
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByHostIdOrPlannerId(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - ACTIVE
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyActiveTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByHostIdOrPlannerIdAndActiveTrueAndDeletedFalse(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - INACTIVE
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyInactiveTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByHostIdOrPlannerIdAndActiveFalseAndDeletedFalse(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - DELETED
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyDeletedTrips(Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByHostIdOrPlannerIdAndDeletedTrue(currentUser.getId(), pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // MY TRIPS - BY STATUS
    // ========================================
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getMyTripsByStatus(TripStatus status, Pageable pageable) {
        User currentUser = getCurrentUser();
        return tripRepository.findByHostIdOrPlannerIdAndStatusAndDeletedFalse(currentUser.getId(), status, pageable)
                .map(TripMapper::mapToResponse);
    }

    // ========================================
    // UPDATE TRIP
    // Owner or Admin
    // ========================================
    @Override
    public TripResponse updateTrip(Long tripId, UpdateTripRequest request) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        validateTripAccess(trip, currentUser);

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new BadRequestException("Deleted trip cannot be updated.");
        }

        boolean isLimited = isLimitedEditMode(trip);

        System.out.println("[TRIP UPDATE] tripId=" + tripId + " isLimited=" + isLimited + " bookedSeats=" + trip.getBookedSeats() + " approvalStatus=" + trip.getApprovalStatus());
        System.out.println("[TRIP UPDATE] request.title=" + request.getTitle());
        System.out.println("[TRIP UPDATE] request.aboutDescription=" + (request.getAboutDescription() != null ? request.getAboutDescription().substring(0, Math.min(50, request.getAboutDescription().length())) : "null"));
        // ── Capture old values BEFORE applying changes ────────
        String adminMsgContext = trip.getRejectionReason(); // admin message before edit
        java.util.List<TripEditLog> editLogs = new java.util.ArrayList<>();
        String sessionId = java.util.UUID.randomUUID().toString();
        int editNumber = tripEditLogRepository.countEditSessions(tripId) + 1;

        // Snapshot scalar fields for diff comparison
        java.util.Map<String, String> oldValues = captureCurrentValues(trip, request, isLimited);

        // Always apply operational fields
        applyOperationalFields(trip, request);

        // Apply structural fields only in full mode
        if (!isLimited) {
            applyStructuralFields(trip, request);
        } else {
            System.out.println("[TRIP UPDATE] Limited mode - structural fields SKIPPED for trip " + tripId);
        }

        // ── Capture new values AFTER applying changes ─────────
        java.util.Map<String, String> newValues = captureCurrentValues(trip, request, isLimited);

        // ── Build edit log entries for changed fields ──────────
        for (java.util.Map.Entry<String, String> entry : oldValues.entrySet()) {
            String fieldName = entry.getKey();
            String oldVal = entry.getValue();
            String newVal = newValues.getOrDefault(fieldName, "");
            if (!java.util.Objects.equals(oldVal, newVal)) {
                TripEditLog log = new TripEditLog();
                log.setTrip(trip);
                log.setEditedBy(currentUser);
                log.setEditSessionId(sessionId);
                log.setFieldName(fieldName);
                log.setOldValue(oldVal);
                log.setNewValue(newVal);
                log.setAdminMessageContext(adminMsgContext);
                log.setEditNumber(editNumber);
                editLogs.add(log);
            }
        }

        // Compute and set approval status transition
        trip.setApprovalStatus(computeApprovalTransition(trip, isLimited));

        // If going back to PENDING from a full edit, reset to DRAFT + inactive
        if (!isLimited && trip.getApprovalStatus() == ApprovalStatus.PENDING) {
            trip.setStatus(TripStatus.DRAFT);
            trip.setActive(false);
            // Keep rejection reason so admin can see history context
            // trip.setRejectionReason(null); -- intentionally NOT clearing now
        }

        Trip updatedTrip = tripRepository.save(trip);

        // Save edit logs
        if (!editLogs.isEmpty()) {
            tripEditLogRepository.saveAll(editLogs);
        }

        return TripMapper.mapToResponse(updatedTrip);
    }

    /**
     * Captures current trip field values as strings for diff comparison.
     * Only captures fields that the request is attempting to change.
     */
    private java.util.Map<String, String> captureCurrentValues(Trip trip, UpdateTripRequest request, boolean isLimited) {
        java.util.Map<String, String> values = new java.util.LinkedHashMap<>();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        // Operational fields (always captured)
        if (request.getStartDate() != null) values.put("startDate", str(trip.getStartDate()));
        if (request.getEndDate() != null) values.put("endDate", str(trip.getEndDate()));
        if (request.getBasePrice() != null) values.put("basePrice", str(trip.getBasePrice()));
        if (request.getTotalSeats() != null) values.put("totalSeats", str(trip.getTotalSeats()));
        if (request.getMaxDiscountPercent() != null) values.put("maxDiscountPercent", str(trip.getMaxDiscountPercent()));
        if (request.getMaxIncreasePercent() != null) values.put("maxIncreasePercent", str(trip.getMaxIncreasePercent()));

        // Structural fields (only in full mode)
        if (!isLimited) {
            if (request.getTitle() != null) values.put("title", str(trip.getTitle()));
            if (request.getDescription() != null) values.put("description", str(trip.getDescription()));
            if (request.getAboutDescription() != null) values.put("aboutDescription", str(trip.getAboutDescription()));
            if (request.getDestinationCity() != null) values.put("destinationCity", trip.getDestination() != null ? str(trip.getDestination().getCity()) : "");
            if (request.getDestinationState() != null) values.put("destinationState", trip.getDestination() != null ? str(trip.getDestination().getState()) : "");
            if (request.getStartsFrom() != null) values.put("startsFrom", str(trip.getStartsFrom()));
            if (request.getEndsAt() != null) values.put("endsAt", str(trip.getEndsAt()));
            if (request.getDurationDays() != null) values.put("durationDays", str(trip.getDurationDays()));
            if (request.getDurationNights() != null) values.put("durationNights", str(trip.getDurationNights()));
            if (request.getMinGroupSize() != null) values.put("minGroupSize", str(trip.getMinGroupSize()));
            if (request.getDifficulty() != null) values.put("difficulty", str(trip.getDifficulty()));
            if (request.getTripType() != null) values.put("tripType", str(trip.getTripType()));
            if (request.getBestTime() != null) values.put("bestTime", str(trip.getBestTime()));
            if (request.getStops() != null) {
                values.put("stops", toJson(mapper, trip.getStops().stream().map(TripStop::getStopName).collect(Collectors.toList())));
            }
            if (request.getInclusions() != null) {
                values.put("inclusions", toJson(mapper, trip.getItems().stream().filter(i -> "INCLUSION".equalsIgnoreCase(i.getType())).map(TripItem::getDescription).collect(Collectors.toList())));
            }
            if (request.getExclusions() != null) {
                values.put("exclusions", toJson(mapper, trip.getItems().stream().filter(i -> "EXCLUSION".equalsIgnoreCase(i.getType())).map(TripItem::getDescription).collect(Collectors.toList())));
            }
            if (request.getCoverImageUrl() != null) values.put("coverImageUrl", str(trip.getMedia().stream().filter(m -> Boolean.TRUE.equals(m.getIsCover())).map(TripMedia::getUrl).findFirst().orElse("")));
            if (request.getGalleryUrls() != null) {
                values.put("galleryUrls", toJson(mapper, trip.getMedia().stream().filter(m -> !Boolean.TRUE.equals(m.getIsCover())).map(TripMedia::getUrl).collect(Collectors.toList())));
            }
        }

        return values;
    }

    private String str(Object val) {
        return val != null ? val.toString() : "";
    }

    private String toJson(com.fasterxml.jackson.databind.ObjectMapper mapper, Object val) {
        try { return mapper.writeValueAsString(val); } catch (Exception e) { return "[]"; }
    }

    // ── Edit Mode Helpers ─────────────────────────────────────

    private boolean isLimitedEditMode(Trip trip) {
        // Limited mode = trip has at least 1 booking (regardless of approval status)
        return trip.getBookedSeats() != null && trip.getBookedSeats() > 0;
    }

    private ApprovalStatus computeApprovalTransition(Trip trip, boolean isLimitedMode) {
        if (isLimitedMode) {
            return ApprovalStatus.APPROVED; // limited edits don't require re-approval
        }
        // Full mode: PENDING stays PENDING, everything else goes to PENDING for re-review
        return ApprovalStatus.PENDING;
    }

    private void applyOperationalFields(Trip trip, UpdateTripRequest request) {
        if (request.getStartDate() != null || request.getEndDate() != null) {
            LocalDate newStart = request.getStartDate() != null ? request.getStartDate() : trip.getStartDate();
            LocalDate newEnd = request.getEndDate() != null ? request.getEndDate() : trip.getEndDate();
            validateTripDates(newStart, newEnd);
            trip.setStartDate(newStart);
            trip.setEndDate(newEnd);
        }

        if (request.getBasePrice() != null) {
            trip.setBasePrice(request.getBasePrice());
            // Recalculate min/max based on discount/increase percentages
            BigDecimal discountPct = request.getMaxDiscountPercent() != null ? request.getMaxDiscountPercent() : (trip.getMaxDiscountPercent() != null ? trip.getMaxDiscountPercent() : BigDecimal.ZERO);
            BigDecimal increasePct = request.getMaxIncreasePercent() != null ? request.getMaxIncreasePercent() : (trip.getMaxIncreasePercent() != null ? trip.getMaxIncreasePercent() : BigDecimal.ZERO);
            trip.setMinPrice(request.getBasePrice().multiply(BigDecimal.ONE.subtract(discountPct.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP))).setScale(2, java.math.RoundingMode.HALF_UP));
            trip.setMaxPrice(request.getBasePrice().multiply(BigDecimal.ONE.add(increasePct.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP))).setScale(2, java.math.RoundingMode.HALF_UP));
            trip.setCurrentPrice(request.getBasePrice());
        }
        if (request.getMinPrice() != null) trip.setMinPrice(request.getMinPrice());
        if (request.getMaxPrice() != null) trip.setMaxPrice(request.getMaxPrice());
        if (request.getMaxDiscountPercent() != null) trip.setMaxDiscountPercent(request.getMaxDiscountPercent());
        if (request.getMaxIncreasePercent() != null) trip.setMaxIncreasePercent(request.getMaxIncreasePercent());
        if (request.getShowPriceBifurcation() != null) trip.setShowPriceBifurcation(request.getShowPriceBifurcation());

        if (request.getTotalSeats() != null) {
            if (trip.getBookedSeats() != null && request.getTotalSeats() < trip.getBookedSeats()) {
                throw new BadRequestException("Total seats cannot be less than already booked seats.");
            }
            trip.setTotalSeats(request.getTotalSeats());
        }

        // Batches are operational
        if (request.getBatches() != null) {
            trip.getBatches().clear();
            for (CreateTripRequest.BatchItem b : request.getBatches()) {
                if (b.getStartDate() == null || b.getEndDate() == null) continue;
                TripBatch batch = new TripBatch();
                batch.setTrip(trip);
                batch.setStartDate(b.getStartDate());
                batch.setEndDate(b.getEndDate());
                batch.setPrice(b.getPrice() != null ? b.getPrice() : trip.getBasePrice());
                batch.setSeatsAvailable(trip.getTotalSeats());
                batch.setStatus("OPEN");
                trip.getBatches().add(batch);
            }
        }
    }

    private void applyStructuralFields(Trip trip, UpdateTripRequest request) {
        if (request.getTitle() != null) trip.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) trip.setDescription(request.getDescription().trim());

        // Destination by city/state
        if (request.getDestinationCity() != null && !request.getDestinationCity().trim().isEmpty()) {
            String city = request.getDestinationCity().trim();
            String state = request.getDestinationState() != null ? request.getDestinationState().trim() : "";
            Destination destination = destinationRepository.findByCityIgnoreCase(city)
                    .orElseGet(() -> {
                        Destination d = new Destination();
                        d.setCity(city);
                        d.setState(state.isEmpty() ? null : state);
                        d.setCountry("India");
                        d.setIsActive(true);
                        return destinationRepository.save(d);
                    });
            trip.setDestination(destination);
        } else if (request.getDestinationId() != null) {
            Destination destination = destinationRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination not found with ID: " + request.getDestinationId()));
            trip.setDestination(destination);
        }

        if (request.getAboutDescription() != null) trip.setAboutDescription(request.getAboutDescription().trim());
        if (request.getDifficulty() != null) trip.setDifficulty(request.getDifficulty());
        if (request.getTripType() != null) trip.setTripType(request.getTripType());
        if (request.getStartsFrom() != null) trip.setStartsFrom(request.getStartsFrom());
        if (request.getEndsAt() != null) trip.setEndsAt(request.getEndsAt());
        if (request.getDurationDays() != null) trip.setDurationDays(request.getDurationDays());
        if (request.getDurationNights() != null) trip.setDurationNights(request.getDurationNights());
        if (request.getMinGroupSize() != null) trip.setMinGroupSize(request.getMinGroupSize());
        if (request.getBestTime() != null) trip.setBestTime(request.getBestTime());
        if (request.getCategory() != null) trip.setCategory(request.getCategory());
        if (request.getCancellationPolicy() != null) trip.setCancellationPolicy(request.getCancellationPolicy());

        // Badges
        if (request.getBadges() != null && !request.getBadges().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<String> badgeNames = mapper.readValue(request.getBadges(),
                        mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                // Remove badges not in new list, add missing ones
                trip.getBadges().removeIf(b -> !badgeNames.contains(b.getBadgeName()));
                java.util.Set<String> existing = trip.getBadges().stream()
                        .map(TripBadge::getBadgeName).collect(java.util.stream.Collectors.toSet());
                for (String name : badgeNames) {
                    if (!existing.contains(name)) {
                        TripBadge badge = new TripBadge();
                        badge.setTrip(trip);
                        badge.setBadgeName(name);
                        trip.getBadges().add(badge);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Cover image
        if (request.getCoverImageUrl() != null) {
            // Remove old cover, add new
            trip.getMedia().removeIf(m -> m.getIsCover() != null && m.getIsCover());
            if (!request.getCoverImageUrl().trim().isEmpty()) {
                TripMedia cover = new TripMedia();
                cover.setTrip(trip);
                cover.setUrl(request.getCoverImageUrl().trim());
                cover.setMediaType(com.tourly.trip.enums.MediaType.IMAGE);
                cover.setIsCover(true);
                cover.setSortOrder(0);
                trip.getMedia().add(cover);
            }
        }

        // Gallery
        if (request.getGalleryUrls() != null) {
            trip.getMedia().removeIf(m -> m.getIsCover() == null || !m.getIsCover());
            for (int i = 0; i < request.getGalleryUrls().size(); i++) {
                String url = request.getGalleryUrls().get(i);
                if (url == null || url.trim().isEmpty()) continue;
                TripMedia media = new TripMedia();
                media.setTrip(trip);
                media.setUrl(url.trim());
                media.setMediaType(com.tourly.trip.enums.MediaType.IMAGE);
                media.setIsCover(false);
                media.setSortOrder(i);
                trip.getMedia().add(media);
            }
        }

        // Highlights
        if (request.getHighlights() != null) {
            trip.getHighlights().clear();
            for (int i = 0; i < request.getHighlights().size(); i++) {
                CreateTripRequest.HighlightItem h = request.getHighlights().get(i);
                if (h.getTitle() == null || h.getTitle().trim().isEmpty()) continue;
                TripHighlight highlight = new TripHighlight();
                highlight.setTrip(trip);
                highlight.setIcon(h.getIcon() != null ? h.getIcon() : "star");
                highlight.setTitle(h.getTitle().trim());
                highlight.setSortOrder(i);
                trip.getHighlights().add(highlight);
            }
        }

        // Itinerary
        if (request.getItinerary() != null) {
            trip.getItinerary().clear();
            for (CreateTripRequest.ItineraryDayItem day : request.getItinerary()) {
                if (day.getTitle() == null || day.getTitle().trim().isEmpty()) continue;
                TripItineraryDay itDay = new TripItineraryDay();
                itDay.setTrip(trip);
                itDay.setDayNumber(day.getDay() != null ? day.getDay() : 1);
                itDay.setTitle(day.getTitle().trim());
                itDay.setDescription(day.getDescription());
                itDay.setStay(day.getStay());
                itDay.setMeals(day.getMeals());
                itDay.setSortOrder(day.getDay() != null ? day.getDay() : 0);
                trip.getItinerary().add(itDay);
            }
        }

        // Inclusions
        if (request.getInclusions() != null) {
            trip.getItems().removeIf(i -> "INCLUSION".equalsIgnoreCase(i.getType()));
            for (int i = 0; i < request.getInclusions().size(); i++) {
                String desc = request.getInclusions().get(i);
                if (desc == null || desc.trim().isEmpty()) continue;
                TripItem item = new TripItem();
                item.setTrip(trip);
                item.setType("INCLUSION");
                item.setDescription(desc.trim());
                item.setSortOrder(i);
                trip.getItems().add(item);
            }
        }

        // Exclusions
        if (request.getExclusions() != null) {
            trip.getItems().removeIf(i -> "EXCLUSION".equalsIgnoreCase(i.getType()));
            for (int i = 0; i < request.getExclusions().size(); i++) {
                String desc = request.getExclusions().get(i);
                if (desc == null || desc.trim().isEmpty()) continue;
                TripItem item = new TripItem();
                item.setTrip(trip);
                item.setType("EXCLUSION");
                item.setDescription(desc.trim());
                item.setSortOrder(i);
                trip.getItems().add(item);
            }
        }

        // Stops
        if (request.getStops() != null) {
            trip.getStops().clear();
            for (int i = 0; i < request.getStops().size(); i++) {
                String stopName = request.getStops().get(i);
                if (stopName == null || stopName.trim().isEmpty()) continue;
                TripStop stop = new TripStop();
                stop.setTrip(trip);
                stop.setStopName(stopName.trim());
                stop.setSortOrder(i);
                trip.getStops().add(stop);
            }
        }

        // Stays
        if (request.getStays() != null) {
            trip.getStays().clear();
            for (int i = 0; i < request.getStays().size(); i++) {
                CreateTripRequest.StayItem s = request.getStays().get(i);
                if (s.getName() == null || s.getName().trim().isEmpty()) continue;
                TripStay stay = new TripStay();
                stay.setTrip(trip);
                stay.setName(s.getName().trim());
                stay.setLocation(s.getLocation());
                stay.setDescription(s.getDescription());
                stay.setSortOrder(i);
                if (s.getAmenities() != null) {
                    for (String amenity : s.getAmenities()) {
                        if (amenity == null || amenity.trim().isEmpty()) continue;
                        TripStayAmenity a = new TripStayAmenity();
                        a.setStay(stay);
                        a.setAmenity(amenity.trim());
                        stay.getAmenities().add(a);
                    }
                }
                if (s.getImages() != null) {
                    for (int j = 0; j < s.getImages().size(); j++) {
                        String imgUrl = s.getImages().get(j);
                        if (imgUrl == null || imgUrl.trim().isEmpty()) continue;
                        TripStayImage img = new TripStayImage();
                        img.setStay(stay);
                        img.setImageUrl(imgUrl.trim());
                        img.setSortOrder(j);
                        stay.getImages().add(img);
                    }
                }
                trip.getStays().add(stay);
            }
        }

        // Price Breakdown
        if (request.getPriceBreakdown() != null) {
            trip.getPriceBreakdown().clear();
            for (int i = 0; i < request.getPriceBreakdown().size(); i++) {
                CreateTripRequest.PriceBreakdownItem pb = request.getPriceBreakdown().get(i);
                if (pb.getCategory() == null || pb.getAmount() == null) continue;
                TripPriceBreakdown breakdown = new TripPriceBreakdown();
                breakdown.setTrip(trip);
                breakdown.setCategory(pb.getCategory().trim());
                breakdown.setAmount(pb.getAmount());
                breakdown.setDescription(pb.getDescription());
                breakdown.setSortOrder(i);
                trip.getPriceBreakdown().add(breakdown);
            }
        }
    }

    // ========================================
    // DELETE TRIP (SOFT DELETE)
    // Owner or Admin
    // ========================================
    @Override
    public void deleteTrip(Long tripId) {
        User currentUser = getCurrentUser();

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        validateTripAccess(trip, currentUser);

        if (Boolean.TRUE.equals(trip.getDeleted())) {
            throw new BadRequestException("Trip is already deleted.");
        }

        trip.setActive(false);
        trip.setDeleted(true);
        trip.setDeletedAt(LocalDateTime.now());

        tripRepository.save(trip);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Authenticated user not found.");
        }

        String email = authentication.getName();

        if ("anonymousUser".equalsIgnoreCase(email)) {
            throw new BadRequestException("Authenticated user not found.");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private User getCurrentUserOptional() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                return null;
            }

            String email = authentication.getName();

            if ("anonymousUser".equalsIgnoreCase(email)) {
                return null;
            }

            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // IMPORTANT:
    // User -> Role -> RoleName
    private RoleName getRoleName(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            throw new BadRequestException("User role is not assigned properly.");
        }
        return user.getRole().getName();
    }

    // Verification required for HOST / PLANNER on your platform
    private void validateVerifiedUser(User user) {
        RoleName roleName = getRoleName(user);

        if (roleName != RoleName.HOST && roleName != RoleName.PLANNER) {
            throw new BadRequestException("Verification is only applicable for host or planner accounts.");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Your account is not active. You cannot create trips.");
        }

        if (Boolean.FALSE.equals(user.getKycVerified())) {
            throw new BadRequestException("Your KYC is not verified. You cannot create trips.");
        }

        if (roleName == RoleName.HOST) {
            // HOSTs are verified via HostVerification (ApprovalStatus enum)
            HostVerification hostVerification = hostVerificationRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException(
                            "Host verification profile not found. Please complete verification first."));

            if (hostVerification.getVerificationStatus() != VerificationStatus.APPROVED) {
                throw new BadRequestException("Your host verification is not approved yet. You cannot create trips.");
            }
        } else {
            // PLANNERs are verified via PlannerVerification (VerificationStatus enum)
            PlannerVerification plannerVerification = plannerVerificationRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException(
                            "Planner verification profile not found. Please complete verification first."));

            if (plannerVerification.getVerificationStatus() != VerificationStatus.APPROVED) {
                throw new BadRequestException("Your planner verification is not approved yet. You cannot create trips.");
            }
        }
    }

    private void validateTripDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }
    }

    private void validateTripPricing(
            java.math.BigDecimal basePrice,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice) {

        if (basePrice == null || minPrice == null || maxPrice == null) {
            throw new BadRequestException("Base price, minimum price, and maximum price are required.");
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price.");
        }

        if (basePrice.compareTo(minPrice) < 0 || basePrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Base price must be between minimum price and maximum price.");
        }
    }

    private void validateTripAccess(Trip trip, User currentUser) {
        boolean isAdmin = getRoleName(currentUser) == RoleName.ADMIN;
        boolean isOwnerByPlanner = trip.getPlanner() != null && trip.getPlanner().getId().equals(currentUser.getId());
        boolean isOwnerByHost = trip.getHost() != null && trip.getHost().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwnerByPlanner && !isOwnerByHost) {
            throw new BadRequestException("You are not allowed to manage this trip.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HostStatsResponse getHostStats() {
        User currentUser = getCurrentUser();

        // 1. Upcoming Trips Count (active, non-deleted, starts in future)
        long upcomingTrips = tripRepository.countByHostIdOrPlannerIdAndActiveTrueAndDeletedFalseAndStartDateAfter(
                currentUser.getId(),
                LocalDate.now()
        );

        // 2. Total Bookings Count (confirmed / completed bookings on host's trips)
        long totalBookings = bookingRepository.countBookingsByHostId(currentUser.getId());

        // 3. Monthly Earnings (total earnings from host's trips)
        java.math.BigDecimal earnings = bookingRepository.sumEarningsByHostId(currentUser.getId());

        // 4. Pending Actions Count (trips that are DRAFT / pending review)
        long pendingTrips = tripRepository.countByHostIdOrPlannerIdAndActiveTrueAndDeletedFalseAndStatus(
                currentUser.getId(),
                TripStatus.DRAFT
        );

        return new HostStatsResponse(upcomingTrips, totalBookings, earnings, pendingTrips);
    }

    @Override
    @Transactional(readOnly = true)
    public HostAnalyticsResponse getHostAnalytics() {
        User currentUser = getCurrentUser();
        Long hostId = currentUser.getId();

        // All trips by this host
        List<Trip> allTrips = tripRepository.findByHostIdOrPlannerId(hostId,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        long totalTrips = allTrips.size();
        long publishedTrips = allTrips.stream()
                .filter(t -> TripStatus.PUBLISHED.equals(t.getStatus())).count();
        long draftTrips = allTrips.stream()
                .filter(t -> TripStatus.DRAFT.equals(t.getStatus())).count();

        long totalSeatsOffered = allTrips.stream()
                .mapToLong(t -> t.getTotalSeats() != null ? t.getTotalSeats() : 0).sum();
        long totalSeatsBooked = allTrips.stream()
                .mapToLong(t -> t.getBookedSeats() != null ? t.getBookedSeats() : 0).sum();

        double occupancyRate = totalSeatsOffered > 0
                ? Math.round((totalSeatsBooked * 100.0 / totalSeatsOffered) * 10.0) / 10.0
                : 0.0;

        long totalBookings = bookingRepository.countBookingsByHostId(hostId);
        java.math.BigDecimal totalRevenue = bookingRepository.sumEarningsByHostId(hostId);

        java.math.BigDecimal avgRevenue = (totalTrips > 0 && totalRevenue != null)
                ? totalRevenue.divide(java.math.BigDecimal.valueOf(totalTrips), 2,
                        java.math.RoundingMode.HALF_UP)
                : java.math.BigDecimal.ZERO;

        // Top trips
        List<Object[]> topRaw = bookingRepository.findTopTripsByHostId(hostId);
        List<TripBookingSummary> topTrips = topRaw.stream()
                .limit(5)
                .map(row -> new TripBookingSummary(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        (java.math.BigDecimal) row[4]))
                .collect(Collectors.toList());

        HostAnalyticsResponse response = new HostAnalyticsResponse();
        response.setTotalTrips(totalTrips);
        response.setPublishedTrips(publishedTrips);
        response.setDraftTrips(draftTrips);
        response.setTotalBookings(totalBookings);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : java.math.BigDecimal.ZERO);
        response.setAvgRevenuePerTrip(avgRevenue);
        response.setTotalSeatsOffered(totalSeatsOffered);
        response.setTotalSeatsBooked(totalSeatsBooked);
        response.setOccupancyRate(occupancyRate);
        response.setTopTrips(topTrips);
        return response;
    }
}