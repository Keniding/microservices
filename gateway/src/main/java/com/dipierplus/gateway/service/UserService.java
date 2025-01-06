package com.dipierplus.gateway.service;

import com.dipierplus.gateway.model.User;
import com.dipierplus.gateway.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    public Mono<User> createUser(User user) {
        return userRepository.findByUsername(user.getUsername())
                .flatMap(existingUser -> Mono.error(
                        new RuntimeException("Ya existe un usuario con el nombre de usuario: " + user.getUsername())
                ))
                .then(userRepository.findByEmail(user.getEmail()))
                .flatMap(existingEmail -> Mono.error(
                        new RuntimeException("Ya existe un usuario con el email: " + user.getEmail())
                ))
                .then(Mono.defer(() -> {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    String passEncrypt = encoder.encode(user.getPassword());

                    User userBuilder = User.builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .password(passEncrypt)
                            .active(user.isActive())
                            .role(user.getRole())
                            .build();

                    LOGGER.info("Guardando nuevo usuario: " + user.getUsername());
                    return userRepository.save(userBuilder);
                }))
                .doOnSuccess(savedUser -> LOGGER.info("Usuario guardado exitosamente: " + savedUser.getUsername()))
                .doOnError(error -> LOGGER.severe("Error al guardar usuario: " + error.getMessage()));
    }
}
