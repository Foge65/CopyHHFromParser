package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcurrentScanFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    //    @Scheduled(cron = "${scheduled.cron.scan}")
    @Transactional
    public void scanAllFiles() {
        log.info("Concurrency start scan all files");

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<?>> futures = new ArrayList<>();

        try (Stream<Path> directoryStream = Files.walk(Path.of(pathFSTracker).resolve("SPIN"), 1)) {
            directoryStream.filter(Files::isDirectory)
                    .filter(path -> !path.equals(Path.of(pathFSTracker).resolve("SPIN")))
                    .forEach(directory -> {
                        Future<?> future = executorService.submit(() -> createThreadForDirectory(directory));
                        futures.add(future);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
            try {
                for (Future<?> future : futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }

            log.info("Concurrency finished scan all files\n");
        }
    }

    private void createThreadForDirectory(Path directory) {
        log.info("Thread started for directory {}", directory);
        try (Stream<Path> fileStream = Files.walk(directory)) {
            fileStream.filter(Files::isRegularFile)
                    .filter(ext -> ext.getFileName().toString().endsWith(".txt") ||
                                   ext.getFileName().toString().endsWith(".xml"))
                    .forEach(this::saveEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Thread finished for directory {}", directory);
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
