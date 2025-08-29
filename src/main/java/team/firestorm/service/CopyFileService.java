package team.firestorm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopyFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @Value("${path.Dist}")
    private String pathDist;

    @Transactional
    public void copyMissedFiles() {
        log.info("Copying missed files started");

        List<FileEntity> files = new ArrayList<>();
        Optional<List<FileEntity>> allNotUploaded = repository.findAllByUploadedFalse();
        if (allNotUploaded.isPresent()) {
            files = allNotUploaded.get();
            for (FileEntity file : files) {
                copyFileByPath(file.getFilePath());
            }
        }
        repository.updateStatus(files);

        log.info("Copying missed files finished");
    }

    @Transactional
    public void copyByOneFile() {
        Optional<FileEntity> fileEntity = repository.findFirstByUploadedFalse();
        if (fileEntity.isPresent()) {
            copyFileByPath(fileEntity.get().getFilePath());
            repository.updateUploadedByFilePath(fileEntity.get().getFilePath(), true);
        }
    }

    @Transactional
    public void copyNFiles(int count) {
        log.info("Copying {} files started", count);

        for (int i = 1; i <= count; i++) {
            Optional<FileEntity> fileEntity = repository.findFirstByUploadedFalse();
            if (fileEntity.isPresent()) {
                copyFileByPath(fileEntity.get().getFilePath());
                repository.updateUploadedByFilePath(fileEntity.get().getFilePath(), true);
            }
        }

        log.info("Copying {} files finished", count);
    }

    @Transactional
    public void copyByDateStartWith(String date) {
        List<String> filePathByMonth = repository.findFilePathByDateStartWith(date);
        for (String path : filePathByMonth) {
            copyFileByPath(path);
        }
    }

    private void copyFileByPath(String relativeFilePath) {
        Path sourcePath = Path.of(pathFSTracker).resolve(relativeFilePath);
        Path destinationPath = Path.of(pathDist).resolve(relativeFilePath);
        try {
            Files.createDirectories(destinationPath.getParent());
            if (!Files.exists(destinationPath)) {
                Files.copy(sourcePath, destinationPath);
            }
        } catch (IOException e) {
            log.error("Error copying {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
