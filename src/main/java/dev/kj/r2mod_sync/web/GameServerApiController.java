package dev.kj.r2mod_sync.web;

import dev.kj.r2mod_sync.aop.CheckLock;
import dev.kj.r2mod_sync.config.GameServerProperties;
import dev.kj.r2mod_sync.model.ServerStatus;
import dev.kj.r2mod_sync.model.server.GameServer;
import dev.kj.r2mod_sync.service.FileService;
import dev.kj.r2mod_sync.service.ServerProfileOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/server")
@RequiredArgsConstructor
public class GameServerApiController {

    private final GameServer gameServerControlService;
    private final GameServerProperties gameServerProperties;
    private final FileService fileService;
    private final ServerProfileOperationService profileOperationService;

    @GetMapping("/health")
    public ServerStatus health() {
        return gameServerControlService.getStatus();
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("processName", gameServerProperties.processName());
    }

    @PostMapping("/start")
    @CheckLock
    public void start() {
        gameServerControlService.start();
    }

    @PostMapping("/stop")
    @CheckLock
    public void stop() {
        gameServerControlService.stop();
    }

    @PostMapping("/restart")
    @CheckLock
    public void restart() {
        gameServerControlService.restart();
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadRtz(@RequestParam("file") MultipartFile file) {
        try {
            UUID operationId = profileOperationService.submitModLoad(() -> fileService.receive(file), "upload");
            return ResponseEntity.accepted().body(Map.of("operationId", operationId.toString()));
        } catch (UncheckedIOException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("messageKey", "upload.error"));
        }
    }

    @PostMapping("/revert")
    public ResponseEntity<Map<String, String>> revert() {
        UUID operationId = profileOperationService.submitModLoad(fileService::getVanilla, "restore");
        return ResponseEntity.accepted().body(Map.of("operationId", operationId.toString()));
    }

    @GetMapping("/operation/{operationId}")
    public ServerProfileOperationService.OperationStatus operationStatus(@PathVariable UUID operationId) {
        ServerProfileOperationService.OperationStatus status = profileOperationService.getStatus(operationId);
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Server operation was not found.");
        }
        return status;
    }

    @GetMapping("/download")
    @CheckLock
    public ResponseEntity<Resource> downloadRtz() {
        File currentRtz = fileService.send();
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
