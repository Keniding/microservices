package com.dipierplus.gateway.security;

import com.dipierplus.gateway.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomReactiveUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        log.info("Buscando usuario por username: {}", username);

        return userRepository.findByUsername(username)
                .doOnNext(user -> log.info("Usuario encontrado en la base de datos: {}", user))
                .map(user -> {
                    UserDetailsImp userDetails = new UserDetailsImp(user);
                    log.info("UserDetails creado exitosamente para: {}", username);
                    log.debug("Roles asignados: {}", userDetails.getAuthorities());
                    return userDetails;
                })
                .cast(UserDetails.class)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Usuario no encontrado: {}", username);
                    return Mono.error(new UsernameNotFoundException(
                            "Usuario no encontrado: " + username
                    ));
                }))
                .doOnError(error -> log.error("Error al buscar usuario: {}", error.getMessage()))
                .doOnSuccess(userDetails -> log.info(
                        "Usuario autenticado exitosamente: {}", username
                ));
    }
}
