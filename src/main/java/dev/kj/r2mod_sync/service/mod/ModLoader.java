package dev.kj.r2mod_sync.service.mod;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public interface ModLoader {

    void loadMods(Path modPack, Path destination);

    default void cleanModDir(Path destination) {
        Path pluginsDir = destination.resolve("BepInEx").resolve("plugins");
        Optional.of(pluginsDir.toFile()).map(File::listFiles).map(Arrays::asList).orElse(Collections.emptyList()).forEach(File::delete);
    }
}
