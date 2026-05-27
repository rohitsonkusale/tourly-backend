package com.tourly.auth.entity;

public enum PermissionName {

    // ================================
    // AUTH / USER
    // ================================
    USER_REGISTER,
    USER_LOGIN,
    USER_VIEW_PROFILE,
    USER_UPDATE_PROFILE,
    USER_DELETE_SELF,

    // ================================
    // TRIP
    // ================================
    TRIP_CREATE,
    TRIP_VIEW,
    TRIP_UPDATE,
    TRIP_DELETE,
    TRIP_SOFT_DELETE,
    TRIP_RESTORE,

    // ================================
    // BOOKING
    // ================================
    BOOKING_CREATE,
    BOOKING_VIEW_OWN,
    BOOKING_CANCEL_OWN,
    BOOKING_VIEW_ALL,
    BOOKING_CONFIRM,

    // ================================
    // PAYMENT
    // ================================
    PAYMENT_CREATE_ORDER,
    PAYMENT_VERIFY,
    PAYMENT_VIEW_OWN,
    PAYMENT_VIEW_ALL,
    PAYMENT_REFUND,

    // ================================
    // PLANNER / HOST
    // ================================
    PLANNER_VIEW_OWN_TRIPS,
    PLANNER_VIEW_OWN_BOOKINGS,
    HOST_MANAGE_TRIP,

    // ================================
    // ADMIN
    // ================================
    ADMIN_VIEW_ALL_USERS,
    ADMIN_UPDATE_USER_STATUS,
    ADMIN_VIEW_ALL_TRIPS,
    ADMIN_TRIP_MODERATE,
    ADMIN_VIEW_ALL_BOOKINGS,
    ADMIN_VIEW_ALL_PAYMENTS
}