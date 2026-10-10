package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.config.DirectoryProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class FileService {

    public static String CANDIDATE_FILE_NAME = "candidate.r2z";
    public static String CURRENT_FILE_NAME = "current.r2z";
    public static String VANILLA_FILE_NAME = "vanilla.r2z";

    private final DirectoryProperties directoryProperties;

    public Path receive(MultipartFile file) throws IOException {
        if (file == null || file.getOriginalFilename() == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a non-empty .r2z file to upload.");
        }
        Path workspace = Path.of(directoryProperties.workspace()).toAbsolutePath().normalize();
        Files.createDirectories(workspace);
        Path destination = workspace.resolve(CANDIDATE_FILE_NAME);
        file.transferTo(destination);
        return destination;
    }

    public File send() {
        return getCurrentPath().toFile();
    }

    @SneakyThrows
    public void updateCurrent(Path candidate) {
        Files.move(candidate, getCurrentPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private Path getCurrentPath() {
        return Path.of(directoryProperties.workspace())
                .toAbsolutePath()
                .normalize()
                .resolve(CURRENT_FILE_NAME);
    }

    public Path getVanilla() {
        return Path.of(directoryProperties.workspace())
                .toAbsolutePath()
                .normalize()
                .resolve(VANILLA_FILE_NAME);
    }
}
