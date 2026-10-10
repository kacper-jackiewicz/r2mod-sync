package dev.kj.r2mod_sync.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.kj.r2mod_sync.config.DirectoryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final DirectoryProperties directoryProperties;

    public Path receive(MultipartFile file) throws IOException {
        if (file == null || file.getOriginalFilename() == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a non-empty .r2z file to upload.");
        }
        Path workspace = Path.of(directoryProperties.workspace()).toAbsolutePath().normalize();
        Files.createDirectories(workspace);
        Path destination = workspace.resolve("candidate.r2z");
        file.transferTo(destination);
        return destination;
    }

    public File send() {
        return Path.of(directoryProperties.workspace())
                .toAbsolutePath()
                .normalize()
                .resolve("current.r2z")
                .toFile();
    }
}
