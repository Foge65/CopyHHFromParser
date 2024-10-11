package team.firestorm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.firestorm.service.ScanFileService;

@RestController
@RequiredArgsConstructor
public class ScanFileController {
    private final ScanFileService service;

    @PostMapping("/scanFiles")
    public void scanFiles() {
        service.scanAllFiles();
    }

}