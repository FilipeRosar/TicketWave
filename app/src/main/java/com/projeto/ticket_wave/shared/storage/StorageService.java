package com.projeto.ticket_wave.shared.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String upload(MultipartFile file, String folder);
    void delete(String key);
}
