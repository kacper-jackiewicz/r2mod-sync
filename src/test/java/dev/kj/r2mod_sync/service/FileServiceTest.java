package dev.kj.r2mod_sync.service;

import dev.kj.r2mod_sync.config.DirectoryProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void receiveUsesGeneratedCandidateNameInsideWorkspace() throws IOException {
        Path workspace = Files.createDirectory(temporaryDirectory.resolve("workspace"));
        FileService fileService = new FileService(new DirectoryProperties(workspace.toString()));
        byte[] contents = {1, 2, 3};
        MockMultipartFile upload = new MockMultipartFile(
                "file", "../../outside.r2z", "application/octet-stream", contents);

        Path candidate = fileService.receive(upload);

        assertEquals(candidate.getParent(), workspace.toAbsolutePath().normalize());
        assertTrue(candidate.getFileName().toString().startsWith(FileService.CANDIDATE_PREFIX));
        assertArrayEquals(contents, Files.readAllBytes(candidate));
    }

    @Test
    void receiveCreatesUniqueCandidatesAndDeleteRemovesOnlyRequestedCandidate() throws IOException {
        Path workspace = Files.createDirectory(temporaryDirectory.resolve("workspace"));
        FileService fileService = new FileService(new DirectoryProperties(workspace.toString()));
        MockMultipartFile upload = new MockMultipartFile("file", "profile.r2z", "application/octet-stream", new byte[]{1});

        Path first = fileService.receive(upload);
        Path second = fileService.receive(upload);

        assertNotEquals(first, second);
        fileService.deleteCandidate(first);

        assertTrue(Files.notExists(first));
        assertTrue(Files.exists(second));
    }

    @Test
    void receiveRejectsEmptyUpload() {
        FileService fileService = new FileService(new DirectoryProperties(temporaryDirectory.toString()));
        MockMultipartFile upload = new MockMultipartFile("file", "empty.r2z", "application/octet-stream", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> fileService.receive(upload));
    }
}
