package app.model.repository;

import java.util.Date;
import java.util.List;

import app.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByScope(String scope);
    List<Account> findByMovementId(Integer id);
    List<Account> findByExpirationDate(Date expirationDate);
}