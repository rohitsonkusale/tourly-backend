package com.tourly.payment.dto.response;

public class PaymentResponse {

    private String razorpayOrderId;
    private Long bookingId;
    private String status;

    // getters and setters

    /**
     * @return String return the razorpayOrderId
     */
    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    /**
     * @param razorpayOrderId the razorpayOrderId to set
     */
    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    /**
     * @return Long return the bookingId
     */
    public Long getBookingId() {
        return bookingId;
    }

    /**
     * @param bookingId the bookingId to set
     */
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
     * @return String return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

}