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

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

    public User(String username, String passwordHash, Set<Role> roles) {
        assignUsername(username);
        assignPasswordHash(passwordHash);
        assignRoles(roles);
    }

    public static User register(String username, String passwordHash, Set<Role> roles) {
        return new User(username, passwordHash, roles);
    }

    public Set<String> roleNames() {
        return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
    }

    private void assignUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidBusinessRuleException("Username cannot be empty");
        }
        this.username = username.trim().toLowerCase();
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
}

