package com.dipierplus.users.controller;

import com.dipierplus.users.model.User;
import com.dipierplus.users.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Iterable<User> listUser(){return userService.findAll();}

    @GetMapping(value = "/{id}")
    public User findUser(@PathVariable String id){return userService.getUser(id);}

    @GetMapping(value = "/{name}/name")
    public User findUserName(@PathVariable String name){return userService.getUserForName(name);}

    @PostMapping
    public void store(@RequestBody User user) {userService.createUser(user);}

    @PutMapping("/{id}")
    public User edit(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        return userService.updateUser(user);
    }

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable("id") String id) {userService.deleteUser(id);}
}
