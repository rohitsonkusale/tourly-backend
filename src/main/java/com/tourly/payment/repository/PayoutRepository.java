package com.tourly.payment.repository;

import com.tourly.payment.entity.Payout;
import com.tourly.payment.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Page<Payout> findByPayeeIdOrderByRequestedAtDesc(Long payeeId, Pageable pageable);

    Page<Payout> findAllByOrderByRequestedAtDesc(Pageable pageable);

    Page<Payout> findByStatusOrderByRequestedAtDesc(PayoutStatus status, Pageable pageable);

    List<Payout> findByPayeeIdAndStatusIn(Long payeeId, List<PayoutStatus> statuses);

    long countByStatus(PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payout p WHERE p.payee.id = :payeeId AND p.status = :status")
    BigDecimal sumNetAmountByPayeeIdAndStatus(@Param("payeeId") Long payeeId, @Param("status") PayoutStatus status);

    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payout p WHERE p.payee.id = :payeeId AND p.status IN :statuses")
    BigDecimal sumNetAmountByPayeeIdAndStatusIn(@Param("payeeId") Long payeeId, @Param("statuses") List<PayoutStatus> statuses);

    // Count all payouts for a host (regardless of status)
    long countByPayeeId(Long payeeId);

    // Count released payouts for a host
    long countByPayeeIdAndStatus(Long payeeId, PayoutStatus status);

    // Find the most recent released payout for a host (excluding current)
    @Query("SELECT p FROM Payout p WHERE p.payee.id = :payeeId AND p.status = 'RELEASED' AND p.id != :excludeId ORDER BY p.releasedAt DESC")
    List<Payout> findLastReleasedPayoutForHost(@Param("payeeId") Long payeeId, @Param("excludeId") Long excludeId);

    // Count payouts for a specific trip (by booking's trip)
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.booking.trip.id = :tripId")
    long countByTripId(@Param("tripId") Long tripId);

    // Sum released payouts for a specific trip
    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payout p WHERE p.booking.trip.id = :tripId AND p.status = 'RELEASED'")
    BigDecimal sumReleasedByTripId(@Param("tripId") Long tripId);

    // Check if there's an active (pending) payout request for a trip
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.trip.id = :tripId AND p.payee.id = :hostId AND p.status IN ('REQUESTED', 'PENDING', 'APPROVED', 'ON_HOLD')")
    long countActivePendingByTripIdAndHostId(@Param("tripId") Long tripId, @Param("hostId") Long hostId);

    // Sum of all released payouts for a specific trip (using trip_id field)
    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payout p WHERE p.trip.id = :tripId AND p.status = 'RELEASED'")
    BigDecimal sumReleasedByTripIdDirect(@Param("tripId") Long tripId);

    // Sum gross amount already released for a trip
    @Query("SELECT COALESCE(SUM(p.grossAmount), 0) FROM Payout p WHERE p.trip.id = :tripId AND p.status = 'RELEASED'")
    BigDecimal sumGrossReleasedByTripId(@Param("tripId") Long tripId);

    // Find payouts by trip and tranche
    @Query("SELECT p FROM Payout p WHERE p.trip.id = :tripId AND p.tranche = :tranche ORDER BY p.requestedAt DESC")
    List<Payout> findByTripIdAndTranche(@Param("tripId") Long tripId, @Param("tranche") com.tourly.payment.enums.PayoutTranche tranche);

    // Check if a tranche has been released for a trip
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.trip.id = :tripId AND p.tranche = :tranche AND p.status = 'RELEASED'")
    long countReleasedByTripIdAndTranche(@Param("tripId") Long tripId, @Param("tranche") com.tourly.payment.enums.PayoutTranche tranche);

    // Find all payouts for a trip ordered by tranche
    @Query("SELECT p FROM Payout p WHERE p.trip.id = :tripId AND p.payee.id = :hostId ORDER BY p.requestedAt ASC")
    List<Payout> findByTripIdAndHostId(@Param("tripId") Long tripId, @Param("hostId") Long hostId);
}
