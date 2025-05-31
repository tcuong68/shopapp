package com.devteria.identity_service.enums;

public enum OrderStatus {
    PENDING,       // Đang chờ xử lý
    CONFIRMED,     // Đã xác nhận
    PROCESSING,    // Đang xử lý
    SHIPPED,       // Đã giao cho đơn vị vận chuyển
    DELIVERED,     // Đã giao hàng
    CANCELLED       // Đã hủy
}
