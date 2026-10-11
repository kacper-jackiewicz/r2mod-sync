package dev.kj.r2mod_sync.service.mod;

import dev.kj.r2mod_sync.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2ModManModLoader implements ModLoader {

    private final AppProperties appProperties;

    @SneakyThrows
    @Override
    public void loadMods(Path modPack, Path destination) {

        String installDir = destination.toAbsolutePath().normalize().toString();
        String workDir = appProperties.tmpDir().toAbsolutePath().normalize().toString();
        String profileZip = modPack.toAbsolutePath().normalize().toString();

        log.info("Running r2podman-headless with install-dir={} work-dir={} profile-zip={}",
                installDir,
                workDir,
                profileZip);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "r2modman-headless",
                "--install-dir=" + installDir,
                "--work-dir=" + workDir,
                "--profile-zip=" + profileZip);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        if (process.waitFor() != 0) {
            log.error("r2podman-headless failed check host logs");
        }
    }

    @Override
    public void cleanModDir(Path destination) {
        try {
            Path pluginsDir = destination.resolve("BepInEx").resolve("plugins");
            log.info("Cleaning mod directory: {}", pluginsDir.toAbsolutePath().normalize());
            Optional.of(pluginsDir.toFile()).map(File::listFiles).map(Arrays::asList).orElse(Collections.emptyList()).forEach(File::delete);
        } catch (Exception e) {
            log.error("Could not clean mod directory: {}", destination.toAbsolutePath().normalize(), e);
        }
    }
}
