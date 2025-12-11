package com.example.fitnessapp.repository;

import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
    excludeAutoConfiguration = {FeignAutoConfiguration.class},
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop"}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindById() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setActive(true);

        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setUsername("finduser");
        user.setEmail("find@example.com");
        user.setPassword("password123");
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setActive(true);

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("finduser");
        assertTrue(found.isPresent());
        assertEquals("finduser", found.get().getUsername());
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setUsername("emailuser");
        user.setEmail("email@example.com");
        user.setPassword("password123");
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setActive(true);

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("email@example.com");
        assertTrue(found.isPresent());
        assertEquals("email@example.com", found.get().getEmail());
    }

    @Test
    void testFindByRoles() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password123");
        adminUser.setRoles(Set.of(Role.ROLE_ADMIN));
        adminUser.setActive(true);

        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword("password123");
        regularUser.setRoles(Set.of(Role.ROLE_USER));
        regularUser.setActive(true);

        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(regularUser);

        var admins = userRepository.findByRoles(Role.ROLE_ADMIN);
        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().anyMatch(u -> u.getUsername().equals("admin")));
    }
}

