package dev.kj.r2mod_sync.service.mod;

import dev.kj.r2mod_sync.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.nio.file.Path;

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

        ProcessResult processResult = new ProcessExecutor().command("r2modman-headless",
                        "--install-dir=" + installDir,
                        "--work-dir=" + workDir,
                        "--profile-zip=" + profileZip)
                .redirectOutput(Slf4jStream.ofCaller().asInfo())
                .redirectError(Slf4jStream.ofCaller().asError())
                .execute();

        if (processResult.getExitValue() != 0) {
            log.error("r2podman-headless failed check host logs");
        } else {
            log.info("Finished r2podman-headless");
        }
    }

    @Override
    public void cleanModDir(Path destination) {
        try {
            Path pluginsDir = destination.resolve("BepInEx").resolve("plugins");
            log.info("Cleaning mod directory: {}", pluginsDir.toAbsolutePath().normalize());
            FileUtils.cleanDirectory(pluginsDir.toFile());
        } catch (Exception e) {
            log.error("Could not clean mod directory: {}", destination.toAbsolutePath().normalize(), e);
        }
    }
}
