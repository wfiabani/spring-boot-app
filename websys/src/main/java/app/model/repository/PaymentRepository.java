package app.model.repository;

import java.util.List;

import app.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByScopeAndFixedAccountId(String scope, Integer fixedAccountId);
}