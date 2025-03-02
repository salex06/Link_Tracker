package backend.academy.clients.github.issues;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

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

class GitHubSingleIssueClientTest {
    private int port = 8090;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static GitHubSingleIssueClient gitHubSingleIssueClient;

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
    void getUpdates_WhenIssueWasUpdated_ThenReturnUpdateMessage() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubSingleIssueClient = new GitHubSingleIssueClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues/3"), restClient);

        String expectedMessage =
                "Обновление issue #Edited README via GitHub по ссылке https://github.com/octocat/Hello-World/pull/3";
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues/3");
        stubFor(get("/octocat/Hello-World/issues/3")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "\t\"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3\",\n"
                                + "\t\"repository_url\": \"https://api.github.com/repos/octocat/Hello-World\",\n"
                                + "\t\"labels_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/labels{/name}\",\n"
                                + "\t\"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/comments\",\n"
                                + "\t\"events_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/events\",\n"
                                + "\t\"html_url\": \"https://github.com/octocat/Hello-World/pull/3\",\n"
                                + "\t\"id\": 1023235,\n"
                                + "\t\"node_id\": \"MDExOlB1bGxSZXF1ZXN0MTc1MTQx\",\n"
                                + "\t\"number\": 3,\n"
                                + "\t\"title\": \"Edited README via GitHub\",\n"
                                + "\t\"user\": {\n"
                                + "\t\t\"login\": \"shailendra75\",\n"
                                + "\t\t\"id\": 831975,\n"
                                + "\t\t\"node_id\": \"MDQ6VXNlcjgzMTk3NQ==\",\n"
                                + "\t\t\"avatar_url\": \"https://avatars.githubusercontent.com/u/831975?v=4\",\n"
                                + "\t\t\"gravatar_id\": \"\",\n"
                                + "\t\t\"url\": \"https://api.github.com/users/shailendra75\",\n"
                                + "\t\t\"html_url\": \"https://github.com/shailendra75\",\n"
                                + "\t\t\"followers_url\": \"https://api.github.com/users/shailendra75/followers\",\n"
                                + "\t\t\"following_url\": \"https://api.github.com/users/shailendra75/following{/other_user}\",\n"
                                + "\t\t\"gists_url\": \"https://api.github.com/users/shailendra75/gists{/gist_id}\",\n"
                                + "\t\t\"starred_url\": \"https://api.github.com/users/shailendra75/starred{/owner}{/repo}\",\n"
                                + "\t\t\"subscriptions_url\": \"https://api.github.com/users/shailendra75/subscriptions\",\n"
                                + "\t\t\"organizations_url\": \"https://api.github.com/users/shailendra75/orgs\",\n"
                                + "\t\t\"repos_url\": \"https://api.github.com/users/shailendra75/repos\",\n"
                                + "\t\t\"events_url\": \"https://api.github.com/users/shailendra75/events{/privacy}\",\n"
                                + "\t\t\"received_events_url\": \"https://api.github.com/users/shailendra75/received_events\",\n"
                                + "\t\t\"type\": \"User\",\n"
                                + "\t\t\"user_view_type\": \"public\",\n"
                                + "\t\t\"site_admin\": false\n"
                                + "\t},\n"
                                + "\t\"labels\": [],\n"
                                + "\t\"state\": \"closed\",\n"
                                + "\t\"locked\": false,\n"
                                + "\t\"assignee\": null,\n"
                                + "\t\"assignees\": [],\n"
                                + "\t\"milestone\": null,\n"
                                + "\t\"comments\": 1,\n"
                                + "\t\"created_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "\t\"updated_at\":\""
                                + LocalDateTime.MAX.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                                + "\",\n" + "\t\"closed_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "\t\"author_association\": \"NONE\",\n"
                                + "\t\"sub_issues_summary\": {\n"
                                + "\t\t\"total\": 0,\n"
                                + "\t\t\"completed\": 0,\n"
                                + "\t\t\"percent_completed\": 0\n"
                                + "\t},\n"
                                + "\t\"active_lock_reason\": null,\n"
                                + "\t\"draft\": false,\n"
                                + "\t\"pull_request\": {\n"
                                + "\t\t\"url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/3\",\n"
                                + "\t\t\"html_url\": \"https://github.com/octocat/Hello-World/pull/3\",\n"
                                + "\t\t\"diff_url\": \"https://github.com/octocat/Hello-World/pull/3.diff\",\n"
                                + "\t\t\"patch_url\": \"https://github.com/octocat/Hello-World/pull/3.patch\",\n"
                                + "\t\t\"merged_at\": null\n"
                                + "\t},\n"
                                + "\t\"body\": \"\",\n"
                                + "\t\"closed_by\": {\n"
                                + "\t\t\"login\": \"shailendra75\",\n"
                                + "\t\t\"id\": 831975,\n"
                                + "\t\t\"node_id\": \"MDQ6VXNlcjgzMTk3NQ==\",\n"
                                + "\t\t\"avatar_url\": \"https://avatars.githubusercontent.com/u/831975?v=4\",\n"
                                + "\t\t\"gravatar_id\": \"\",\n"
                                + "\t\t\"url\": \"https://api.github.com/users/shailendra75\",\n"
                                + "\t\t\"html_url\": \"https://github.com/shailendra75\",\n"
                                + "\t\t\"followers_url\": \"https://api.github.com/users/shailendra75/followers\",\n"
                                + "\t\t\"following_url\": \"https://api.github.com/users/shailendra75/following{/other_user}\",\n"
                                + "\t\t\"gists_url\": \"https://api.github.com/users/shailendra75/gists{/gist_id}\",\n"
                                + "\t\t\"starred_url\": \"https://api.github.com/users/shailendra75/starred{/owner}{/repo}\",\n"
                                + "\t\t\"subscriptions_url\": \"https://api.github.com/users/shailendra75/subscriptions\",\n"
                                + "\t\t\"organizations_url\": \"https://api.github.com/users/shailendra75/orgs\",\n"
                                + "\t\t\"repos_url\": \"https://api.github.com/users/shailendra75/repos\",\n"
                                + "\t\t\"events_url\": \"https://api.github.com/users/shailendra75/events{/privacy}\",\n"
                                + "\t\t\"received_events_url\": \"https://api.github.com/users/shailendra75/received_events\",\n"
                                + "\t\t\"type\": \"User\",\n"
                                + "\t\t\"user_view_type\": \"public\",\n"
                                + "\t\t\"site_admin\": false\n"
                                + "\t},\n"
                                + "\t\"reactions\": {\n"
                                + "\t\t\"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/reactions\",\n"
                                + "\t\t\"total_count\": 0,\n"
                                + "\t\t\"+1\": 0,\n"
                                + "\t\t\"-1\": 0,\n"
                                + "\t\t\"laugh\": 0,\n"
                                + "\t\t\"hooray\": 0,\n"
                                + "\t\t\"confused\": 0,\n"
                                + "\t\t\"heart\": 0,\n"
                                + "\t\t\"rocket\": 0,\n"
                                + "\t\t\"eyes\": 0\n"
                                + "\t},\n"
                                + "\t\"timeline_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/timeline\",\n"
                                + "\t\"performed_via_github_app\": null,\n"
                                + "\t\"state_reason\": null\n"
                                + "}")));

        List<String> updates = gitHubSingleIssueClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.getFirst()).isEqualTo(expectedMessage);
    }

    @Test
    void getUpdates_WhenIssueWasNotUpdated_ThenReturnEmpty() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubSingleIssueClient = new GitHubSingleIssueClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues/3"), restClient);

        String expectedMessage =
                "Обновление issue #Edited README via GitHub по ссылке https://github.com/octocat/Hello-World/pull/3";
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues/3");
        stubFor(get("/octocat/Hello-World/issues/3")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "\t\"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3\",\n"
                                + "\t\"repository_url\": \"https://api.github.com/repos/octocat/Hello-World\",\n"
                                + "\t\"labels_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/labels{/name}\",\n"
                                + "\t\"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/comments\",\n"
                                + "\t\"events_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/events\",\n"
                                + "\t\"html_url\": \"https://github.com/octocat/Hello-World/pull/3\",\n"
                                + "\t\"id\": 1023235,\n"
                                + "\t\"node_id\": \"MDExOlB1bGxSZXF1ZXN0MTc1MTQx\",\n"
                                + "\t\"number\": 3,\n"
                                + "\t\"title\": \"Edited README via GitHub\",\n"
                                + "\t\"user\": {\n"
                                + "\t\t\"login\": \"shailendra75\",\n"
                                + "\t\t\"id\": 831975,\n"
                                + "\t\t\"node_id\": \"MDQ6VXNlcjgzMTk3NQ==\",\n"
                                + "\t\t\"avatar_url\": \"https://avatars.githubusercontent.com/u/831975?v=4\",\n"
                                + "\t\t\"gravatar_id\": \"\",\n"
                                + "\t\t\"url\": \"https://api.github.com/users/shailendra75\",\n"
                                + "\t\t\"html_url\": \"https://github.com/shailendra75\",\n"
                                + "\t\t\"followers_url\": \"https://api.github.com/users/shailendra75/followers\",\n"
                                + "\t\t\"following_url\": \"https://api.github.com/users/shailendra75/following{/other_user}\",\n"
                                + "\t\t\"gists_url\": \"https://api.github.com/users/shailendra75/gists{/gist_id}\",\n"
                                + "\t\t\"starred_url\": \"https://api.github.com/users/shailendra75/starred{/owner}{/repo}\",\n"
                                + "\t\t\"subscriptions_url\": \"https://api.github.com/users/shailendra75/subscriptions\",\n"
                                + "\t\t\"organizations_url\": \"https://api.github.com/users/shailendra75/orgs\",\n"
                                + "\t\t\"repos_url\": \"https://api.github.com/users/shailendra75/repos\",\n"
                                + "\t\t\"events_url\": \"https://api.github.com/users/shailendra75/events{/privacy}\",\n"
                                + "\t\t\"received_events_url\": \"https://api.github.com/users/shailendra75/received_events\",\n"
                                + "\t\t\"type\": \"User\",\n"
                                + "\t\t\"user_view_type\": \"public\",\n"
                                + "\t\t\"site_admin\": false\n"
                                + "\t},\n"
                                + "\t\"labels\": [],\n"
                                + "\t\"state\": \"closed\",\n"
                                + "\t\"locked\": false,\n"
                                + "\t\"assignee\": null,\n"
                                + "\t\"assignees\": [],\n"
                                + "\t\"milestone\": null,\n"
                                + "\t\"comments\": 1,\n"
                                + "\t\"created_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "\t\"updated_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "\t\"closed_at\": \"2011-06-08T10:58:32Z\",\n"
                                + "\t\"author_association\": \"NONE\",\n"
                                + "\t\"sub_issues_summary\": {\n"
                                + "\t\t\"total\": 0,\n"
                                + "\t\t\"completed\": 0,\n"
                                + "\t\t\"percent_completed\": 0\n"
                                + "\t},\n"
                                + "\t\"active_lock_reason\": null,\n"
                                + "\t\"draft\": false,\n"
                                + "\t\"pull_request\": {\n"
                                + "\t\t\"url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/3\",\n"
                                + "\t\t\"html_url\": \"https://github.com/octocat/Hello-World/pull/3\",\n"
                                + "\t\t\"diff_url\": \"https://github.com/octocat/Hello-World/pull/3.diff\",\n"
                                + "\t\t\"patch_url\": \"https://github.com/octocat/Hello-World/pull/3.patch\",\n"
                                + "\t\t\"merged_at\": null\n"
                                + "\t},\n"
                                + "\t\"body\": \"\",\n"
                                + "\t\"closed_by\": {\n"
                                + "\t\t\"login\": \"shailendra75\",\n"
                                + "\t\t\"id\": 831975,\n"
                                + "\t\t\"node_id\": \"MDQ6VXNlcjgzMTk3NQ==\",\n"
                                + "\t\t\"avatar_url\": \"https://avatars.githubusercontent.com/u/831975?v=4\",\n"
                                + "\t\t\"gravatar_id\": \"\",\n"
                                + "\t\t\"url\": \"https://api.github.com/users/shailendra75\",\n"
                                + "\t\t\"html_url\": \"https://github.com/shailendra75\",\n"
                                + "\t\t\"followers_url\": \"https://api.github.com/users/shailendra75/followers\",\n"
                                + "\t\t\"following_url\": \"https://api.github.com/users/shailendra75/following{/other_user}\",\n"
                                + "\t\t\"gists_url\": \"https://api.github.com/users/shailendra75/gists{/gist_id}\",\n"
                                + "\t\t\"starred_url\": \"https://api.github.com/users/shailendra75/starred{/owner}{/repo}\",\n"
                                + "\t\t\"subscriptions_url\": \"https://api.github.com/users/shailendra75/subscriptions\",\n"
                                + "\t\t\"organizations_url\": \"https://api.github.com/users/shailendra75/orgs\",\n"
                                + "\t\t\"repos_url\": \"https://api.github.com/users/shailendra75/repos\",\n"
                                + "\t\t\"events_url\": \"https://api.github.com/users/shailendra75/events{/privacy}\",\n"
                                + "\t\t\"received_events_url\": \"https://api.github.com/users/shailendra75/received_events\",\n"
                                + "\t\t\"type\": \"User\",\n"
                                + "\t\t\"user_view_type\": \"public\",\n"
                                + "\t\t\"site_admin\": false\n"
                                + "\t},\n"
                                + "\t\"reactions\": {\n"
                                + "\t\t\"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/reactions\",\n"
                                + "\t\t\"total_count\": 0,\n"
                                + "\t\t\"+1\": 0,\n"
                                + "\t\t\"-1\": 0,\n"
                                + "\t\t\"laugh\": 0,\n"
                                + "\t\t\"hooray\": 0,\n"
                                + "\t\t\"confused\": 0,\n"
                                + "\t\t\"heart\": 0,\n"
                                + "\t\t\"rocket\": 0,\n"
                                + "\t\t\"eyes\": 0\n"
                                + "\t},\n"
                                + "\t\"timeline_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/3/timeline\",\n"
                                + "\t\"performed_via_github_app\": null,\n"
                                + "\t\"state_reason\": null\n"
                                + "}")));

        List<String> updates = gitHubSingleIssueClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }

    @Test
    void getUpdates_WhenWrongRequest_ThenReturnEmpty() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubSingleIssueClient = new GitHubSingleIssueClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues/3000"), restClient);

        String expectedMessage =
                "Обновление issue #Edited README via GitHub по ссылке https://github.com/octocat/Hello-World/pull/3000";
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues/3000");
        stubFor(get("/octocat/Hello-World/issues/3")
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Not Found\",\"documentation_url\":"
                                + "\"https://docs.github.com/rest/issues/issues#get-an-issue\",\"status\":\"404\"}")));

        List<String> updates = gitHubSingleIssueClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
