package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.exception.NotValidR2Z;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class FileValidator {

    private static final Pattern WINDOWS_DRIVE_PREFIX = Pattern.compile("^[A-Za-z]:");

    public void validateR2Z(Path file) {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            if (!zip.isValidZipFile()) {
                throw new NotValidR2Z("Invalid zip file");
            }

            List<FileHeader> headers = zip.getFileHeaders();
            if (headers.stream().noneMatch(header -> "export.r2x".equals(header.getFileName()))) {
                throw new NotValidR2Z("Invalid zip file");
            }

            for (FileHeader header : headers) {
                validateEntryName(header.getFileName());
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new NotValidR2Z(e);
        }
    }

    private void validateEntryName(String entryName) {
        String normalizedEntryName = entryName.replace('\\', '/');
        if (normalizedEntryName.isBlank()
                || normalizedEntryName.startsWith("/")
                || WINDOWS_DRIVE_PREFIX.matcher(normalizedEntryName).find()) {
            throw new NotValidR2Z("Archive contains an unsafe path");
        }

        for (String pathSegment : normalizedEntryName.split("/")) {
            if ("..".equals(pathSegment)) {
                throw new NotValidR2Z("Archive contains an unsafe path");
            }
        }
    }
}
