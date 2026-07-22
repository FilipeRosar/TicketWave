package com.projeto.ticket_wave.application.service;

import com.projeto.ticket_wave.application.interfaces.StorageService;
import com.projeto.ticket_wave.infrastructure.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public String upload(MultipartFile file, String folder) {
        return "";
    }

    @Override
    public void delete(String key) {

    }
}
