package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.exception.NotValidR2Z;
import net.lingala.zip4j.ZipFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

//TODO: Improve validation to check for more than just the presence of export.r2x
@Service
public class FileValidator {

    public void validateR2Z(Path file) {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            if (!zip.isValidZipFile() || zip.getFileHeader("export.r2x") == null) {
                throw new NotValidR2Z("Invalid zip file");
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new NotValidR2Z(e);
        }
    }
}
