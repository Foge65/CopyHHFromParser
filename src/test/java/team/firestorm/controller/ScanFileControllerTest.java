package team.firestorm.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import team.firestorm.service.ConcurrentScanFileService;

@WebMvcTest(ScanFileController.class)
class ScanFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConcurrentScanFileService service;

    @Test
    void scanFiles() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/scanFiles"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(service, Mockito.times(1)).scanAllFiles();
    }

}