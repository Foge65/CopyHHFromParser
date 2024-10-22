package team.firestorm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.firestorm.service.CopyFileService;

@RestController
@RequiredArgsConstructor
public class CopyFileController {
    private final CopyFileService service;

    @PostMapping("/copyNFiles")
    public void copyNFiles(@RequestParam int count) {
        service.copyNFiles(count);
    }

}
