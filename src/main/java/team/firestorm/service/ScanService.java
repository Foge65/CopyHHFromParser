package team.firestorm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.firestorm.entity.FileEntity;
import team.firestorm.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ScanService {
    private final FileRepository repository;

    @Transactional
    public void createThreadForDirectory(Path directory) {
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
