package app.model.repository;

import java.util.List;

import app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    //@Query("SELECT FROM apae_user u where u.login = ?1 AND u.password = ?2")
    //Iterable<User> verify(String login, String password);
    List<User> findByLogin(String login);
}