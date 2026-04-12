package com.monkey.mcbot.licenseserver;

import com.monkey.mcbot.licenseserver.config.LicenseServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = LicenseServerProperties.class)
public class LicenseServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LicenseServerApplication.class, args);
    }
}
