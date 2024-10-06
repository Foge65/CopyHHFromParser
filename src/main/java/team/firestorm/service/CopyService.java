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
import java.util.List;

@Service
@RequiredArgsConstructor
public class CopyService {
    private final FileRepository repository;

    @Value("${path.to.FSTracker}")
    private String pathToFSTracker;

    @Transactional
    @Scheduled(cron = "*/40 * * * * *")
    public void copyFileByPath() {
        List<FileEntity> notUploaded = repository.findByUploaded(false);
        for (FileEntity fileEntity : notUploaded) {
            copyFile(fileEntity);
            repository.updateUploadedByFilePath(fileEntity.getFilePath(), true);
        }
    }

    private void copyFile(FileEntity fileEntity) {
        String path = fileEntity.getFilePath();
        Path fullPath = Path.of(path);
        Path relativePath = Paths.get(pathToFSTracker).relativize(fullPath);
        Path destinationPath = Path.of("C:\\Users\\user\\AppData\\Local\\PokerTracker 4\\Processed\\"
                                       + relativePath);
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
