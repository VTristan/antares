package vt.project.antares.ntiers.service;

import org.springframework.stereotype.Service;
import vt.project.antares.ntiers.dto.CustomerDto;
import vt.project.antares.ntiers.entity.Customer;
import vt.project.antares.ntiers.exception.CustomerNotFoundException;
import vt.project.antares.ntiers.repository.CustomerRepository;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<CustomerDto> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(this::toDto)
                .toList();
    }

    public CustomerDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public List<CustomerDto> findByLastName(String lastName) {
        return repository.findByLastName(lastName)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public CustomerDto create(CustomerDto dto) {

        Customer customer = new Customer(
                dto.firstName(),
                dto.lastName()
        );

        return toDto(repository.save(customer));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        repository.deleteById(id);
    }

    private CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName()
        );
    }
}