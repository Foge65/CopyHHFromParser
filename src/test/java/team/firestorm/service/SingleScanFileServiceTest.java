package team.firestorm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import team.firestorm.repository.FileRepository;

class SingleScanFileServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private SingleScanFileService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "pathFSTracker", "C:\\FStracker");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void scanAllFiles() {

    }

}