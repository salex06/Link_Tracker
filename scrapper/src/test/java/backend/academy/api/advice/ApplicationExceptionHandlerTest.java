package backend.academy.api.advice;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.api.LinkController;
import backend.academy.clients.ClientManager;
import backend.academy.repository.orm.OrmChatLinkFiltersRepository;
import backend.academy.repository.orm.OrmChatLinkRepository;
import backend.academy.repository.orm.OrmChatLinkTagsRepository;
import backend.academy.repository.orm.OrmChatRepository;
import backend.academy.repository.orm.OrmLinkRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest
@Import(ApplicationExceptionHandler.class)
@MockitoBean(
        types = {
            OrmChatRepository.class,
            OrmChatLinkFiltersRepository.class,
            OrmChatLinkTagsRepository.class,
            OrmLinkRepository.class,
            OrmChatLinkRepository.class,
            ChatService.class,
            LinkService.class,
            ClientManager.class
        })
class ApplicationExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    LinkController linkController;

    @Configuration
    static class Config {
        @Bean
        @Primary
        public EntityManagerFactory factory() {
            return Mockito.mock(EntityManagerFactory.class);
        }
    }

    @Test
    public void handleHttpMessageNotReadableExceptionWorksCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/links")
                        .content("{\"invalid\": json}")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("HttpMessageNotReadableException"))
                .andExpect(jsonPath("$.exceptionMessage")
                        .value(
                                "JSON parse error: Unrecognized token 'json': was expecting (JSON String, Number, Array, "
                                        + "Object or token 'null', 'true' or 'false')"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @Test
    public void handleMethodArgumentTypeMismatchExceptionWorksCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/links")
                        .content(
                                """
                    {
                        "link" : 1,
                        "tags": [2],
                        "filters": [3,4,5]
                    }
                    """)
                        .header("Tg-Chat-Id", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentTypeMismatchException"))
                .andExpect(
                        jsonPath("$.exceptionMessage")
                                .value(
                                        "Method parameter 'Tg-Chat-Id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"abc\""))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @Test
    public void handleHttpRequestMethodNotSupportedExceptionWorksCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/links")
                        .content("")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value("405"))
                .andExpect(jsonPath("$.exceptionName").value("HttpRequestMethodNotSupportedException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Request method 'PUT' is not supported"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @Test
    public void handleNoResourceFoundExceptionWorksCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/linkss")
                        .content("")
                        .header("Tg-Chat-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.exceptionName").value("NoResourceFoundException"))
                .andExpect(jsonPath("$.exceptionMessage").value("No static resource linkss."))
                .andExpect(jsonPath("$.stacktrace").exists());
    }

    @Test
    public void handleMissingRequestHeaderExceptionWorksCorrectly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/links").content("").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.exceptionName").value("MissingRequestHeaderException"))
                .andExpect(jsonPath("$.exceptionMessage")
                        .value("Required request header 'Tg-Chat-Id' for method parameter type Long is not present"))
                .andExpect(jsonPath("$.stacktrace").exists());
    }
}
