package vt.project.antares.hexa.application.port.out;

import vt.project.antares.hexa.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerPersistencePort {

    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    List<Customer> findByLastName(String lastName);

    Customer save(Customer customer);

    boolean existsById(Long id);

    void deleteById(Long id);
}