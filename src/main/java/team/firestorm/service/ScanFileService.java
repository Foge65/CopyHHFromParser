package team.firestorm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    private Path rootDirectory;

    @PostConstruct
    public void init() {
        rootDirectory = Path.of(pathFSTracker).resolve("SPIN");
    }

    @Scheduled(cron = "${scheduled.cron.scan}")
    public void scanAllFiles() {
        log.info("Starting scan all files");

        try (Stream<Path> stream = Files.walk(rootDirectory, 1)) {
            stream
                .filter(Files::isDirectory)
                .filter(path -> !path.equals(rootDirectory))
                .forEach(this::saveScannedEntities);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Finished scan all files");
    }

    @Async
    public void saveScannedEntities(Path directory) {
        try {
            List<Path> paths = collectNewPaths(directory);
            List<FileEntity> entities = collectFileEntities(paths);
            repository.saveAll(entities);
        } catch (Exception e) {
            log.error("Error in directory {}", directory, e);
        }
    }

    private List<Path> collectNewPaths(Path directory) {
        String relativePath = relativize(directory);
        List<String> filesFromRepository = repository.findAllByFilePathStartsWith(relativePath);
        List<Path> filesFromDisk = getFilesFromDisk(directory);
        List<Path> collectedPaths = new ArrayList<>();
        for (Path filePath : filesFromDisk) {
            if (Files.isRegularFile(filePath)) {
                String path = relativize(filePath);
                if (!filesFromRepository.contains(path)) {
                    collectedPaths.add(filePath);
                }
            }
        }
        return collectedPaths;
    }

    private List<FileEntity> collectFileEntities(List<Path> paths) {
        List<FileEntity> entities = new ArrayList<>();
        for (Path path : paths) {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFilePath(relativize(path));
            entities.add(fileEntity);
        }
        return entities;
    }

    private String relativize(Path path) {
        return "SPIN/" + rootDirectory.relativize(path).toString().replace("\\", "/");
    }

    private List<Path> getFilesFromDisk(Path directory) {
        try (Stream<Path> fileStream = Files.walk(directory)) {
            return fileStream
            .filter(Files::isRegularFile)
            .filter(ext -> ext.getFileName().toString().endsWith(".txt") ||
                ext.getFileName().toString().endsWith(".xml"))
            .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
