package com.capitalcruise.platform.referencedata.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "exchange_rates")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "quoted_at", nullable = false)
    private Instant quotedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ExchangeRate(String baseCurrency,
                        String quoteCurrency,
                        BigDecimal rate,
                        String source,
                        Instant quotedAt) {
        this.baseCurrency = normalizeCurrency(baseCurrency);
        this.quoteCurrency = normalizeCurrency(quoteCurrency);
        this.rate = rate;
        this.source = source;
        this.quotedAt = quotedAt;
    }

    public static ExchangeRate of(String baseCurrency,
                                  String quoteCurrency,
                                  BigDecimal rate,
                                  String source,
                                  Instant quotedAt) {
        return new ExchangeRate(baseCurrency, quoteCurrency, rate, source, quotedAt);
    }

    public void update(BigDecimal rate, String source, Instant quotedAt) {
        this.rate = rate;
        this.source = source;
        this.quotedAt = quotedAt;
    }

    private String normalizeCurrency(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
    public Long getId() {
        return id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getSource() {
        return source;
    }

    public Instant getQuotedAt() {
        return quotedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
