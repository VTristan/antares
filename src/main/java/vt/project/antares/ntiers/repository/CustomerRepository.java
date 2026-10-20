package vt.project.antares.ntiers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vt.project.antares.ntiers.entity.Customer;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);
}