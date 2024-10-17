package team.firestorm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.firestorm.service.SingleScanFileService;

@RestController
@RequiredArgsConstructor
public class SingleScanFileController {
    private final SingleScanFileService service;

    @PostMapping("/singleScanFiles")
    public void scanFiles() {
        service.scanAllFiles();
    }

}
