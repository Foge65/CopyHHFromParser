package team.firestorm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void scanAllFiles_ShouldSkipExistingFilePaths() {
        Path mockedFile1 = mock(Path.class);
        Path mockedFile2 = mock(Path.class);

        when(mockedFile1.toString()).thenReturn("mocked/path/file1.txt");
        when(mockedFile2.toString()).thenReturn("mocked/path/file2.txt");

        FileEntity existingFileEntity = new FileEntity();
        existingFileEntity.setId(UUID.randomUUID());
        existingFileEntity.setFilePath(mockedFile1.toString());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            Stream<Path> mockStream = Stream.of(mockedFile1, mockedFile2);
            mockedFiles.when(() -> Files.walk(any(Path.class))).thenReturn(mockStream);

            mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);

            when(repository.findByFilePath(mockedFile1.toString())).thenReturn(Optional.of(existingFileEntity));

            service.scanAllFiles();

            ArgumentCaptor<FileEntity> fileEntityCaptor = ArgumentCaptor.forClass(FileEntity.class);
            verify(repository, times(1)).save(fileEntityCaptor.capture());

            assertEquals(1, fileEntityCaptor.getAllValues().size());
            assertEquals(mockedFile2.toString(), fileEntityCaptor.getValue().getFilePath());
        }
    }
}