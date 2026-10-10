package dev.kacperjackiewicz.r2mod_sync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "directory")
public record DirectoryProperties(String workspace) {
}
