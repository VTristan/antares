package vt.project.antares.ntiers.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vt.project.antares.ntiers.dto.OrderDto;
import vt.project.antares.ntiers.dto.OrderRequest;
import vt.project.antares.ntiers.entity.Customer;
import vt.project.antares.ntiers.entity.CustomerOrder;
import vt.project.antares.ntiers.entity.Product;
import vt.project.antares.ntiers.repository.CustomerOrderRepository;
import vt.project.antares.ntiers.repository.CustomerRepository;
import vt.project.antares.ntiers.repository.ProductRepository;

import java.util.List;

@Service
public class OrderService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository orderRepository;

    public OrderService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            CustomerOrderRepository orderRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderDto createOrder(OrderRequest request) {

        // Règle métier 1
        isUpperToZero(request);

        Customer customer = customerRepository
                .findById(request.customerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found")
                );

        Product product = productRepository
                .findById(request.productId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found")
                );

        // Règle métier 2
        if (product.getStock() < request.quantity()) {
            throw new IllegalStateException(
                    "Not enough stock"
            );
        }

        product.decreaseStock(request.quantity());

        CustomerOrder order = new CustomerOrder(
                customer,
                product,
                request.quantity()
        );

        CustomerOrder savedOrder = orderRepository.save(order);

        return toDto(savedOrder);
    }

    private static void isUpperToZero(OrderRequest request) {
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    public List<OrderDto> findByCustomer(Long customerId) {

        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private OrderDto toDto(CustomerOrder order) {

        return new OrderDto(
                order.getId(),
                order.getCustomer().getId(),
                order.getProduct().getId(),
                order.getProduct().getName(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getStatus()
        );
    }
}