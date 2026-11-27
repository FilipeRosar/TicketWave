package com.projeto.ticket_wave.modules.identity.application.service;

import com.projeto.ticket_wave.modules.identity.application.dto.ParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.dto.UpdateParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.identity.application.port.ParticipantProfileRepository;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ParticipantProfileService {
    private static final int MAX_PHOTO_BYTES = 1024 * 1024;
    private static final int MAX_PHOTO_DIMENSION = 2048;

    private final CurrentUser currentUser;
    private final ParticipantProfileRepository profiles;

    public ParticipantProfile get() {
        return profiles.findByUserId(currentUser.getCurrentUser().getId())
                .orElseThrow(() -> new NotFoundException("Perfil ainda não criado."));
    }

    public ParticipantProfile save(UpdateParticipantProfile request) {
        MultipartFile file = request.photo();
        boolean hasPhoto = file != null && !file.isEmpty();
        boolean removePhoto = Boolean.TRUE.equals(request.removePhoto());
        if (hasPhoto && removePhoto) {
            throw new BusinessException("Escolha entre substituir ou remover a foto.");
        }

        String photoType = hasPhoto ? readPhotoType(file) : null;
        byte[] photo = null;
        if (hasPhoto) {
            try {
                photo = file.getBytes();
            } catch (IOException ex) {
                throw new BusinessException("Não foi possível ler a foto.");
            }
        }
        var profile = new ParticipantProfile(
                request.name().strip(), clean(request.occupation()), clean(request.city()),
                clean(request.bio()), photoType, photo);
        return profiles.save(currentUser.getCurrentUser().getId(), profile, hasPhoto || removePhoto);
    }

    public void delete() {
        profiles.deleteByUserId(currentUser.getCurrentUser().getId());
    }

    private String readPhotoType(MultipartFile file) {
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new BusinessException("A foto deve ter até 1 MB.");
        }

        try (var input = new MemoryCacheImageInputStream(file.getInputStream())) {
            var readers = ImageIO.getImageReaders(input);
            if (readers.hasNext()) {
                var reader = readers.next();
                try {
                    String type = switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
                        case "jpeg" -> "image/jpeg";
                        case "png" -> "image/png";
                        default -> throw new BusinessException("Envie uma foto JPEG ou PNG.");
                    };
                    reader.setInput(input, true, true);
                    if (reader.getWidth(0) > MAX_PHOTO_DIMENSION || reader.getHeight(0) > MAX_PHOTO_DIMENSION) {
                        throw new BusinessException("A foto deve ter até 2048 × 2048 pixels.");
                    }
                    reader.read(0);
                    return type;
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler a foto.");
        }
        throw new BusinessException("Envie uma foto JPEG ou PNG.");
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
