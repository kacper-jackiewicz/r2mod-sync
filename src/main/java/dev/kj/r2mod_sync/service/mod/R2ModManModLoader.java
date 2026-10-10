package dev.kj.r2mod_sync.service.mod;

import dev.kj.r2mod_sync.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class R2ModManModLoader implements ModLoader {

    private final AppProperties appProperties;

    @SneakyThrows
    @Override
    public void loadMods(Path modPack, Path destination) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "r2podman-headless",
                "--install-dir=" + destination.toAbsolutePath().normalize(),
                "--work-dir=" + appProperties.tmpDir().toAbsolutePath().normalize(),
                "--profile-zip=" + modPack.toAbsolutePath().normalize());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        if (process.waitFor() != 0) {
            throw new IOException("r2podman-headless failed check host logs");
        }
    }
}
