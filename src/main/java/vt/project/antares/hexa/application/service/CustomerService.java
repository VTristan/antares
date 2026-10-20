package vt.project.antares.hexa.application.service;


import vt.project.antares.hexa.application.port.in.CustomerUseCase;
import vt.project.antares.hexa.application.port.out.CustomerPersistencePort;
import vt.project.antares.hexa.domain.exception.CustomerNotFoundException;
import vt.project.antares.hexa.domain.model.Customer;

import java.util.List;

public class CustomerService implements CustomerUseCase {

    private final CustomerPersistencePort customers;

    public CustomerService(CustomerPersistencePort customers) {
        this.customers = customers;
    }

    @Override
    public List<Customer> findAll() {
        return customers.findAll();
    }

    @Override
    public Customer findById(Long id) {
        return customers.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    public List<Customer> findByLastName(String lastName) {
        return customers.findByLastName(lastName);
    }

    @Override
    public Customer create(String firstName, String lastName) {
        return customers.save(
                new Customer(null, firstName, lastName)
        );
    }

    @Override
    public void delete(Long id) {
        if (!customers.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }

        customers.deleteById(id);
    }
}