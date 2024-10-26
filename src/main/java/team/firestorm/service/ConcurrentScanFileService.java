package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private final TransactionScanService transactionScanService;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @Scheduled(cron = "${scheduled.cron.scan}")
    public void scanAllFiles() {
        log.info("Concurrency start scan all files");

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();

        try (Stream<Path> directoryStream = Files.walk(Path.of(pathFSTracker).resolve("SPIN"), 1)) {
            directoryStream.filter(Files::isDirectory)
                    .filter(path -> !path.equals(Path.of(pathFSTracker).resolve("SPIN")))
                    .forEach(directory -> {
                        Future<?> future = executorService.submit(() ->
                                transactionScanService.createThreadForDirectory(directory));
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

            log.info("Concurrency finished scan all files");
        }
    }

}
