package com.rubix.pension.userAuth.service;

import com.rubix.pension.userAuth.dto.UpdateUser;
import com.rubix.pension.userAuth.dto.FirstLoginPasswordRequest;
import com.rubix.pension.userAuth.entity.User;
import com.rubix.pension.userAuth.exception.InvalidCredentialsException;
import com.rubix.pension.userAuth.exception.UserNotFoundException;
import com.rubix.pension.userAuth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository repository) {
        this.userRepository = repository;
    }

    public User createUser(User newUser) {
        if (newUser.getUserLogin() == null || newUser.getUserLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("User Login can not be empty");
        }
        if (userRepository.existsByUserLogin(newUser.getUserLogin())) {
            throw new IllegalArgumentException("User Login can not be duplicated");
        }
        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User login(String userLogin, String password){

        User user = userRepository.findByUserLogin(userLogin)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid Login ID or Password"));

        if(!user.getPassword().equals(password)){
            throw new InvalidCredentialsException("Invalid Login ID or Password");
        }

        return user;
    }

    public User updateUser(Integer Id,UpdateUser newUser){

        User user=userRepository.findById(Id).orElseThrow(
                ()-> new UserNotFoundException("User not found")
        );

        if(newUser.getUserLogin()!= null){
            if(!newUser.getUserLogin().trim().isEmpty()
                    && !newUser.getUserLogin().equals(user.getUserLogin())
                    && userRepository.existsByUserLogin(newUser.getUserLogin())){
                throw new IllegalArgumentException(
                        "User Login can not be duplicated");

            }
            user.setUserLogin(newUser.getUserLogin());
        }
        if(newUser.getFullName()!=null){
            user.setFullName(newUser.getFullName());
        }
        if(newUser.getUserSecurity()!=null) {
            user.setUserSecurity(newUser.getUserSecurity());
        }

        return userRepository.save(user);
    }

    public User changePassword(Integer Id){

        User user=userRepository.findById(Id).orElseThrow(
                ()-> new UserNotFoundException("User not found")
        );

        user.setPassword("Password");
        return userRepository.save(user);
    }

    public User changeFirstLoginPassword(Integer Id, FirstLoginPasswordRequest requestUser){

        User loginUser=userRepository.findById(Id).orElseThrow(
                ()-> new UserNotFoundException("User not found")
        );

        if(!loginUser.getPassword().equals(requestUser.getCurrentPassword())){
            throw new InvalidCredentialsException("Invalid Current Password");
        }
        if (requestUser.getNewPassword().equals(requestUser.getCurrentPassword())){
            throw new InvalidCredentialsException("New password can not be the same as the current password");
        }
        loginUser.setPassword(requestUser.getNewPassword());

        return userRepository.save(loginUser);

    }


}
