package backend.academy.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BotControllerDDoSTest {
    @Autowired
    private MockMvc mockMvc;

    @Value("${resilience4j.ratelimiter.instances.default.limitForPeriod}")
    private int rateLimit;

    @Test
    public void update_WhenLimitExceeded_ThenReturn429TooManyRequests() throws Exception {
        for (int i = 0; i < rateLimit; ++i) {
            mockMvc.perform(
                            post("/updates")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                        {
                            "id" : 1,
                            "url" : "test",
                            "description" : "test_descr",
                            "tgChatIds" : [
                                1,
                                4
                            ]
                        }"""))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(
                        post("/updates")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                    {
                        "id" : 1,
                        "url" : "test",
                        "description" : "test_descr",
                        "tgChatIds" : [
                            1,
                            4
                        ]
                    }"""))
                .andExpect(status().isTooManyRequests());
    }
}
