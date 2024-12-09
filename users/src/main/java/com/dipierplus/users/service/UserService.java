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

    public User getUserForName(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public void createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el nombre de usuario: " + user.getUsername());
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el email: " + user.getEmail());
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String passEncrypt = encoder.encode(user.getPassword());

        User userBuilder = User.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(passEncrypt)
                .active(user.isActive())
                .role(user.getRole())
                .build();

        userRepository.save(userBuilder);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
