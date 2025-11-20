package com.example.fitnessapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 30)
    @Column(nullable = false, length = 30)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Set<Role> roles = new HashSet<>();

    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private Integer heightCm;

    @Column(precision = 6, scale = 2)
    private BigDecimal currentWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Goal goal;

    @NotNull
    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(BigDecimal currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}


