package team.firestorm.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class ScanFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private ScanFileService service;

    @BeforeEach
    void setUp() {
        String mockPath = "mocked/path";
        ReflectionTestUtils.setField(service, "pathToFSTracker", mockPath);
    }

    @Test
    void scanAllFiles() {
        Path existingFile = Mockito.mock(Path.class);
        Path newFile = Mockito.mock(Path.class);

        FileEntity existingFileEntity = new FileEntity();
        existingFileEntity.setFilePath(existingFile.toString());

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            Stream<Path> mockStream = Stream.of(existingFile, newFile);
            mockedFiles.when(() -> Files.walk(ArgumentMatchers.any(Path.class))).thenReturn(mockStream);
            mockedFiles.when(() -> Files.isRegularFile(ArgumentMatchers.any(Path.class))).thenReturn(true);

            Mockito.when(repository.findByFilePath(existingFile.toString())).thenReturn(Optional.of(existingFileEntity));

            service.scanAllFiles();

            ArgumentCaptor<FileEntity> fileEntityCaptor = ArgumentCaptor.forClass(FileEntity.class);
            Mockito.verify(repository, Mockito.times(1)).save(fileEntityCaptor.capture());

            Assertions.assertEquals(1, fileEntityCaptor.getAllValues().size());
            Assertions.assertEquals(newFile.toString(), fileEntityCaptor.getValue().getFilePath());
        }
    }
}