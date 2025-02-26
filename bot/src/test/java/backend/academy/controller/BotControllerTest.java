package backend.academy.controller;

import backend.academy.api.BotController;
import backend.academy.bot.Bot;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class BotControllerTest {
    @Mock
    private Bot bot;

    @Mock
    private LinkUpdate linkUpdate;

    @InjectMocks
    private BotController botController;

    @Test
    public void updates_WhenLinkUpdateDTOIsValid_thenReturnOK() {
        mockValidLinkUpdate();

        ResponseEntity<?> response = botController.update(linkUpdate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).isInstanceOf(String.class);
    }

    private void mockValidLinkUpdate() {
        Mockito.when(linkUpdate.id()).thenReturn(1L);
        Mockito.when(linkUpdate.url()).thenReturn("url");
        Mockito.when(linkUpdate.tgChatIds()).thenReturn(new ArrayList<>(List.of(1L, 2L, 3L)));
        Mockito.when(linkUpdate.description()).thenReturn("description");
    }

    @Test
    public void updates_WhenLinkUpdateDTOIsValid_thenThrowsApiErrorException() {
        assertThrows(ApiErrorException.class, () -> botController.update(linkUpdate));
    }
}
