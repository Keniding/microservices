package com.dipierplus.users.controller;

import com.dipierplus.users.model.User;
import com.dipierplus.users.repository.UserRepository;
import com.dipierplus.users.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Iterable<User> listUser(){return userService.findAll();}

    @PostMapping
    public void store(@RequestBody User user) {userService.createUser(user);}

    @PutMapping
    public void edit(@RequestBody User user) {userService.updateUser(user);}

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable("id") String id) {userService.deleteUser(id);}
}
