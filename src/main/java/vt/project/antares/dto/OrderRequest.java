package vt.project.antares.dto;

public record OrderRequest(
        Long customerId,
        Long productId,
        int quantity
) {
}