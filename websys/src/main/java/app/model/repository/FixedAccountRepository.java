package app.model.repository;

import java.util.List;

import app.model.entity.FixedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedAccountRepository extends JpaRepository<FixedAccount, Integer> {

    List<FixedAccount> findByScope(String scope);
}