package com.monkey.mcbot.licenseserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "license_events")
public class LicenseEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private LicenseEntity license;

    @Column(name = "installation_id", length = 64)
    private String installationId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "result", nullable = false, length = 32)
    private String result;

    @Column(name = "reason_code", length = 64)
    private String reasonCode;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void setLicense(LicenseEntity license) {
        this.license = license;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
