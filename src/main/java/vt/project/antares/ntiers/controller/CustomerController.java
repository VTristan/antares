package vt.project.antares.ntiers.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vt.project.antares.ntiers.dto.CustomerDto;
import vt.project.antares.ntiers.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CustomerDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/search")
    public List<CustomerDto> findByLastName(@RequestParam String lastName) {
        return service.findByLastName(lastName);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDto create(@RequestBody CustomerDto customer) {
        return service.create(customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}