package vt.project.antares.ntiers.dto;

public record OrderRequest(
        Long customerId,
        Long productId,
        int quantity
) {
}