package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopyFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @Value("${path.Dist}")
    private String pathDist;

    @Transactional
    @Scheduled(cron = "${scheduled.cron.copy}")
    public void copyByOneFile() {
        Optional<FileEntity> fileEntity = repository.findFirstByUploadedFalse();
        if (fileEntity.isPresent()) {
            copyFileByPath(Path.of(fileEntity.get().getFilePath()));
            repository.updateUploadedByFilePath(fileEntity.get().getFilePath(), true);
        }
    }

    @Transactional
    public void copyNFiles(int count) {
        log.info("Copying {} files started", count);

        for (int i = 1; i <= count; i++) {
            Optional<FileEntity> fileEntity = repository.findFirstByUploadedFalse();
            if (fileEntity.isPresent()) {
                copyFileByPath(Path.of(fileEntity.get().getFilePath()));
                repository.updateUploadedByFilePath(fileEntity.get().getFilePath(), true);
            }
        }

        log.info("Copying {} files finished", count);
    }

    private void copyFileByPath(Path filePath) {
        Path relativePath = Paths.get(pathFSTracker).relativize(filePath);
        Path destinationPath = Path.of(pathDist + "/" + relativePath);
        try {
            Files.createDirectories(destinationPath.getParent());
            if (!Files.exists(destinationPath)) {
                Files.copy(filePath, destinationPath);
            }
        } catch (IOException e) {
            log.error("Error copying {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void copyByDateStartWith(String date) {
        List<String> filePathByMonth = repository.findFilePathByDateStartWith(date);
        for (String path : filePathByMonth) {
            copyFileByPath(Path.of(path));
        }
    }

}
