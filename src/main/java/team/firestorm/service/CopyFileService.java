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
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopyFileService {
    private final FileRepository repository;

    @Value("${path.FSTracker}")
    private String pathFSTracker;

    @Value("${path.Dist}")
    private String pathDist;

    private final Set<Path> createdDirs = ConcurrentHashMap.newKeySet();

    @Transactional(readOnly = true)
    public List<FileEntity> findNotUploadedFiles() {
        return repository.findAllByUploadedFalse().orElse(Collections.emptyList());
    }

    @Transactional
    public void copyMissedFiles() {
        log.info("Copying missed files started");

        List<FileEntity> files = findNotUploadedFiles();
        if (files.isEmpty()) {
            log.info("No files to copy");
            return;
        }

        List<Long> copiedIds = files.parallelStream()
                .filter(file -> copyFileByPath(file.getFilePath()))
                .map(FileEntity::getId)
                .collect(java.util.stream.Collectors.toList());

        repository.updateStatusByIds(copiedIds);

        log.info("Copying missed files finished. Copied {} of {}", copiedIds.size(), files.size());
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

    private boolean copyFileByPath(String relativeFilePath) {
        Path sourcePath = Path.of(pathFSTracker).resolve(relativeFilePath);
        Path destinationPath = Path.of(pathDist).resolve(relativeFilePath);
        try {
            Path parentDir = destinationPath.getParent();
            if (createdDirs.add(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Error copying {}: {}", relativeFilePath, e.getMessage());
            return false;
        }
    }

}
