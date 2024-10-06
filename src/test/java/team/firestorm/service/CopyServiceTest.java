package team.firestorm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.firestorm.repository.FileRepository;

@ExtendWith(MockitoExtension.class)
class CopyServiceTest {

    @Mock
    private FileRepository repository;

    @InjectMocks
    private CopyService service;

    @Test
    void upload() {

    }
}