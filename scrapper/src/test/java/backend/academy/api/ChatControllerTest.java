package backend.academy.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Value("${resilience4j.ratelimiter.instances.default.limitForPeriod}")
    private int rateLimit;

    private ChatController chatController;

    @MockitoBean
    private ChatService chatService;

    @BeforeEach
    public void setup() {
        chatController = new ChatController(chatService);
    }

    @Test
    public void registerChat_WhenRequestIsCorrect_ThenReturnSuccessMessage() {
        String expectedMessage = "Вы зарегистрированы";
        when(chatService.saveChat(anyLong())).thenReturn(new TgChat(1L, 1L, null, new HashSet<>()));

        ResponseEntity<?> response = chatController.registerChat(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat((String) response.getBody()).isEqualTo(expectedMessage);
    }

    @Test
    public void registerChat_WhenRequestIsWrong_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.saveChat(anyLong())).thenReturn(null);

        ResponseEntity<?> response = chatController.registerChat(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void deleteChat_WhenRequestIsCorrect_ThenReturnSuccessMessage() {
        String expectedMessage = "Чат успешно удален";
        when(chatService.getPlainTgChatByChatId(anyLong()))
                .thenReturn(Optional.of(new TgChat(1L, 1L, null, new HashSet<>())));

        ResponseEntity<?> response = chatController.deleteChat(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(String.class);
        assertThat((String) response.getBody()).isEqualTo(expectedMessage);
    }

    @Test
    public void deleteChat_WhenRequestIsWrong_ThenReturnSuccessMessage() {
        String expectedMessage = "Чат не существует";
        when(chatService.getPlainTgChatByChatId(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = chatController.deleteChat(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void updateTimeConfiguration_WhenChatNotFound_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.getPlainTgChatByChatId(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = chatController.updateTimeConfiguration(1L, "10:30");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void updateTimeConfiguration_WhenTimeWasNotUpdated_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.getPlainTgChatByChatId(anyLong()))
                .thenReturn(Optional.of(new TgChat(1L, 1L, null, new HashSet<>())));
        when(chatService.updateTimeConfig(any(), any())).thenReturn(false);

        ResponseEntity<?> response = chatController.updateTimeConfiguration(1L, "wrong time");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void updateTimeConfiguration_WhenChatWasFoundAndConfigWasUpdated_ThenReturnOkMessage() {
        when(chatService.getPlainTgChatByChatId(anyLong()))
                .thenReturn(Optional.of(new TgChat(1L, 1L, null, new HashSet<>())));
        when(chatService.updateTimeConfig(any(), any())).thenReturn(true);

        ResponseEntity<?> response = chatController.updateTimeConfiguration(1L, "10:30");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void registerChat_WhenLimitExceeded_ThenReturn429TooManyRequests() throws Exception {
        when(chatService.saveChat(anyLong())).thenReturn(new TgChat(1L, 1L, null, new HashSet<>()));

        for (int i = 0; i < rateLimit; ++i) {
            mockMvc.perform(post("/tg-chat/0")).andExpect(status().isOk());
        }

        mockMvc.perform(post("/tg-chat/0")).andExpect(status().isTooManyRequests());
    }
}
