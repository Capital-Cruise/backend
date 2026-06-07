package com.capitalcruise.platform.profiles.domain.model.aggregates;

import com.capitalcruise.platform.profiles.domain.model.valueobjects.DocumentNumber;
import com.capitalcruise.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.capitalcruise.platform.profiles.domain.model.valueobjects.FirstName;
import com.capitalcruise.platform.profiles.domain.model.valueobjects.LastName;
import com.capitalcruise.platform.shared.domain.model.aggregates.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends AuditableModel {

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 8)
    private String documentNumber;

    @Column
    private Long userId;

    public Profile(FirstName firstName,
                   LastName lastName,
                   EmailAddress emailAddress,
                   DocumentNumber documentNumber,
                   Long userId) {
        this.firstName = firstName.value();
        this.lastName = lastName.value();
        this.email = emailAddress.value();
        this.documentNumber = documentNumber != null ? documentNumber.value() : null;
        this.userId = userId;
    }

    public static Profile create(String firstName,
                                 String lastName,
                                 String email,
                                 String documentNumber,
                                 Long userId) {
        DocumentNumber resolvedDocumentNumber =
                (documentNumber == null || documentNumber.isBlank()) ? null : new DocumentNumber(documentNumber);
        return new Profile(
                new FirstName(firstName),
                new LastName(lastName),
                new EmailAddress(email),
                resolvedDocumentNumber,
                userId
        );
    }
}

