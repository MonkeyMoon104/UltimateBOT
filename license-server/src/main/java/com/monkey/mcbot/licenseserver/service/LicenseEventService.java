package com.monkey.mcbot.licenseserver.service;

import com.monkey.mcbot.licenseserver.domain.LicenseEntity;
import com.monkey.mcbot.licenseserver.domain.LicenseEventEntity;
import com.monkey.mcbot.licenseserver.repo.LicenseEventRepository;
import org.springframework.stereotype.Service;

@Service
public class LicenseEventService {

    private final LicenseEventRepository eventRepository;

    public LicenseEventService(LicenseEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void log(LicenseEntity license,
                    String installationId,
                    String eventType,
                    String result,
                    String reasonCode,
                    String message,
                    String ip) {
        LicenseEventEntity event = new LicenseEventEntity();
        event.setLicense(license);
        event.setInstallationId(installationId);
        event.setEventType(eventType);
        event.setResult(result);
        event.setReasonCode(reasonCode);
        event.setMessage(message);
        event.setIp(ip);
        eventRepository.save(event);
    }
}
