package com.rubix.pension.userAuth.repository;

import com.rubix.pension.userAuth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserLogin(String userLogin);
    boolean existsByUserLogin(String userLogin);
}

