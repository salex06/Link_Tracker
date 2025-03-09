package backend.academy.clients.github.issues;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.Link;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class GitHubIssueListClientTest {
    private int port = 8090;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static GitHubIssueListClient gitHubIssueListClient;

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
    void getUpdates_WhenNewComment_ThenReturnUpdateMessage() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubIssueListClient = new GitHubIssueListClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues"), restClient);

        String expectedMessage1 =
                String.format("Новый комментарий к issue README file modified %nАвтор: mattstifanelli%n"
                        + "Время создания: +999999999-12-31T23:59:59%n"
                        + "Превью: Let's try again via Issue tacker...%n");
        String expectedMessage2 =
                String.format("Новый комментарий к issue Edited README via GitHub%n" + "Автор: masonzou%n"
                        + "Время создания: +999999999-12-31T23:59:59%n"
                        + "Превью: test%n");
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues");
        stubFor(get("/octocat/Hello-World/issues")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\n" + "    {\n"
                                + "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825\",\n"
                                + "        \"html_url\": \"https://github.com/octocat/Hello-World/pull/2#issuecomment-1146825\",\n"
                                + "        \"issue_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2\",\n"
                                + "        \"id\": 1146825,\n"
                                + "        \"node_id\": \"MDEyOklzc3VlQ29tbWVudDExNDY4MjU=\",\n"
                                + "        \"user\": {\n"
                                + "            \"login\": \"mattstifanelli\",\n"
                                + "            \"id\": 783382,\n"
                                + "            \"node_id\": \"MDQ6VXNlcjc4MzM4Mg==\",\n"
                                + "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/783382?v=4\",\n"
                                + "            \"gravatar_id\": \"\",\n"
                                + "            \"url\": \"https://api.github.com/users/mattstifanelli\",\n"
                                + "            \"html_url\": \"https://github.com/mattstifanelli\",\n"
                                + "            \"followers_url\": \"https://api.github.com/users/mattstifanelli/followers\",\n"
                                + "            \"following_url\": \"https://api.github.com/users/mattstifanelli/following{/other_user}\",\n"
                                + "            \"gists_url\": \"https://api.github.com/users/mattstifanelli/gists{/gist_id}\",\n"
                                + "            \"starred_url\": \"https://api.github.com/users/mattstifanelli/starred{/owner}{/repo}\",\n"
                                + "            \"subscriptions_url\": \"https://api.github.com/users/mattstifanelli/subscriptions\",\n"
                                + "            \"organizations_url\": \"https://api.github.com/users/mattstifanelli/orgs\",\n"
                                + "            \"repos_url\": \"https://api.github.com/users/mattstifanelli/repos\",\n"
                                + "            \"events_url\": \"https://api.github.com/users/mattstifanelli/events{/privacy}\",\n"
                                + "            \"received_events_url\": \"https://api.github.com/users/mattstifanelli/received_events\",\n"
                                + "            \"type\": \"User\",\n"
                                + "            \"user_view_type\": \"public\",\n"
                                + "            \"site_admin\": false\n"
                                + "        },\n"
                                + "        \"created_at\": \""
                                + LocalDateTime.MAX.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                                + "\",\n" + "        \"updated_at\": \"2011-05-12T14:34:22Z\",\n"
                                + "        \"author_association\": \"NONE\",\n"
                                + "        \"body\": \"Let's try again via Issue tacker...\\n\",\n"
                                + "        \"reactions\": {\n"
                                + "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825/reactions\",\n"
                                + "            \"total_count\": 0,\n"
                                + "            \"+1\": 0,\n"
                                + "            \"-1\": 0,\n"
                                + "            \"laugh\": 0,\n"
                                + "            \"hooray\": 0,\n"
                                + "            \"confused\": 0,\n"
                                + "            \"heart\": 0,\n"
                                + "            \"rocket\": 0,\n"
                                + "            \"eyes\": 0\n"
                                + "        },\n"
                                + "        \"performed_via_github_app\": null\n"
                                + "    },\n"
                                + "    {\n"
                                + "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1325876\",\n"
                                + "        \"html_url\": \"https://github.com/octocat/Hello-World/pull/3#issuecomment-1325876\",\n"
                                + "        \"issue_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3\",\n"
                                + "        \"id\": 1325876,\n"
                                + "        \"node_id\": \"MDEyOklzc3VlQ29tbWVudDEzMjU4NzY=\",\n"
                                + "        \"user\": {\n"
                                + "            \"login\": \"shailendra75\",\n"
                                + "            \"id\": 831975,\n"
                                + "            \"node_id\": \"MDQ6VXNlcjgzMTk3NQ==\",\n"
                                + "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/831975?v=4\",\n"
                                + "            \"gravatar_id\": \"\",\n"
                                + "            \"url\": \"https://api.github.com/users/shailendra75\",\n"
                                + "            \"html_url\": \"https://github.com/shailendra75\",\n"
                                + "            \"followers_url\": \"https://api.github.com/users/shailendra75/followers\",\n"
                                + "            \"following_url\": \"https://api.github.com/users/shailendra75/following{/other_user}\",\n"
                                + "            \"gists_url\": \"https://api.github.com/users/shailendra75/gists{/gist_id}\",\n"
                                + "            \"starred_url\": \"https://api.github.com/users/shailendra75/starred{/owner}{/repo}\",\n"
                                + "            \"subscriptions_url\": \"https://api.github.com/users/shailendra75/subscriptions\",\n"
                                + "            \"organizations_url\": \"https://api.github.com/users/shailendra75/orgs\",\n"
                                + "            \"repos_url\": \"https://api.github.com/users/shailendra75/repos\",\n"
                                + "            \"events_url\": \"https://api.github.com/users/shailendra75/events{/privacy}\",\n"
                                + "            \"received_events_url\": \"https://api.github.com/users/shailendra75/received_events\",\n"
                                + "            \"type\": \"User\",\n"
                                + "            \"user_view_type\": \"public\",\n"
                                + "            \"site_admin\": false\n"
                                + "        },\n"
                                + "        \"created_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "        \"updated_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "        \"author_association\": \"NONE\",\n"
                                + "        \"body\": \"getting famlirized with git\\n\",\n"
                                + "        \"reactions\": {\n"
                                + "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1325876/reactions\",\n"
                                + "            \"total_count\": 0,\n"
                                + "            \"+1\": 0,\n"
                                + "            \"-1\": 0,\n"
                                + "            \"laugh\": 0,\n"
                                + "            \"hooray\": 0,\n"
                                + "            \"confused\": 0,\n"
                                + "            \"heart\": 0,\n"
                                + "            \"rocket\": 0,\n"
                                + "            \"eyes\": 0\n"
                                + "        },\n"
                                + "        \"performed_via_github_app\": null\n"
                                + "    },\n"
                                + "    {\n"
                                + "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1340258\",\n"
                                + "        \"html_url\": \"https://github.com/octocat/Hello-World/pull/1#issuecomment-1340258\",\n"
                                + "        \"issue_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/1\",\n"
                                + "        \"id\": 1340258,\n"
                                + "        \"node_id\": \"MDEyOklzc3VlQ29tbWVudDEzNDAyNTg=\",\n"
                                + "        \"user\": {\n"
                                + "            \"login\": \"masonzou\",\n"
                                + "            \"id\": 841296,\n"
                                + "            \"node_id\": \"MDQ6VXNlcjg0MTI5Ng==\",\n"
                                + "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/841296?v=4\",\n"
                                + "            \"gravatar_id\": \"\",\n"
                                + "            \"url\": \"https://api.github.com/users/masonzou\",\n"
                                + "            \"html_url\": \"https://github.com/masonzou\",\n"
                                + "            \"followers_url\": \"https://api.github.com/users/masonzou/followers\",\n"
                                + "            \"following_url\": \"https://api.github.com/users/masonzou/following{/other_user}\",\n"
                                + "            \"gists_url\": \"https://api.github.com/users/masonzou/gists{/gist_id}\",\n"
                                + "            \"starred_url\": \"https://api.github.com/users/masonzou/starred{/owner}{/repo}\",\n"
                                + "            \"subscriptions_url\": \"https://api.github.com/users/masonzou/subscriptions\",\n"
                                + "            \"organizations_url\": \"https://api.github.com/users/masonzou/orgs\",\n"
                                + "            \"repos_url\": \"https://api.github.com/users/masonzou/repos\",\n"
                                + "            \"events_url\": \"https://api.github.com/users/masonzou/events{/privacy}\",\n"
                                + "            \"received_events_url\": \"https://api.github.com/users/masonzou/received_events\",\n"
                                + "            \"type\": \"User\",\n"
                                + "            \"user_view_type\": \"public\",\n"
                                + "            \"site_admin\": false\n"
                                + "        },\n"
                                + "        \"created_at\": \""
                                + LocalDateTime.MAX.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                                + "\",\n" + "        \"updated_at\": \"2011-06-10T07:30:27Z\",\n"
                                + "        \"author_association\": \"NONE\",\n"
                                + "        \"body\": \"test\\n\",\n"
                                + "        \"reactions\": {\n"
                                + "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1340258/reactions\",\n"
                                + "            \"total_count\": 0,\n"
                                + "            \"+1\": 0,\n"
                                + "            \"-1\": 0,\n"
                                + "            \"laugh\": 0,\n"
                                + "            \"hooray\": 0,\n"
                                + "            \"confused\": 0,\n"
                                + "            \"heart\": 0,\n"
                                + "            \"rocket\": 0,\n"
                                + "            \"eyes\": 0\n"
                                + "        },\n"
                                + "        \"performed_via_github_app\": null\n"
                                + "    }"
                                + "]")));

        List<String> updates = gitHubIssueListClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.size()).isEqualTo(2);
        assertThat(updates.getFirst().trim()).isEqualTo(expectedMessage1.trim());
        assertThat(updates.get(1).trim()).isEqualTo(expectedMessage2.trim());
    }

    @Test
    void getUpdates_WhenNoNewComments_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubIssueListClient = new GitHubIssueListClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues"), restClient);

        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues");
        stubFor(get("/octocat/Hello-World/issues")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\n" + "    {\n"
                                + "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825\",\n"
                                + "        \"html_url\": \"https://github.com/octocat/Hello-World/pull/2#issuecomment-1146825\",\n"
                                + "        \"issue_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2\",\n"
                                + "        \"id\": 1146825,\n"
                                + "        \"node_id\": \"MDEyOklzc3VlQ29tbWVudDExNDY4MjU=\",\n"
                                + "        \"user\": {\n"
                                + "            \"login\": \"mattstifanelli\",\n"
                                + "            \"id\": 783382,\n"
                                + "            \"node_id\": \"MDQ6VXNlcjc4MzM4Mg==\",\n"
                                + "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/783382?v=4\",\n"
                                + "            \"gravatar_id\": \"\",\n"
                                + "            \"url\": \"https://api.github.com/users/mattstifanelli\",\n"
                                + "            \"html_url\": \"https://github.com/mattstifanelli\",\n"
                                + "            \"followers_url\": \"https://api.github.com/users/mattstifanelli/followers\",\n"
                                + "            \"following_url\": \"https://api.github.com/users/mattstifanelli/following{/other_user}\",\n"
                                + "            \"gists_url\": \"https://api.github.com/users/mattstifanelli/gists{/gist_id}\",\n"
                                + "            \"starred_url\": \"https://api.github.com/users/mattstifanelli/starred{/owner}{/repo}\",\n"
                                + "            \"subscriptions_url\": \"https://api.github.com/users/mattstifanelli/subscriptions\",\n"
                                + "            \"organizations_url\": \"https://api.github.com/users/mattstifanelli/orgs\",\n"
                                + "            \"repos_url\": \"https://api.github.com/users/mattstifanelli/repos\",\n"
                                + "            \"events_url\": \"https://api.github.com/users/mattstifanelli/events{/privacy}\",\n"
                                + "            \"received_events_url\": \"https://api.github.com/users/mattstifanelli/received_events\",\n"
                                + "            \"type\": \"User\",\n"
                                + "            \"user_view_type\": \"public\",\n"
                                + "            \"site_admin\": false\n"
                                + "        },\n"
                                + "        \"created_at\": \"2011-05-12T14:34:22Z\",\n"
                                + "        \"updated_at\": \"2011-05-12T14:34:22Z\",\n"
                                + "        \"author_association\": \"NONE\",\n"
                                + "        \"body\": \"Let's try again via Issue tacker...\\n\",\n"
                                + "        \"reactions\": {\n"
                                + "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825/reactions\",\n"
                                + "            \"total_count\": 0,\n"
                                + "            \"+1\": 0,\n"
                                + "            \"-1\": 0,\n"
                                + "            \"laugh\": 0,\n"
                                + "            \"hooray\": 0,\n"
                                + "            \"confused\": 0,\n"
                                + "            \"heart\": 0,\n"
                                + "            \"rocket\": 0,\n"
                                + "            \"eyes\": 0\n"
                                + "        },\n"
                                + "        \"performed_via_github_app\": null\n"
                                + "    }"
                                + "]")));

        List<String> updates = gitHubIssueListClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }

    @Test
    void getUpdates_WhenBadRequest_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubIssueListClient = new GitHubIssueListClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues"), restClient);

        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues");
        stubFor(get("/octocat/Hello-World/issues")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("bad request")));

        List<String> updates = gitHubIssueListClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
