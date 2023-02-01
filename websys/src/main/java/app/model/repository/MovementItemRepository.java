package app.model.repository;

import java.util.List;

import app.model.entity.MovementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementItemRepository extends JpaRepository<MovementItem, Integer> {
    List<MovementItem> findByMovementId(Integer id);
    List<MovementItem> findByProductId(Integer id);
    List<MovementItem> findBySkuId(Integer id);
}