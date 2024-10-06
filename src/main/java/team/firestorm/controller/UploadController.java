package team.firestorm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import team.firestorm.service.CopyService;

@RestController
@RequiredArgsConstructor
public class UploadController {
    private final CopyService service;

    @PostMapping("/uploadFile")
    public ResponseEntity<Void> uploadFileByPath() {
        service.copyFileByPath();
        return ResponseEntity.ok().build();
    }
}
