package com.example.fitnessapp.repository;

import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.entities.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByActiveTrue();

    Page<User> findByActiveTrue(Pageable pageable);

    List<User> findByRoles(Role role);

    Page<User> findByRoles(Role role, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String usernameKeyword,
        String emailKeyword,
        Pageable pageable
    );
}


