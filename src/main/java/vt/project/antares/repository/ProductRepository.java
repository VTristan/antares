package vt.project.antares.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vt.project.antares.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}