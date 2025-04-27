package backend.academy.stability;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.config.properties.ApplicationStabilityProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@SpringBootTest
public class RestClientTest {
    private int port;

    @Autowired
    @Qualifier("botConnectionClient")
    private RestClient restClient;

    @Autowired
    private ApplicationStabilityProperties properties;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setupBeforeEach() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        port = wireMockServer.port();
        WireMock.configureFor("localhost", port);
    }

    @AfterEach
    public void shutdown() {
        wireMockServer.stop();
    }

    @Test
    void testRestClientConnectTimeout() {
        String url = "http://192.0.2.0:8080/api/data";
        long expectedTimeoutMillis = properties.getConnectTimeout();
        long toleranceMillis = 200;

        long startTime = System.currentTimeMillis();

        ResourceAccessException exception = assertThrows(ResourceAccessException.class, () -> {
            restClient.get().uri(url).retrieve().body(String.class);
        });

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        assertTrue(elapsedTime >= expectedTimeoutMillis - toleranceMillis
                && elapsedTime <= expectedTimeoutMillis + toleranceMillis);
    }

    @Test
    void testRestClientReadTimeout() {
        stubFor(get(urlEqualTo("/api/data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello, world!")
                        .withFixedDelay(properties.getReadTimeout())));

        String url = "http://localhost:" + wireMockServer.port() + "/api/data";

        assertThrows(ResourceAccessException.class, () -> {
            restClient.get().uri(url).retrieve().body(String.class);
        });
    }
}
