package app.model.repository;

import java.util.List;

import app.model.entity.SKU;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SKURepository extends JpaRepository<SKU, Integer> {
    List<SKU> findByProductId(Integer productId);
    List<SKU> findByExternalId(String externalId);
    List<SKU> xespa();
}