package vt.project.antares.ntiers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vt.project.antares.ntiers.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}