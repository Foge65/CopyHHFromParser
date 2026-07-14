package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.firestorm.repository.FileRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStatusService {
    private final FileRepository repository;

    @Transactional
    public void updateUploadedStatus(List<Long> ids) {
        repository.updateStatusByIds(ids);
    }
}
