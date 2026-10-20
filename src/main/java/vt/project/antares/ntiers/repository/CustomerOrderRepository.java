package vt.project.antares.ntiers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vt.project.antares.ntiers.entity.CustomerOrder;

import java.util.List;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomerId(Long customerId);
}