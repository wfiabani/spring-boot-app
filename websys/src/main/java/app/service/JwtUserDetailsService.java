package app.service;

import java.util.ArrayList;
import java.util.List;

import app.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<app.model.entity.User> users = userRepository.findByLogin(username);
        if (users.size() == 1) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            return new User(
                    users.get(0).getLogin(),
                    encoder.encode(users.get(0).getPassword()),
                    new ArrayList<>()
            );
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}