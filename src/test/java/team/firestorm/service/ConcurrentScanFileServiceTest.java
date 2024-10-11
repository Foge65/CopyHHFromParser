package team.firestorm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.repository.FileRepository;

@ExtendWith(MockitoExtension.class)
class ConcurrentScanFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private ConcurrentScanFileService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "pathFSTracker", "C:\\FStracker");
    }

    @Test
    void scanFiles() {
        service.scanFiles();
    }

}