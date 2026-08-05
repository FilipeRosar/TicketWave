package com.projeto.ticket_wave.infrastructure.storage;

import com.projeto.ticket_wave.application.interfaces.StorageService;
import com.projeto.ticket_wave.infrastructure.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {
    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public String upload(MultipartFile file, String folder) {

        String extension = getExtension(file.getOriginalFilename());

        String key = folder + "/" + UUID.randomUUID() + "." + extension;

        try{
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );
            return buildUrl(key);
        } catch (IOException e){
            throw new StorageException("Erro ao enviar imagem: " + e.getMessage());
        }

    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    private String buildUrl(String key){
        return storageProperties.getEndpoint()
                + "/"
                + storageProperties.getBucket()
                + "/"
                + key;
    }
}
