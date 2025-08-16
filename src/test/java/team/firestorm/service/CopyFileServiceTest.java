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
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(Path.of(pathFSTracker));

        Files.walkFileTree(Path.of(pathDist), new SimpleFileVisitor<>() {
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
        fileEntity1.setId(1L);
        fileEntity1.setFilePath(Path.of(pathFSTracker).relativize(file1Txt).toString());
        fileEntity1.setUploaded(false);

        Mockito.when(repository.findFirstByUploadedFalse()).thenReturn(Optional.of(fileEntity1));

        service.copyByOneFile();

        Mockito.verify(repository, Mockito.times(1)).findFirstByUploadedFalse();

        List<Path> trackerFrom = listFiles(Path.of(pathFSTracker));
        List<Path> trackerTo = listFiles(Path.of(pathDist));

        Assertions.assertEquals(trackerFrom.size(), trackerTo.size());

        Files.delete(file1Txt);
        Files.delete(dir1);
    }

    @Test
    void copyNFiles() throws IOException {
        Path dir1 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir1"));
        Path file1 = Files.createFile(dir1.resolve("file1.txt"));
        Path file2 = Files.createFile(dir1.resolve("file2.txt"));
        Path file3 = Files.createFile(dir1.resolve("file3.txt"));

        FileEntity fileEntity1 = new FileEntity();
        fileEntity1.setId(1L);
        fileEntity1.setFilePath(Path.of(pathFSTracker).relativize(file1).toString());
        fileEntity1.setUploaded(false);

        FileEntity fileEntity2 = new FileEntity();
        fileEntity2.setId(2L);
        fileEntity2.setFilePath(Path.of(pathFSTracker).relativize(file2).toString());
        fileEntity2.setUploaded(false);

        FileEntity fileEntity3 = new FileEntity();
        fileEntity3.setId(3L);
        fileEntity3.setFilePath(Path.of(pathFSTracker).relativize(file3).toString());
        fileEntity3.setUploaded(false);

        Mockito.when(repository.findFirstByUploadedFalse()).thenReturn(Optional.of(fileEntity1))
                .thenReturn(Optional.of(fileEntity2));

        service.copyNFiles(2);

        List<Path> trackerTo = listFiles(Path.of(pathDist));

        Assertions.assertEquals(4, trackerTo.size());

        Files.delete(file3);
        Files.delete(file2);
        Files.delete(file1);
        Files.delete(dir1);
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
