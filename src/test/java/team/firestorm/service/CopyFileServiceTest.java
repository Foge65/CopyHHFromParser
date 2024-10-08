package team.firestorm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.nio.file.Path;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CopyFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private CopyFileService service;

    @Value("${path.to.FSTracker}")
    private String pathSource = "C:\\FStracker";

    @Value("${path.to.processed.directory}")
    private String pathToProcessedDirectory = "C:\\Users\\user\\AppData\\Local\\PokerTracker 4\\Processed";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "pathToFSTracker", pathSource);
        ReflectionTestUtils.setField(service, "pathToProcessedDirectory", pathToProcessedDirectory);
    }

    @Test
    void copyByOneFile() {
        Path existingPath = Path.of(pathSource);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilePath(existingPath.toString());

        Mockito.when(repository.findFirstByUploadedFalse()).thenReturn(Optional.of(fileEntity));

        service.copyByOneFile();

        Mockito.verify(repository).updateUploadedByFilePath(fileEntity.getFilePath(), true);
    }
}