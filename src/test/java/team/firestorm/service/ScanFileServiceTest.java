package team.firestorm.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@ExtendWith(MockitoExtension.class)
class ScanFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private ScanFileService service;

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
    void scanDisk_correctFilters() throws IOException {
        Path fileTxt = Files.createFile(Path.of(pathFSTracker).resolve("file1.txt"));
        Path fileXml = Files.createFile(Path.of(pathFSTracker).resolve("file2.xml"));
        Path fileCsv = Files.createFile(Path.of(pathFSTracker).resolve("file3.csv"));
        Path fileJpg = Files.createFile(Path.of(pathFSTracker).resolve("file4.jpg"));
        Path fileTxt2 = Files.createFile(Path.of(pathFSTracker).resolve("file5.txt"));

        service.scanFiles();

        ArgumentCaptor<FileEntity> fileEntity = ArgumentCaptor.forClass(FileEntity.class);
        Mockito.verify(repository, Mockito.times(1)).save(fileEntity.capture());

        List<FileEntity> allEntity = fileEntity.getAllValues();

        Assertions.assertEquals(fileTxt.toString(), allEntity.get(0).getFilePath());
        Assertions.assertEquals(fileXml.toString(), allEntity.get(1).getFilePath());
        Assertions.assertEquals(fileTxt2.toString(), allEntity.get(2).getFilePath());


        Files.delete(fileTxt);
        Files.delete(fileXml);
        Files.delete(fileCsv);
        Files.delete(fileJpg);
        Files.delete(fileTxt2);
    }

    @Test
    void scanDisk_saveWithoutDuplicate() throws IOException {
        Path fileTxt = Files.createFile(Path.of(pathFSTracker).resolve("file1.txt"));

        FileEntity existingFileEntity = new FileEntity();
        existingFileEntity.setFilePath(fileTxt.toString());
        Mockito.when(repository.findByFilePath(fileTxt.toString())).thenReturn(Optional.of(existingFileEntity));

        service.scanFiles();

        Mockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any(FileEntity.class));

        Files.delete(fileTxt);
    }

    @Test
    @Disabled
    void scanDisk_threadCreation() throws IOException, InterruptedException {
        Path dir1 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir1"));
        Path dir2 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir2"));
        Path dir3 = Files.createDirectory(Path.of(pathFSTracker).resolve("dir3"));
        Files.createFile(dir1.resolve("file1.txt"));
        Files.createFile(dir2.resolve("file2.txt"));
        Files.createFile(dir3.resolve("file3.txt"));

        ExecutorService mockExecutorService = Mockito.spy(ExecutorService.class);

        CountDownLatch latch = new CountDownLatch(10);
        Mockito.doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            System.out.println("Starting task: " + task.hashCode());
            task.run();
            System.out.println("Finished task: " + task.hashCode());
            latch.countDown();
            return null;
        }).when(mockExecutorService).submit(ArgumentMatchers.any(Runnable.class));

        service.scanFiles();

        latch.await();

        Mockito.verify(mockExecutorService, Mockito.times(3)).submit(ArgumentMatchers.any(Runnable.class));

        Files.delete(dir1.resolve("file1.txt"));
        Files.delete(dir2.resolve("file2.txt"));
        Files.delete(dir3.resolve("file3.txt"));
        Files.delete(dir1);
        Files.delete(dir2);
        Files.delete(dir3);
    }

}