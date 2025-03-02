package backend.academy.clients.stackoverflow.answers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.model.Link;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class SoAnswerClientTest {
    private int port = 8090;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static SoAnswerClient soAnswerClient;

    @BeforeEach
    public void setupBeforeEach() {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
    }

    @AfterEach
    public void shutdown() {
        wireMockServer.stop();
    }

    @BeforeAll
    public static void setUp() {
        restClient = RestClient.create();
    }

    @Test
    void getUpdates_WhenQuestionWasUpdated_ThenReturnUpdateMessage() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        soAnswerClient =
                new SoAnswerClient(x -> String.format("http://localhost:" + port + "/answers/79461427"), restClient);

        String expectedMessage =
                "Обновление ответа пользователя 0___________ по ссылке https://stackoverflow.com/answers/79461427";
        Link link = new Link(1L, "https://stackoverflow.com/answers/79461427");
        stubFor(get("/answers/79461427")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"items\":[{\"owner\":{\"account_id\":7195741,\"reputation\":67749,"
                                + "\"user_id\":6110094,\"user_type\":\"registered\",\"accept_rate\":83,\"profile_image\":"
                                + "\"https://i.sstatic.net/epYPz.png?s=256\",\"display_name\":\"0___________\",\"link\":"
                                + "\"https://stackoverflow.com/users/6110094/0\"},\"is_accepted\":false,\"score\":1,"
                                + "\"last_activity_date\":1740324079,\"last_edit_date\":"
                                + Instant.now().getEpochSecond() + 1000 + ",\"creation_date\":1740322674,"
                                + "\"answer_id\":79461427,\"question_id\":79461387,"
                                + "\"content_license\":\"CC BY-SA 4.0\"}],"
                                + "\"has_more\":false,\"quota_max\":300,\"quota_remaining\":262}")));

        List<String> updates = soAnswerClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.getFirst()).isEqualTo(expectedMessage);
    }

    @Test
    void getUpdates_WhenRequestError_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        soAnswerClient =
                new SoAnswerClient(x -> String.format("http://localhost:" + port + "/answers/79461427"), restClient);

        Link link = new Link(1L, "https://stackoverflow.com/answers/79461427");
        stubFor(get("/answers/79461427")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"items\":[],\"has_more\":false,\"quota_max\":300,\"quota_remaining\":290}")));

        List<String> updates = soAnswerClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
