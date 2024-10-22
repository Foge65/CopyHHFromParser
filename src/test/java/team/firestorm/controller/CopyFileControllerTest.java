package team.firestorm.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import team.firestorm.service.CopyFileService;


@WebMvcTest(CopyFileController.class)
class CopyFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CopyFileService service;

    @Test
    void copyNFiles() throws Exception {
        int count = 3;

        mockMvc.perform(MockMvcRequestBuilders.post("/copyNFiles")
                        .param("count", String.valueOf(count)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(service, Mockito.times(1)).copyNFiles(count);
    }

}