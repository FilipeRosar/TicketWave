package com.projeto.ticket_wave.modules.organizations.infrastructure.security;

import com.projeto.ticket_wave.shared.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class OrganizationDocumentEncryption {
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final SecureRandom random = new SecureRandom();
    private final SecretKey key;

    public OrganizationDocumentEncryption(@Value("${app.organizations.document-key}") String configuredKey) {
        if (configuredKey.isBlank()) {
            key = null;
            return;
        }
        byte[] bytes = Base64.getDecoder().decode(configuredKey.strip());
        if (bytes.length != 32) throw new IllegalArgumentException("ORGANIZATION_DOCUMENT_KEY deve ter 32 bytes em Base64.");
        key = new SecretKeySpec(bytes, "AES");
    }

    public byte[] encrypt(String document) {
        if (document == null) return null;
        if (key == null) throw new StorageException("Chave de criptografia de documentos indisponível.");
        byte[] nonce = new byte[NONCE_BYTES];
        random.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
            byte[] encrypted = cipher.doFinal(document.getBytes(StandardCharsets.US_ASCII));
            return ByteBuffer.allocate(nonce.length + encrypted.length).put(nonce).put(encrypted).array();
        } catch (GeneralSecurityException error) {
            throw new StorageException("Falha ao criptografar documento cadastral.");
        }
    }
}
