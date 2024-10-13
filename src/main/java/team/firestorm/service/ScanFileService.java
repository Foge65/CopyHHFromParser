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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @Transactional
    @Scheduled(cron = "0 0 */12 * * *")
    public void scanFiles() {
        try (Stream<Path> dirs = Files.walk(Path.of(pathFSTracker), 2)) {
            dirs.filter(Files::isDirectory)
                    .filter(dir -> !dir.equals(Path.of(pathFSTracker, "Spin")))
                    .forEach(this::createTask);
        } catch (IOException e) {
            log.error("Error scanning directories: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void createTask(Path directory) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> filterDirectory(directory));
        executorService.shutdown();
    }

    private void filterDirectory(Path directory) {
        try (Stream<Path> files = Files.walk(directory)) {
            log.info("Thread started for {}", directory);
            files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".txt") ||
                                    file.getFileName().toString().endsWith(".xml"))
                    .forEach(this::saveEntity);
            log.info("Thread stopped for {}", directory);
        } catch (IOException e) {
            log.error("Error processing directory {}: {}", directory, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void saveEntity(Path file) {
        String path = file.toString();
        if (repository.findByFilePath(path).isPresent()) {
            return;
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilePath(path);
        repository.save(fileEntity);
    }

}
