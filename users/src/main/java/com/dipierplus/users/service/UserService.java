package com.dipierplus.users.service;

import com.dipierplus.users.model.User;
import com.dipierplus.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public User getUser(String id) {
        return userRepository.findById(id).orElseThrow();
    }

    public User createUser(User user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String passEncrypt = encoder.encode(user.getPassword());
        User userBuilder = User.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(passEncrypt)
                .active(user.isActive())
                .build();
        return userRepository.save(userBuilder);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
