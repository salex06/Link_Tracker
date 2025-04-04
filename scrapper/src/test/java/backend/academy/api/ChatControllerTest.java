package backend.academy.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ChatControllerTest {
    private ChatController chatController;
    private ChatService chatService;

    @BeforeEach
    public void setup() {
        chatService = Mockito.mock(ChatService.class);
        chatController = new ChatController(chatService);
    }

    @Test
    public void registerChat_WhenRequestIsCorrect_ThenReturnSuccessMessage() {
        String expectedMessage = "Вы зарегистрированы";
        when(chatService.saveChat(anyLong())).thenReturn(new TgChat(1L, 1L, new HashSet<>()));

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
                .thenReturn(Optional.of(new TgChat(1L, 1L, new HashSet<>())));

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
}
