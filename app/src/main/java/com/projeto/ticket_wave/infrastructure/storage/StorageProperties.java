package com.projeto.ticket_wave.infrastructure.storage;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {
    private String bucket;
    private String endpoint;
    private String region;
}
