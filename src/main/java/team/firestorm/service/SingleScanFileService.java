package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class SingleScanFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    public void scanAllFiles() {
        log.info("Single scanning all files started");
        File filePath = new File(pathFSTracker);
        try (Stream<Path> stream = Files.walk(filePath.toPath())) {
            stream.filter(Files::isRegularFile)
                    .filter(ext -> ext.getFileName().toString().endsWith(".txt") ||
                                   ext.getFileName().toString().endsWith(".xml"))
                    .forEach(file -> {
                        String path = file.toString();
                        if (repository.findByFilePath(path).isPresent()) {
                            return;
                        }
                        FileEntity fileEntity = new FileEntity();
                        fileEntity.setFilePath(path);
                        repository.save(fileEntity);
                    });
            log.info("Single scanning all files finished");
        } catch (IOException e) {
            log.error("Error scan directory {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
