package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.exception.NotValidR2Z;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileValidatorTest {

    @TempDir
    Path temporaryDirectory;

    private final FileValidator validator = new FileValidator();

    @Test
    void acceptsZipContainingExportR2x() throws IOException {
        Path profile = createZip("valid.r2z", "export.r2x");

        assertDoesNotThrow(() -> validator.validateR2Z(profile));
    }

    @Test
    void rejectsZipWithoutExportR2x() throws IOException {
        Path profile = createZip("missing-export.r2z", "other.txt");

        assertThrows(NotValidR2Z.class, () -> validator.validateR2Z(profile));
    }

    @Test
    void rejectsNonZipFile() throws IOException {
        Path profile = temporaryDirectory.resolve("invalid.r2z");
        Files.writeString(profile, "not a zip archive");

        assertThrows(NotValidR2Z.class, () -> validator.validateR2Z(profile));
    }

    private Path createZip(String filename, String entryName) throws IOException {
        Path profile = temporaryDirectory.resolve(filename);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(profile))) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write("profile".getBytes());
            zip.closeEntry();
        }
        return profile;
    }
}
