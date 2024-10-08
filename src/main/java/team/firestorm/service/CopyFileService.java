package team.firestorm.service;

import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CopyFileService {
    private final FileRepository repository;

    @Value("${path.to.FSTracker}")
    private String pathToFSTracker;

    @Value("${path.to.processed.directory}")
    private String pathToProcessedDirectory;

    @Transactional
    @Scheduled(cron = "*/30 * * * * *")
    public void copyByOneFile() {
        Optional<FileEntity> fileEntity = repository.findFirstByUploadedFalse();
        if (fileEntity.isPresent()) {
            copyFile(fileEntity.get());
            repository.updateUploadedByFilePath(fileEntity.get().getFilePath(), true);
        }
    }

    private void copyFile(FileEntity fileEntity) {
        String path = fileEntity.getFilePath();
        Path fullPath = Path.of(path);
        Path relativePath = Paths.get(pathToFSTracker).relativize(fullPath);
        Path destinationPath = Path.of(pathToProcessedDirectory + relativePath);
        try {
            Files.createDirectories(destinationPath.getParent());
            if (!Files.exists(destinationPath)) {
                Files.copy(fullPath, destinationPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
