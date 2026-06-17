package com.capitalcruise.platform.creditoperation.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "public_quote_shares")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicQuoteShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false)
    private Long operationId;

    @Column(name = "share_token", nullable = false, unique = true, length = 80)
    private String shareToken;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    public PublicQuoteShare(Long operationId,
                            String shareToken,
                            Boolean active,
                            Instant expiresAt,
                            Instant createdAt,
                            Long createdByUserId) {
        this.operationId = operationId;
        this.shareToken = shareToken;
        this.active = active;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.createdByUserId = createdByUserId;
    }
    public Long getId() {
        return id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public Boolean getActive() {
        return active;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }
}
