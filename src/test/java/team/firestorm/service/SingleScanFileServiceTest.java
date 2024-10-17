package team.firestorm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class SingleScanFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private SingleScanFileService service;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @BeforeEach
    void setUp() {
        try {
            pathFSTracker = String.valueOf(Files.createTempDirectory("scanDirTest"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ReflectionTestUtils.setField(service, "pathFSTracker", pathFSTracker);
    }

    @AfterEach
    void tearDown() {
        try {
            Files.delete(Path.of(pathFSTracker));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void fieldPath_returnSetValue() {
        Object trackerField = ReflectionTestUtils.getField(service, "pathFSTracker");
        Assertions.assertEquals(pathFSTracker, trackerField);
    }

    @Test
    void scanAllFiles() throws IOException {
        Path dir1 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir1"));
        Path dir2 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir2"));
        Path file1 = Files.createFile(Path.of(pathFSTracker).resolve("file1.txt"));
        Path file2 = Files.createFile(Path.of(pathFSTracker).resolve("file2.csv"));
        Path file3 = Files.createFile(Path.of(pathFSTracker).resolve("file3.xml"));

        service.scanAllFiles();

        ArgumentCaptor<FileEntity> fileEntity = ArgumentCaptor.forClass(FileEntity.class);
        Mockito.verify(repository, Mockito.times(2)).save(fileEntity.capture());

        repository.findAll().forEach(System.out::println);

        Files.delete(file1);
        Files.delete(file2);
        Files.delete(file3);
        Files.delete(dir1);
        Files.delete(dir2);
    }

}