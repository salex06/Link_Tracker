package backend.academy.service;

import backend.academy.clients.Client;
import backend.academy.clients.github.issues.GitHubComment;
import backend.academy.clients.github.repository.GitHubRepositoryClient;
import backend.academy.clients.stackoverflow.questions.SoQuestionClient;
import backend.academy.service.sql.SqlLinkService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LinkServiceTest {
    private static Client client;
    private static Client client2;
    private static LinkService linkService;

    @BeforeAll
    static void setUp(){
        client = Mockito.mock(GitHubRepositoryClient.class);
        client2 = Mockito.mock(SoQuestionClient.class);
        linkService = Mockito.mock(SqlLinkService.class);
    }

    @Test
    public void validateLink_WhenSuitableClientExists_ThenReturnTrue(){
        String link = "abc";
        when(client.supportLink(link)).thenReturn(true);
        when(client2.supportLink(link)).thenReturn(false);
        when(linkService.validateLink(anyList(), anyString())).thenCallRealMethod();

        boolean result = linkService.validateLink(List.of(client, client2), link);

        assertThat(result).isTrue();
    }

    @Test
    public void validateLink_WhenSuitableClientDoNotExist_ThenReturnFalse(){
        String link = "abc";
        when(client.supportLink(link)).thenReturn(false);
        when(client2.supportLink(link)).thenReturn(false);
        when(linkService.validateLink(anyList(), anyString())).thenCallRealMethod();

        boolean result = linkService.validateLink(List.of(client, client2), link);

        assertThat(result).isFalse();
    }
}
