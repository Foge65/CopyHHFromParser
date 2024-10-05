package team.firestorm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.firestorm.service.ScanFileService;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final ScanFileService scanFileService;

    @PostMapping("/allFiles")
    public ResponseEntity<Void> scanAllFiles() {
        scanFileService.scanAllFiles();
        return ResponseEntity.ok().build();
    }
}
