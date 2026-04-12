package com.monkey.mcbot.licenseserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "licenses")
public class LicenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "license_key_hmac", nullable = false, unique = true, length = 64)
    private String licenseKeyHmac;

    @Column(name = "license_key_prefix", nullable = false, length = 4)
    private String licenseKeyPrefix;

    @Column(name = "license_key_encrypted", length = 255)
    private String licenseKeyEncrypted;

    @Column(name = "customer_id", nullable = false, length = 128)
    private String customerId;

    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    @Column(name = "plan_code", nullable = false, length = 64)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private LicenseStatus status;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_servers", nullable = false)
    private int maxServers;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getLicenseKeyHmac() {
        return licenseKeyHmac;
    }

    public void setLicenseKeyHmac(String licenseKeyHmac) {
        this.licenseKeyHmac = licenseKeyHmac;
    }

    public String getLicenseKeyPrefix() {
        return licenseKeyPrefix;
    }

    public void setLicenseKeyPrefix(String licenseKeyPrefix) {
        this.licenseKeyPrefix = licenseKeyPrefix;
    }

    public String getLicenseKeyEncrypted() {
        return licenseKeyEncrypted;
    }

    public void setLicenseKeyEncrypted(String licenseKeyEncrypted) {
        this.licenseKeyEncrypted = licenseKeyEncrypted;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public LicenseStatus getStatus() {
        return status;
    }

    public void setStatus(LicenseStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getMaxServers() {
        return maxServers;
    }

    public void setMaxServers(int maxServers) {
        this.maxServers = maxServers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
    }
}
