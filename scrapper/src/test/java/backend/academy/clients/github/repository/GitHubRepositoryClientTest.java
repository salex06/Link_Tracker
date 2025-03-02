package backend.academy.clients.github.repository;

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

class GitHubRepositoryClientTest {
    private int port = 8090;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static GitHubRepositoryClient gitHubRepositoryClient;

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
    void getUpdates_WhenRepositoryWasUpdated_ThenReturnUpdateMessage() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubRepositoryClient = new GitHubRepositoryClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/"), restClient);

        String expectedMessage = "Обновление репозитория Hello-World по ссылке https://github.com/octocat/Hello-World";
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/");
        stubFor(get("/octocat/Hello-World/")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"id\": 1296269,\n"
                                + "    \"node_id\": \"MDEwOlJlcG9zaXRvcnkxMjk2MjY5\",\n"
                                + "    \"name\": \"Hello-World\",\n"
                                + "    \"full_name\": \"octocat/Hello-World\",\n"
                                + "    \"private\": false,\n"
                                + "    \"owner\": {\n"
                                + "        \"login\": \"octocat\",\n"
                                + "        \"id\": 583231,\n"
                                + "        \"node_id\": \"MDQ6VXNlcjU4MzIzMQ==\",\n"
                                + "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/583231?v=4\",\n"
                                + "        \"gravatar_id\": \"\",\n"
                                + "        \"url\": \"https://api.github.com/users/octocat\",\n"
                                + "        \"html_url\": \"https://github.com/octocat\",\n"
                                + "        \"followers_url\": \"https://api.github.com/users/octocat/followers\",\n"
                                + "        \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",\n"
                                + "        \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",\n"
                                + "        \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",\n"
                                + "        \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",\n"
                                + "        \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",\n"
                                + "        \"repos_url\": \"https://api.github.com/users/octocat/repos\",\n"
                                + "        \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",\n"
                                + "        \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",\n"
                                + "        \"type\": \"User\",\n"
                                + "        \"user_view_type\": \"public\",\n"
                                + "        \"site_admin\": false\n"
                                + "    },\n"
                                + "    \"html_url\": \"https://github.com/octocat/Hello-World\",\n"
                                + "    \"description\": \"My first repository on GitHub!\",\n"
                                + "    \"fork\": false,\n"
                                + "    \"url\": \"https://api.github.com/repos/octocat/Hello-World\",\n"
                                + "    \"forks_url\": \"https://api.github.com/repos/octocat/Hello-World/forks\",\n"
                                + "    \"keys_url\": \"https://api.github.com/repos/octocat/Hello-World/keys{/key_id}\",\n"
                                + "    \"collaborators_url\": \"https://api.github.com/repos/octocat/Hello-World/collaborators{/collaborator}\",\n"
                                + "    \"teams_url\": \"https://api.github.com/repos/octocat/Hello-World/teams\",\n"
                                + "    \"hooks_url\": \"https://api.github.com/repos/octocat/Hello-World/hooks\",\n"
                                + "    \"issue_events_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/events{/number}\",\n"
                                + "    \"events_url\": \"https://api.github.com/repos/octocat/Hello-World/events\",\n"
                                + "    \"assignees_url\": \"https://api.github.com/repos/octocat/Hello-World/assignees{/user}\",\n"
                                + "    \"branches_url\": \"https://api.github.com/repos/octocat/Hello-World/branches{/branch}\",\n"
                                + "    \"tags_url\": \"https://api.github.com/repos/octocat/Hello-World/tags\",\n"
                                + "    \"blobs_url\": \"https://api.github.com/repos/octocat/Hello-World/git/blobs{/sha}\",\n"
                                + "    \"git_tags_url\": \"https://api.github.com/repos/octocat/Hello-World/git/tags{/sha}\",\n"
                                + "    \"git_refs_url\": \"https://api.github.com/repos/octocat/Hello-World/git/refs{/sha}\",\n"
                                + "    \"trees_url\": \"https://api.github.com/repos/octocat/Hello-World/git/trees{/sha}\",\n"
                                + "    \"statuses_url\": \"https://api.github.com/repos/octocat/Hello-World/statuses/{sha}\",\n"
                                + "    \"languages_url\": \"https://api.github.com/repos/octocat/Hello-World/languages\",\n"
                                + "    \"stargazers_url\": \"https://api.github.com/repos/octocat/Hello-World/stargazers\",\n"
                                + "    \"contributors_url\": \"https://api.github.com/repos/octocat/Hello-World/contributors\",\n"
                                + "    \"subscribers_url\": \"https://api.github.com/repos/octocat/Hello-World/subscribers\",\n"
                                + "    \"subscription_url\": \"https://api.github.com/repos/octocat/Hello-World/subscription\",\n"
                                + "    \"commits_url\": \"https://api.github.com/repos/octocat/Hello-World/commits{/sha}\",\n"
                                + "    \"git_commits_url\": \"https://api.github.com/repos/octocat/Hello-World/git/commits{/sha}\",\n"
                                + "    \"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/comments{/number}\",\n"
                                + "    \"issue_comment_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments{/number}\",\n"
                                + "    \"contents_url\": \"https://api.github.com/repos/octocat/Hello-World/contents/{+path}\",\n"
                                + "    \"compare_url\": \"https://api.github.com/repos/octocat/Hello-World/compare/{base}...{head}\",\n"
                                + "    \"merges_url\": \"https://api.github.com/repos/octocat/Hello-World/merges\",\n"
                                + "    \"archive_url\": \"https://api.github.com/repos/octocat/Hello-World/{archive_format}{/ref}\",\n"
                                + "    \"downloads_url\": \"https://api.github.com/repos/octocat/Hello-World/downloads\",\n"
                                + "    \"issues_url\": \"https://api.github.com/repos/octocat/Hello-World/issues{/number}\",\n"
                                + "    \"pulls_url\": \"https://api.github.com/repos/octocat/Hello-World/pulls{/number}\",\n"
                                + "    \"milestones_url\": \"https://api.github.com/repos/octocat/Hello-World/milestones{/number}\",\n"
                                + "    \"notifications_url\": \"https://api.github.com/repos/octocat/Hello-World/notifications{?since,all,participating}\",\n"
                                + "    \"labels_url\": \"https://api.github.com/repos/octocat/Hello-World/labels{/name}\",\n"
                                + "    \"releases_url\": \"https://api.github.com/repos/octocat/Hello-World/releases{/id}\",\n"
                                + "    \"deployments_url\": \"https://api.github.com/repos/octocat/Hello-World/deployments\",\n"
                                + "    \"created_at\": \"2011-01-26T19:01:12Z\",\n"
                                + "    \"updated_at\": \""
                                + LocalDateTime.MAX.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
                                + "\",\n" + "    \"pushed_at\": \"2024-08-20T23:54:42Z\",\n"
                                + "    \"git_url\": \"git://github.com/octocat/Hello-World.git\",\n"
                                + "    \"ssh_url\": \"git@github.com:octocat/Hello-World.git\",\n"
                                + "    \"clone_url\": \"https://github.com/octocat/Hello-World.git\",\n"
                                + "    \"svn_url\": \"https://github.com/octocat/Hello-World\",\n"
                                + "    \"homepage\": \"\",\n"
                                + "    \"size\": 1,\n"
                                + "    \"stargazers_count\": 2859,\n"
                                + "    \"watchers_count\": 2859,\n"
                                + "    \"language\": null,\n"
                                + "    \"has_issues\": true,\n"
                                + "    \"has_projects\": true,\n"
                                + "    \"has_downloads\": true,\n"
                                + "    \"has_wiki\": true,\n"
                                + "    \"has_pages\": false,\n"
                                + "    \"has_discussions\": false,\n"
                                + "    \"forks_count\": 2723,\n"
                                + "    \"mirror_url\": null,\n"
                                + "    \"archived\": false,\n"
                                + "    \"disabled\": false,\n"
                                + "    \"open_issues_count\": 1557,\n"
                                + "    \"license\": null,\n"
                                + "    \"allow_forking\": true,\n"
                                + "    \"is_template\": false,\n"
                                + "    \"web_commit_signoff_required\": false,\n"
                                + "    \"topics\": [],\n"
                                + "    \"visibility\": \"public\",\n"
                                + "    \"forks\": 2723,\n"
                                + "    \"open_issues\": 1557,\n"
                                + "    \"watchers\": 2859,\n"
                                + "    \"default_branch\": \"master\",\n"
                                + "    \"temp_clone_token\": null,\n"
                                + "    \"network_count\": 2723,\n"
                                + "    \"subscribers_count\": 1736\n"
                                + "}")));

        List<String> updates = gitHubRepositoryClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.getFirst()).isEqualTo(expectedMessage);
    }

    @Test
    void getUpdates_WhenRepositoryWasNotUpdated_ThenReturnBlank() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubRepositoryClient = new GitHubRepositoryClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/"), restClient);

        String expectedMessage = "Обновление репозитория Hello-World по ссылке https://github.com/octocat/Hello-World";
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/");
        stubFor(get("/octocat/Hello-World/")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"id\": 1296269,\n"
                                + "    \"node_id\": \"MDEwOlJlcG9zaXRvcnkxMjk2MjY5\",\n"
                                + "    \"name\": \"Hello-World\",\n"
                                + "    \"full_name\": \"octocat/Hello-World\",\n"
                                + "    \"private\": false,\n"
                                + "    \"owner\": {\n"
                                + "        \"login\": \"octocat\",\n"
                                + "        \"id\": 583231,\n"
                                + "        \"node_id\": \"MDQ6VXNlcjU4MzIzMQ==\",\n"
                                + "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/583231?v=4\",\n"
                                + "        \"gravatar_id\": \"\",\n"
                                + "        \"url\": \"https://api.github.com/users/octocat\",\n"
                                + "        \"html_url\": \"https://github.com/octocat\",\n"
                                + "        \"followers_url\": \"https://api.github.com/users/octocat/followers\",\n"
                                + "        \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",\n"
                                + "        \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",\n"
                                + "        \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",\n"
                                + "        \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",\n"
                                + "        \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",\n"
                                + "        \"repos_url\": \"https://api.github.com/users/octocat/repos\",\n"
                                + "        \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",\n"
                                + "        \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",\n"
                                + "        \"type\": \"User\",\n"
                                + "        \"user_view_type\": \"public\",\n"
                                + "        \"site_admin\": false\n"
                                + "    },\n"
                                + "    \"html_url\": \"https://github.com/octocat/Hello-World\",\n"
                                + "    \"description\": \"My first repository on GitHub!\",\n"
                                + "    \"fork\": false,\n"
                                + "    \"url\": \"https://api.github.com/repos/octocat/Hello-World\",\n"
                                + "    \"forks_url\": \"https://api.github.com/repos/octocat/Hello-World/forks\",\n"
                                + "    \"keys_url\": \"https://api.github.com/repos/octocat/Hello-World/keys{/key_id}\",\n"
                                + "    \"collaborators_url\": \"https://api.github.com/repos/octocat/Hello-World/collaborators{/collaborator}\",\n"
                                + "    \"teams_url\": \"https://api.github.com/repos/octocat/Hello-World/teams\",\n"
                                + "    \"hooks_url\": \"https://api.github.com/repos/octocat/Hello-World/hooks\",\n"
                                + "    \"issue_events_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/events{/number}\",\n"
                                + "    \"events_url\": \"https://api.github.com/repos/octocat/Hello-World/events\",\n"
                                + "    \"assignees_url\": \"https://api.github.com/repos/octocat/Hello-World/assignees{/user}\",\n"
                                + "    \"branches_url\": \"https://api.github.com/repos/octocat/Hello-World/branches{/branch}\",\n"
                                + "    \"tags_url\": \"https://api.github.com/repos/octocat/Hello-World/tags\",\n"
                                + "    \"blobs_url\": \"https://api.github.com/repos/octocat/Hello-World/git/blobs{/sha}\",\n"
                                + "    \"git_tags_url\": \"https://api.github.com/repos/octocat/Hello-World/git/tags{/sha}\",\n"
                                + "    \"git_refs_url\": \"https://api.github.com/repos/octocat/Hello-World/git/refs{/sha}\",\n"
                                + "    \"trees_url\": \"https://api.github.com/repos/octocat/Hello-World/git/trees{/sha}\",\n"
                                + "    \"statuses_url\": \"https://api.github.com/repos/octocat/Hello-World/statuses/{sha}\",\n"
                                + "    \"languages_url\": \"https://api.github.com/repos/octocat/Hello-World/languages\",\n"
                                + "    \"stargazers_url\": \"https://api.github.com/repos/octocat/Hello-World/stargazers\",\n"
                                + "    \"contributors_url\": \"https://api.github.com/repos/octocat/Hello-World/contributors\",\n"
                                + "    \"subscribers_url\": \"https://api.github.com/repos/octocat/Hello-World/subscribers\",\n"
                                + "    \"subscription_url\": \"https://api.github.com/repos/octocat/Hello-World/subscription\",\n"
                                + "    \"commits_url\": \"https://api.github.com/repos/octocat/Hello-World/commits{/sha}\",\n"
                                + "    \"git_commits_url\": \"https://api.github.com/repos/octocat/Hello-World/git/commits{/sha}\",\n"
                                + "    \"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/comments{/number}\",\n"
                                + "    \"issue_comment_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments{/number}\",\n"
                                + "    \"contents_url\": \"https://api.github.com/repos/octocat/Hello-World/contents/{+path}\",\n"
                                + "    \"compare_url\": \"https://api.github.com/repos/octocat/Hello-World/compare/{base}...{head}\",\n"
                                + "    \"merges_url\": \"https://api.github.com/repos/octocat/Hello-World/merges\",\n"
                                + "    \"archive_url\": \"https://api.github.com/repos/octocat/Hello-World/{archive_format}{/ref}\",\n"
                                + "    \"downloads_url\": \"https://api.github.com/repos/octocat/Hello-World/downloads\",\n"
                                + "    \"issues_url\": \"https://api.github.com/repos/octocat/Hello-World/issues{/number}\",\n"
                                + "    \"pulls_url\": \"https://api.github.com/repos/octocat/Hello-World/pulls{/number}\",\n"
                                + "    \"milestones_url\": \"https://api.github.com/repos/octocat/Hello-World/milestones{/number}\",\n"
                                + "    \"notifications_url\": \"https://api.github.com/repos/octocat/Hello-World/notifications{?since,all,participating}\",\n"
                                + "    \"labels_url\": \"https://api.github.com/repos/octocat/Hello-World/labels{/name}\",\n"
                                + "    \"releases_url\": \"https://api.github.com/repos/octocat/Hello-World/releases{/id}\",\n"
                                + "    \"deployments_url\": \"https://api.github.com/repos/octocat/Hello-World/deployments\",\n"
                                + "    \"created_at\": \"2011-01-26T19:01:12Z\",\n"
                                + "    \"updated_at\": \"2011-01-26T19:01:12Z\",\n"
                                + "    \"pushed_at\": \"2024-08-20T23:54:42Z\",\n"
                                + "    \"git_url\": \"git://github.com/octocat/Hello-World.git\",\n"
                                + "    \"ssh_url\": \"git@github.com:octocat/Hello-World.git\",\n"
                                + "    \"clone_url\": \"https://github.com/octocat/Hello-World.git\",\n"
                                + "    \"svn_url\": \"https://github.com/octocat/Hello-World\",\n"
                                + "    \"homepage\": \"\",\n"
                                + "    \"size\": 1,\n"
                                + "    \"stargazers_count\": 2859,\n"
                                + "    \"watchers_count\": 2859,\n"
                                + "    \"language\": null,\n"
                                + "    \"has_issues\": true,\n"
                                + "    \"has_projects\": true,\n"
                                + "    \"has_downloads\": true,\n"
                                + "    \"has_wiki\": true,\n"
                                + "    \"has_pages\": false,\n"
                                + "    \"has_discussions\": false,\n"
                                + "    \"forks_count\": 2723,\n"
                                + "    \"mirror_url\": null,\n"
                                + "    \"archived\": false,\n"
                                + "    \"disabled\": false,\n"
                                + "    \"open_issues_count\": 1557,\n"
                                + "    \"license\": null,\n"
                                + "    \"allow_forking\": true,\n"
                                + "    \"is_template\": false,\n"
                                + "    \"web_commit_signoff_required\": false,\n"
                                + "    \"topics\": [],\n"
                                + "    \"visibility\": \"public\",\n"
                                + "    \"forks\": 2723,\n"
                                + "    \"open_issues\": 1557,\n"
                                + "    \"watchers\": 2859,\n"
                                + "    \"default_branch\": \"master\",\n"
                                + "    \"temp_clone_token\": null,\n"
                                + "    \"network_count\": 2723,\n"
                                + "    \"subscribers_count\": 1736\n"
                                + "}")));

        List<String> updates = gitHubRepositoryClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.size()).isEqualTo(1);
        assertThat(updates.getFirst()).isBlank();
    }

    @Test
    void getUpdates_WhenRepositoryNotFound_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubRepositoryClient = new GitHubRepositoryClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-Worldd"), restClient);

        String expectedMessage = "Обновление репозитория Hello-World по ссылке https://github.com/octocat/Hello-Worldd";
        Link link = new Link(1L, "https://github.com/octocat/Hello-Worldd");
        stubFor(get("/octocat/Hello-Worldd")
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"message\": \"Not Found\",\n"
                                + "    \"documentation_url\": \"https://docs.github.com/rest/repos/repos#get-a-repository\",\n"
                                + "    \"status\": \"404\"\n"
                                + "}")));

        List<String> updates = gitHubRepositoryClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
