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
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + user.getId()));

        if (!existingUser.getUsername().equals(user.getUsername()) &&
                userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el nombre de usuario: " + user.getUsername());
        }

        if (!existingUser.getEmail().equals(user.getEmail()) &&
                userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con el email: " + user.getEmail());
        }

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setActive(user.isActive());
        existingUser.setRole(user.getRole());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            existingUser.setPassword(encoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

}
