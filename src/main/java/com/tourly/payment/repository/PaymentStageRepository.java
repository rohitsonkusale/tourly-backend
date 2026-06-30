package com.tourly.payment.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourly.payment.entity.PaymentStage;
import com.tourly.payment.enums.PaymentStageStatus;

public interface PaymentStageRepository extends JpaRepository<PaymentStage, Long> {

    List<PaymentStage> findByBookingIdOrderByStageNumberAsc(Long bookingId);

    /**
     * Find stages whose invoice window has opened but status is still PENDING.
     * These need to be transitioned to INVOICE_SENT.
     */
    @Query("SELECT ps FROM PaymentStage ps WHERE ps.status = :status " +
           "AND ps.invoiceOpenDate IS NOT NULL AND ps.invoiceOpenDate <= :today " +
           "AND ps.isImmediate = false")
    List<PaymentStage> findStagesReadyForInvoice(
            @Param("status") PaymentStageStatus status,
            @Param("today") LocalDate today);

    /**
     * Find stages that are overdue (deadline passed, still not paid).
     */
    @Query("SELECT ps FROM PaymentStage ps WHERE ps.status IN (:statuses) " +
           "AND ps.deadlineAt IS NOT NULL AND ps.deadlineAt < CURRENT_TIMESTAMP")
    List<PaymentStage> findOverdueStages(@Param("statuses") List<PaymentStageStatus> statuses);

    /**
     * Find all unpaid stages for a booking (for cancellation cleanup).
     */
    List<PaymentStage> findByBookingIdAndStatusIn(Long bookingId, List<PaymentStageStatus> statuses);

    Optional<PaymentStage> findByBookingIdAndStageNumber(Long bookingId, Integer stageNumber);

    /**
     * Find all pending/invoice-sent stages for bookings belonging to a specific traveler.
     * Used by the dashboard "Upcoming Payments" widget.
     */
    @Query("SELECT ps FROM PaymentStage ps " +
           "JOIN FETCH ps.booking b " +
           "LEFT JOIN FETCH b.trip t " +
           "LEFT JOIN FETCH t.destination " +
           "WHERE b.traveler.id = :travelerId " +
           "AND ps.status IN (:statuses) " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "ORDER BY ps.dueDate ASC, ps.deadlineAt ASC")
    List<PaymentStage> findUpcomingStagesForTraveler(
            @Param("travelerId") Long travelerId,
            @Param("statuses") List<PaymentStageStatus> statuses);

    /**
     * Find all stages with a specific status (for scheduler auto-cancel).
     */
    List<PaymentStage> findByStatus(PaymentStageStatus status);
}
