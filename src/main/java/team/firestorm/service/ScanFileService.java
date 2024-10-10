package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanFileService {
    private final FileRepository fileRepository;

    @Value("${path.to.FSTracker}")
    private String pathToFSTracker;

    @Scheduled(cron = "* */10 * * * *")
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
                        fileEntity.setFilePath(path);
                        fileRepository.save(fileEntity);
                    });
        } catch (IOException e) {
            log.error("Error scan directory {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
