package vt.project.antares.ntiers.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vt.project.antares.ntiers.dto.OrderDto;
import vt.project.antares.ntiers.dto.OrderRequest;
import vt.project.antares.ntiers.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto create(@RequestBody OrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping("/customer/{customerId}")
    public List<OrderDto> findByCustomer(
            @PathVariable Long customerId
    ) {
        return service.findByCustomer(customerId);
    }
}