package com.example.movie.entity;

public enum OrderStatus {
    /** 待支付 */
    CREATED,
    /** 已支付/已出票 */
    PAID,
    /** 已取消 */
    CANCELLED,
    /** 已退款 */
    REFUNDED,
    /** 支付超时 */
    EXPIRED
}
