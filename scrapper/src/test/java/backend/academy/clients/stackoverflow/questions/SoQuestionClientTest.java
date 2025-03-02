package backend.academy.clients.stackoverflow.questions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.clients.stackoverflow.answers.SoAnswerClient;
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

class SoQuestionClientTest {
    private int port = 8090;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static SoQuestionClient soQuestionClient;

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
        soQuestionClient =
                new SoQuestionClient(x -> String.format("http://localhost:" + port + "/questions/6031003"), restClient);

        String expectedMessage =
                "Обновление в вопросе 'stackoverflow search api' по ссылке https://stackoverflow.com/questions/6031003";
        Link link = new Link(1L, "https://stackoverflow.com/questions/6031003");
        stubFor(
                get("/questions/6031003")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"items\":[{\"tags\":[\"c#\",\"json\",\"stackexchange-api\"],\"owner\":"
                                                        + "{\"account_id\":393732,\"reputation\":63,\"user_id\":755752,\"user_type\":\"registered\","
                                                        + "\"profile_image\":"
                                                        + "\"https://www.gravatar.com/avatar/ad9ef1d4d7c31b5827c94e64217594da?s=256&d=identicon&r=PG\","
                                                        + "\"display_name\":\"khuzbuzz\",\"link\":\"https://stackoverflow.com/users/755752/khuzbuzz\"},"
                                                        + "\"is_answered\":true,\"view_count\":3358,\"accepted_answer_id\":6031169,\"answer_count\":3,"
                                                        + "\"score\":6,\"last_activity_date\":"
                                                        + String.valueOf(
                                                                Instant.now().getEpochSecond() + 1000)
                                                        + ",\"creation_date\":1305636594,\"question_id\":6031003,"
                                                        + "\"content_license\":\"CC BY-SA 3.0\",\"link\":\"https://stackoverflow.com/questions/6031003/stackoverflow-search-api\","
                                                        + "\"title\":\"stackoverflow search api\"}],\"has_more\":false,\"quota_max\":300,\"quota_remaining\":215}")));

        List<String> updates = soQuestionClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.getFirst()).isEqualTo(expectedMessage);
    }

    @Test
    void getUpdates_WhenRequestError_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        soQuestionClient =
            new SoQuestionClient(x -> String.format("http://localhost:" + port + "/questions/79461427"), restClient);

        Link link = new Link(1L, "https://stackoverflow.com/questions/79461427");
        stubFor(get("/questions/79461427")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"items\":[],\"has_more\":false,\"quota_max\":300,\"quota_remaining\":290}")));

        List<String> updates = soQuestionClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
