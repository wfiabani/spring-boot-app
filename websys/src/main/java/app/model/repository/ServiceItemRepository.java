package app.model.repository;

import java.util.List;

import app.model.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
    List<ServiceItem> findByMovementId(Integer id);
    List<ServiceItem> findByServiceId(Integer id);
}