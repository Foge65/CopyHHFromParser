package team.firestorm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.firestorm.service.ConcurrentScanFileService;

@RestController
@RequiredArgsConstructor
public class ScanFileController {
    private final ConcurrentScanFileService service;

    @PostMapping("/scanFiles")
    public void scanFiles() {
        service.scanAllFiles();
    }

}
