package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

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
public class ScanFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @Scheduled(cron = "${scheduled.cron.scan}")
    public void scanAllFiles() {
        log.info("Start scan all files");

        ExecutorService executorService = Executors.newFixedThreadPool(12);
        List<Future<?>> futures = new ArrayList<>();

        try (Stream<Path> directoryStream = Files.walk(Path.of(pathFSTracker).resolve("SPIN"), 1)) {
            directoryStream.filter(Files::isDirectory)
                    .filter(path -> !path.equals(Path.of(pathFSTracker).resolve("SPIN")))
                    .forEach(directory -> {
                        Future<?> future = executorService
                                .submit(() -> createThreadForDirectory(directory));
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

            log.info("Finished scan all files");
        }
    }

    @Transactional
    private void createThreadForDirectory(Path directory) {
        List<String> filesFromRepository = repository.findAllByFilePathStartsWith(String.valueOf(directory));

        List<Path> filesFromDisk = getFilesFromDisk(directory);

        filesFromDisk.stream()
                .filter(Files::isRegularFile)
                .filter(file -> !filesFromRepository.contains(String.valueOf(file)))
                .forEach(this::saveEntity);
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

    private void saveEntity(Path file) {
        String path = file.toString();
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFilePath(path);
        repository.save(fileEntity);
    }

}
