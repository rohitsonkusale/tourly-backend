package com.tourly.payment.entity;

import com.tourly.booking.entity.Booking;
import com.tourly.payment.enums.PaymentStageStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_stages", uniqueConstraints = {
    @UniqueConstraint(name = "uq_payment_stages_booking_stage", columnNames = {"booking_id", "stage_number"})
})
public class PaymentStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_stage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "stage_number", nullable = false)
    private Integer stageNumber;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PaymentStageStatus status = PaymentStageStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "invoice_open_date")
    private LocalDate invoiceOpenDate;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "invoice_sent_at")
    private LocalDateTime invoiceSentAt;

    @Column(name = "is_immediate", nullable = false)
    private Boolean isImmediate = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PaymentStage() {}

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Integer getStageNumber() { return stageNumber; }
    public void setStageNumber(Integer stageNumber) { this.stageNumber = stageNumber; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

    public PaymentStageStatus getStatus() { return status; }
    public void setStatus(PaymentStageStatus status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getInvoiceOpenDate() { return invoiceOpenDate; }
    public void setInvoiceOpenDate(LocalDate invoiceOpenDate) { this.invoiceOpenDate = invoiceOpenDate; }

    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }

    public LocalDateTime getInvoiceSentAt() { return invoiceSentAt; }
    public void setInvoiceSentAt(LocalDateTime invoiceSentAt) { this.invoiceSentAt = invoiceSentAt; }

    public Boolean getIsImmediate() { return isImmediate; }
    public void setIsImmediate(Boolean isImmediate) { this.isImmediate = isImmediate; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
