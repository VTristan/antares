package vt.project.antares.hexa.domain.model;

import vt.project.antares.ntiers.entity.OrderStatus;
import vt.project.antares.ntiers.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerOrder (
        Long id,
        Product product,
        int quantity,
        BigDecimal unitPrice,
        LocalDateTime createdAt,
        OrderStatus status
) {}