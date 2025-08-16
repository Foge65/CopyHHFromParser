package team.firestorm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ScanFileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private ScanFileService service;

    private String pathFSTracker;

    @BeforeEach
    void setUp() throws IOException {
        pathFSTracker = String.valueOf(Files.createTempDirectory("trackerFrom"));

        ReflectionTestUtils.setField(service, "pathFSTracker", pathFSTracker);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walkFileTree(Path.of(pathFSTracker), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    void fieldPath_returnSetValue() {
        Object trackerField = ReflectionTestUtils.getField(service, "pathFSTracker");
        Assertions.assertEquals(pathFSTracker, trackerField);
    }

    @Test
    void scanAllFiles_savesRelativePaths() throws Exception {
        Path root = Files.createDirectory(Path.of(pathFSTracker).resolve("SPIN"));

        Path player1 = Files.createDirectory(root.resolve("player1"));
        Files.createFile(player1.resolve("file1.txt"));

        Path player2 = Files.createDirectory(root.resolve("player2"));
        Files.createFile(player2.resolve("file2.xml"));

        Path player2SubDir = Files.createDirectory(player2.resolve("2020"));
        Files.createFile(player2SubDir.resolve("file3.xml"));

        Mockito.lenient().when(fileRepository.findAllByFilePathStartsWith(Mockito.anyString()))
                .thenReturn(List.of());

        service.scanAllFiles();

        Mockito.verify(fileRepository)
                .save(Mockito.argThat(entity -> entity.getFilePath().equals("SPIN/player1/file1.txt")));
        Mockito.verify(fileRepository)
                .save(Mockito.argThat(entity -> entity.getFilePath().equals("SPIN/player2/file2.xml")));
        Mockito.verify(fileRepository)
                .save(Mockito.argThat(entity -> entity.getFilePath().equals("SPIN/player2/2020/file3.xml")));
    }

}
