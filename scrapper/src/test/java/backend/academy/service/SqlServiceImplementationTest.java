package backend.academy.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.service.sql.SqlChatService;
import backend.academy.service.sql.SqlLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {"service.access-type=SQL"})
public class SqlServiceImplementationTest {
    @Autowired
    private LinkService linkService;

    @Autowired
    private ChatService chatService;

    @org.junit.jupiter.api.Test
    void testSqlLinkServiceIsAutowired() {
        assertThat(linkService).isNotNull();
        assertThat(linkService).isInstanceOf(SqlLinkService.class);
    }

    @Test
    void testSqlChatServiceIsAutowired() {
        assertThat(chatService).isNotNull();
        assertThat(chatService).isInstanceOf(SqlChatService.class);
    }
}
