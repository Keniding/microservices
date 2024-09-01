package com.dipierplus.users.controller;

import com.dipierplus.users.model.User;
import com.dipierplus.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    public List<User> listUser(){return userRepository.findAll();}

    @PostMapping
    public void store(@RequestBody User user) {userRepository.save(user);}

    @PutMapping
    public void edit(@RequestBody User user) {userRepository.save(user);}

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable("id") String id) {userRepository.deleteById(id);}
}
