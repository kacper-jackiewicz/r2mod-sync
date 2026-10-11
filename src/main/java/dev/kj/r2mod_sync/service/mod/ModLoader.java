package dev.kj.r2mod_sync.service.mod;

import java.nio.file.Path;

public interface ModLoader {

    void loadMods(Path modPack, Path destination);

    void cleanModDir(Path destination);
}
