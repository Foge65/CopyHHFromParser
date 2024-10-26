package team.firestorm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

class TransactionScanServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private TransactionScanService service;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void createThreadForDirectory() throws IOException {
        Path root = Files.createDirectory(Path.of(pathFSTracker).resolve("SPIN"));

        Path player1 = Files.createDirectory(root.resolve("player1"));
        Path file1 = Files.createFile(player1.resolve("file1.txt"));

        Path player2 = Files.createDirectory(root.resolve("player2"));
        Path file2 = Files.createFile(player2.resolve("file2.xml"));

        Path player2SubDir = Files.createDirectory(player2.resolve("2020"));
        Path file3 = Files.createFile(player2SubDir.resolve("file3.xml"));

        Mockito.when(repository.findAllByFilePathStartsWith(String.valueOf(player2)))
                .thenReturn(Collections.emptyList());
        Mockito.when(repository.findAllByFilePathStartsWith(String.valueOf(player2SubDir)))
                .thenReturn(Collections.emptyList());

        service.scanAllFiles();

        Mockito.verify(repository, Mockito.never())
                .save(Mockito.argThat(entity -> entity.getFilePath().equals(file1.toString())));
        Mockito.verify(repository, Mockito.times(1))
                .save(Mockito.argThat(entity -> entity.getFilePath().equals(file2.toString())));
        Mockito.verify(repository, Mockito.times(1))
                .save(Mockito.argThat(entity -> entity.getFilePath().equals(file3.toString())));
    }

}