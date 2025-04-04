package backend.academy.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.service.orm.OrmChatService;
import backend.academy.service.orm.OrmLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {"service.access-type=ORM"})
public class OrmServiceImplementationTest {
    @Autowired
    private LinkService linkService;

    @Autowired
    private ChatService chatService;

    @Test
    void testOrmLinkServiceIsAutowired() {
        assertThat(linkService).isNotNull();
        assertThat(linkService).isInstanceOf(OrmLinkService.class);
    }

    @Test
    void testOrmChatServiceIsAutowired() {
        assertThat(chatService).isNotNull();
        assertThat(chatService).isInstanceOf(OrmChatService.class);
    }
}
