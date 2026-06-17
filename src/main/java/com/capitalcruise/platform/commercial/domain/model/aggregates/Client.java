package com.capitalcruise.platform.commercial.domain.model.aggregates;

import com.capitalcruise.platform.commercial.domain.model.valueobjects.ClientName;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentNumber;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.DocumentType;
import com.capitalcruise.platform.commercial.domain.model.valueobjects.EmailAddress;
import com.capitalcruise.platform.shared.domain.exceptions.InvalidBusinessRuleException;
import com.capitalcruise.platform.shared.domain.model.aggregates.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Client extends AuditableModel {

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, unique = true, length = 40)
    private String documentNumber;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "monthly_income", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(length = 1000)
    private String notes;

    public Client(ClientName name,
                  DocumentType documentType,
                  DocumentNumber documentNumber,
                  EmailAddress emailAddress,
                  String phone,
                  String address,
                  BigDecimal monthlyIncome,
                  String notes) {
        assignName(name);
        assignDocumentType(documentType);
        assignDocumentNumber(documentNumber);
        assignEmail(emailAddress);
        assignPhone(phone);
        assignAddress(address);
        assignMonthlyIncome(monthlyIncome);
        assignNotes(notes);
    }

    public static Client create(String firstName,
                                String lastName,
                                DocumentType documentType,
                                String documentNumber,
                                String email,
                                String phone,
                                String address,
                                BigDecimal monthlyIncome,
                                String notes) {
        return new Client(
                new ClientName(firstName, lastName),
                documentType,
                new DocumentNumber(documentNumber),
                email == null || email.isBlank() ? null : new EmailAddress(email),
                phone,
                address,
                monthlyIncome,
                notes
        );
    }

    public void update(String firstName,
                       String lastName,
                       DocumentType documentType,
                       String documentNumber,
                       String email,
                       String phone,
                       String address,
                       BigDecimal monthlyIncome,
                       String notes) {
        assignName(new ClientName(firstName, lastName));
        assignDocumentType(documentType);
        assignDocumentNumber(new DocumentNumber(documentNumber));
        assignEmail(email == null || email.isBlank() ? null : new EmailAddress(email));
        assignPhone(phone);
        assignAddress(address);
        assignMonthlyIncome(monthlyIncome);
        assignNotes(notes);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    private void assignName(ClientName name) {
        this.firstName = name.firstName();
        this.lastName = name.lastName();
    }

    private void assignDocumentType(DocumentType documentType) {
        if (documentType == null) {
            throw new InvalidBusinessRuleException("Document type is required");
        }
        this.documentType = documentType;
    }

    private void assignDocumentNumber(DocumentNumber documentNumber) {
        this.documentNumber = documentNumber.value();
    }

    private void assignEmail(EmailAddress emailAddress) {
        this.email = emailAddress != null ? emailAddress.value() : null;
    }

    private void assignPhone(String phone) {
        this.phone = normalizeOptional(phone);
    }

    private void assignAddress(String address) {
        this.address = normalizeOptional(address);
    }

    private void assignMonthlyIncome(BigDecimal monthlyIncome) {
        if (monthlyIncome == null) {
            throw new InvalidBusinessRuleException("Monthly income is required");
        }
        if (monthlyIncome.signum() < 0) {
            throw new InvalidBusinessRuleException("Monthly income must be greater than or equal to zero");
        }
        this.monthlyIncome = monthlyIncome;
    }

    private void assignNotes(String notes) {
        this.notes = normalizeOptional(notes);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    public Long getId() {
        return super.getId();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public String getNotes() {
        return notes;
    }
}
