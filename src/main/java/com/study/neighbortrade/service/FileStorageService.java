package com.study.neighbortrade.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public StoredFile storeProductImage(MultipartFile file) {
        validate(file);
        String original = file.getOriginalFilename() == null ? "image" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension = extension(original);
        String stored = UUID.randomUUID() + extension;
        Path dir = Path.of(uploadDir, "products").toAbsolutePath().normalize();
        Path target = dir.resolve(stored);
        try {
            Files.createDirectories(dir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장 중 오류가 발생했습니다.", e);
        }
        return new StoredFile(original, stored, "/uploads/products/" + stored, file.getContentType(), file.getSize());
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일을 선택해 주세요.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("이미지는 jpg, png, gif, webp 형식만 업로드할 수 있습니다.");
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "";
        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }

    @Getter
    @RequiredArgsConstructor
    public static class StoredFile {
        private final String originalFilename;
        private final String storedFilename;
        private final String url;
        private final String contentType;
        private final long size;
    }
}
