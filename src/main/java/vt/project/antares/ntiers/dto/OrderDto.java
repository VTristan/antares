package vt.project.antares.ntiers.dto;

import vt.project.antares.ntiers.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDto(
        Long id,
        Long customerId,
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        OrderStatus status
) {
}