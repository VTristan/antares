package vt.project.antares.hexa.application.port.in;

import vt.project.antares.hexa.domain.model.Customer;

import java.util.List;

public interface CustomerUseCase {

    List<Customer> findAll();

    Customer findById(Long id);

    List<Customer> findByLastName(String lastName);

    Customer create(String firstName, String lastName);

    void delete(Long id);
}