package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.config.DirectoryProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    public static String CANDIDATE_FILE_NAME = "candidate.r2z";
    public static String CURRENT_FILE_NAME = "current.r2z";
    public static String VANILLA_FILE_NAME = "vanilla.r2z";

    private final DirectoryProperties directoryProperties;

    @PostConstruct
    public void initializeVanilla() {
        try (InputStream vanilla = getClass().getResourceAsStream("/" + VANILLA_FILE_NAME)) {
            Path vanillaPath = getVanilla();
            Files.createDirectories(vanillaPath.getParent().toAbsolutePath().normalize());
            Files.copy(vanilla, vanillaPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            log.error("Failed to initialize vanilla.r2z", exception);
        }
    }

    public Path receive(MultipartFile file) throws IOException {
        if (file == null || file.getOriginalFilename() == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a non-empty .r2z file to upload.");
        }
        Path workspace = Path.of(directoryProperties.workspace()).toAbsolutePath().normalize();
        Path destination = workspace.resolve(CANDIDATE_FILE_NAME);
        file.transferTo(destination);
        return destination;
    }

    public File send() {
        return getCurrentPath().toFile();
    }

    @SneakyThrows
    public void updateCurrent(Path candidate) {
        Files.copy(candidate, getCurrentPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @SneakyThrows
    public void deleteCandidate(Path candidate) {
        Files.delete(candidate);
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
