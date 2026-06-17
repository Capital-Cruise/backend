package com.capitalcruise.platform.iam.domain.model.aggregates;

import com.capitalcruise.platform.iam.domain.model.entities.Role;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.model.aggregates.AuditableModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AuditableModel {

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

    public User(String username, String email, String passwordHash, boolean active, Instant lastLoginAt, Set<Role> roles) {
        assignUsername(username);
        assignEmail(email);
        assignPasswordHash(passwordHash);
        this.active = active;
        this.lastLoginAt = lastLoginAt;
        assignRoles(roles);
    }

    public static User register(String username, String email, String passwordHash, Set<Role> roles) {
        return new User(username, email, passwordHash, true, null, roles);
    }

    public static User register(String username, String passwordHash, Set<Role> roles) {
        return register(username, username + "@local", passwordHash, roles);
    }

    public Set<String> roleNames() {
        return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }

    public void markLogin(Instant loginAt) {
        this.lastLoginAt = loginAt;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateIdentity(String username, String email) {
        assignUsername(username);
        assignEmail(email);
    }

    public void changePasswordHash(String passwordHash) {
        assignPasswordHash(passwordHash);
    }

    private void assignUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidBusinessRuleException("Username cannot be empty");
        }
        this.username = username.trim().toLowerCase();
    }

    private void assignEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidBusinessRuleException("Email cannot be empty");
        }
        this.email = email.trim().toLowerCase();
    }

    private void assignPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidBusinessRuleException("Password hash cannot be empty");
        }
        this.passwordHash = passwordHash;
    }

    private void assignRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new InvalidBusinessRuleException("User must have at least one role");
        }
        this.roles = new LinkedHashSet<>(roles);
    }
    public Long getId() {
        return super.getId();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
