package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ScanFileService {
    private final FileRepository fileRepository;

    @Value("${path.to.FSTracker}")
    private String pathToFSTracker;

    @Scheduled(cron = "*/30 * * * * *")
    public void scanAllFiles() {
        File filePath = new File(pathToFSTracker);
        try (Stream<Path> stream = Files.walk(filePath.toPath())) {
            stream.filter(Files::isRegularFile)
                    .forEach(file -> {
                        String path = file.toString();
                        if (fileRepository.findByFilePath(path).isPresent()) {
                            return;
                        }

                        FileEntity fileEntity = new FileEntity();
                        fileEntity.setId(UUID.randomUUID());
                        fileEntity.setFilePath(path);
                        fileRepository.save(fileEntity);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
