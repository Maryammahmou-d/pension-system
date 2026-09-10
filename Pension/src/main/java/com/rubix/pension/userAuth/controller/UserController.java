package com.rubix.pension.userAuth.controller;

import com.rubix.pension.userAuth.dto.FirstLoginPasswordRequest;
import com.rubix.pension.userAuth.dto.LoginRequest;
import com.rubix.pension.userAuth.dto.UpdateUser;
import com.rubix.pension.userAuth.entity.User;
import com.rubix.pension.userAuth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User newUser) {
        return userService.createUser(newUser);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request){
        return userService.login(
                request.getUserLogin(),
                request.getPassword()
        );
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody UpdateUser user){
        return userService.updateUser(id, user);
    }

    @PutMapping("/{id}/reset-password")
    public User changePassword(@PathVariable Integer id){
        return userService.changePassword(id);
    }

    @PutMapping("/{id}/reset-first-login-password")
    public User changeFirstLoginPassword(@PathVariable Integer id, @RequestBody FirstLoginPasswordRequest requestUser){
        return userService.changeFirstLoginPassword(id,requestUser);
    }



}