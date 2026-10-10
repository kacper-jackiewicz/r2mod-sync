package dev.kacperjackiewicz.r2mod_sync.service;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RtzUploadService {

    private final String processName;

    public RtzUploadService(@Value("${game-server.process-name:r2modman-headless}") String processName) {
        this.processName = processName;
    }

    public String getProcessName() {
        return processName;
    }

    public void receive(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a non-empty .rtz file to upload.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".rtz")) {
            throw new IllegalArgumentException("Only .rtz files are accepted.");
        }
    }
}
