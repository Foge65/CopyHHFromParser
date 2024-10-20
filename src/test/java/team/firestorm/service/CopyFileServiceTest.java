package team.firestorm.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CopyFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private CopyFileService service;

    private String pathFSTracker;

    private String pathDist;

    @BeforeEach
    void setUp() throws IOException {
        pathFSTracker = String.valueOf(Files.createTempDirectory("trackerFrom"));
        pathDist = String.valueOf(Files.createTempDirectory("trackerTo"));

        ReflectionTestUtils.setField(service, "pathFSTracker", pathFSTracker);
        ReflectionTestUtils.setField(service, "pathDist", pathDist);
    }

    @Test
    void fieldPath_returnSetValue() {
        Object trackerFieldTracker = ReflectionTestUtils.getField(service, "pathFSTracker");
        assertEquals(pathFSTracker, trackerFieldTracker);

        Object trackerFieldDist = ReflectionTestUtils.getField(service, "pathDist");
        assertEquals(pathDist, trackerFieldDist);
    }

    @Test
    void copyByOneFile() throws IOException {
        Path dir1 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir1"));
        Path file1Txt = Files.createFile(dir1.resolve("file1.txt"));

        FileEntity fileEntity1 = new FileEntity();
        fileEntity1.setUuid(UUID.randomUUID());
        fileEntity1.setFilePath(String.valueOf(file1Txt));
        fileEntity1.setUploaded(false);

        Mockito.when(repository.findFirstByUploadedFalse()).thenReturn(Optional.of(fileEntity1));

        service.copyByOneFile();

        Mockito.verify(repository, Mockito.times(1)).findFirstByUploadedFalse();

        int trackerSize = listFiles(Path.of(pathFSTracker)).size();
        int distSize = listFiles(Path.of(pathDist)).size();

        Assertions.assertEquals(trackerSize, distSize);
    }

    private List<Path> listFiles(Path path) {
        List<Path> listFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(path)) {
            stream.forEach(listFiles::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listFiles;
    }

}