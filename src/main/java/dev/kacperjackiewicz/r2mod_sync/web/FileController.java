package dev.kacperjackiewicz.r2mod_sync.web;

import java.io.File;
import java.io.IOException;

import dev.kacperjackiewicz.r2mod_sync.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService r2zService;

    @PostMapping("/server/upload")
    public ResponseEntity<Map<String, String>> uploadRtz(@RequestParam("file") MultipartFile file)
            throws IOException {
        try {
            r2zService.receive(file);
            return ResponseEntity.ok(Map.of("messageKey", "upload.success"));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("messageKey", "upload.error"));
        }
    }

    @GetMapping("/server/download")
    public ResponseEntity<Resource> downloadRtz() {
        File currentRtz = r2zService.send();
        if (!currentRtz.isFile() || !currentRtz.canRead()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Current RTZ profile is not available.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(currentRtz.getName()).build().toString())
                .body(new FileSystemResource(currentRtz));
    }
}
