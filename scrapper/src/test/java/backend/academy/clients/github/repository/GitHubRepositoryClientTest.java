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
        stubFor(
                get("/octocat/Hello-World/")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                        { "id": 1296269, "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5", "name":
                        "Hello-World", "full_name": "octocat/Hello-World", "private": false, "owner":
                        { "login": "octocat", "id": 583231, "node_id": "MDQ6VXNlcjU4MzIzMQ==", "avatar_url":
                        "https://avatars.githubusercontent.com/u/583231?v=4", "gravatar_id": "", "url":
                        "https://api.github.com/users/octocat", "html_url": "https://github.com/octocat",
                        "followers_url": "https://api.github.com/users/octocat/followers", "following_url":
                        "https://api.github.com/users/octocat/following{/other_user}", "gists_url":
                        "https://api.github.com/users/octocat/gists{/gist_id}", "starred_url":
                        "https://api.github.com/users/octocat/starred{/owner}{/repo}", "subscriptions_url":
                        "https://api.github.com/users/octocat/subscriptions", "organizations_url":
                        "https://api.github.com/users/octocat/orgs", "repos_url":
                        "https://api.github.com/users/octocat/repos", "events_url":
                        "https://api.github.com/users/octocat/events{/privacy}", "received_events_url":
                        "https://api.github.com/users/octocat/received_events", "type": "User", "user_view_type":
                        "public", "site_admin": false }, "html_url": "https://github.com/octocat/Hello-World",
                        "description": "My first repository on GitHub!", "fork": false, "url":
                        "https://api.github.com/repos/octocat/Hello-World", "forks_url":
                        "https://api.github.com/repos/octocat/Hello-World/forks", "keys_url": "https://api.github.com/repos/octocat/Hello-World/keys{/key_id}",
                        "collaborators_url": "https://api.github.com/repos/octocat/Hello-World/collaborators{/collaborator}",
                        "teams_url": "https://api.github.com/repos/octocat/Hello-World/teams", "hooks_url":
                        "https://api.github.com/repos/octocat/Hello-World/hooks", "issue_events_url":
                        "https://api.github.com/repos/octocat/Hello-World/issues/events{/number}", "events_url":
                        "https://api.github.com/repos/octocat/Hello-World/events", "assignees_url":
                        "https://api.github.com/repos/octocat/Hello-World/assignees{/user}", "branches_url":
                        "https://api.github.com/repos/octocat/Hello-World/branches{/branch}", "tags_url":
                        "https://api.github.com/repos/octocat/Hello-World/tags", "blobs_url":
                        "https://api.github.com/repos/octocat/Hello-World/git/blobs{/sha}", "git_tags_url":
                        "https://api.github.com/repos/octocat/Hello-World/git/tags{/sha}", "git_refs_url":
                        "https://api.github.com/repos/octocat/Hello-World/git/refs{/sha}", "trees_url":
                        "https://api.github.com/repos/octocat/Hello-World/git/trees{/sha}", "statuses_url":
                        "https://api.github.com/repos/octocat/Hello-World/statuses/{sha}", "languages_url":
                        "https://api.github.com/repos/octocat/Hello-World/languages", "stargazers_url":
                        "https://api.github.com/repos/octocat/Hello-World/stargazers", "contributors_url":
                        "https://api.github.com/repos/octocat/Hello-World/contributors", "subscribers_url":
                        "https://api.github.com/repos/octocat/Hello-World/subscribers", "subscription_url":
                        "https://api.github.com/repos/octocat/Hello-World/subscription", "commits_url":
                        "https://api.github.com/repos/octocat/Hello-World/commits{/sha}", "git_commits_url":
                        "https://api.github.com/repos/octocat/Hello-World/git/commits{/sha}", "comments_url":
                        "https://api.github.com/repos/octocat/Hello-World/comments{/number}", "issue_comment_url":
                        "https://api.github.com/repos/octocat/Hello-World/issues/comments{/number}", "contents_url":
                        "https://api.github.com/repos/octocat/Hello-World/contents/{+path}", "compare_url":
                        "https://api.github.com/repos/octocat/Hello-World/compare/{base}...{head}", "merges_url":
                        "https://api.github.com/repos/octocat/Hello-World/merges", "archive_url":
                        "https://api.github.com/repos/octocat/Hello-World/{archive_format}{/ref}",
                        "downloads_url": "https://api.github.com/repos/octocat/Hello-World/downloads",
                        "issues_url": "https://api.github.com/repos/octocat/Hello-World/issues{/number}",
                        "pulls_url": "https://api.github.com/repos/octocat/Hello-World/pulls{/number}",
                        "milestones_url": "https://api.github.com/repos/octocat/Hello-World/milestones{/number}",
                        "notifications_url": "https://api.github.com/repos/octocat/Hello-World/notifications{?since,all,participating}",
                        "labels_url": "https://api.github.com/repos/octocat/Hello-World/labels{/name}",
                        "releases_url": "https://api.github.com/repos/octocat/Hello-World/releases{/id}",
                        "deployments_url": "https://api.github.com/repos/octocat/Hello-World/deployments",
                        "created_at": "2011-01-26T19:01:12Z", "updated_at": "+999999999-12-31T23:59:59Z", "pushed_at":
                        "2024-08-20T23:54:42Z", "git_url": "git://github.com/octocat/Hello-World.git", "ssh_url":
                        "git@github.com:octocat/Hello-World.git", "clone_url": "https://github.com/octocat/Hello-World.git",
                        "svn_url": "https://github.com/octocat/Hello-World", "homepage": "", "size": 1, "stargazers_count": 2859,
                        "watchers_count": 2859, "language": null, "has_issues": true, "has_projects": true, "has_downloads": true,
                        "has_wiki": true, "has_pages": false, "has_discussions": false, "forks_count": 2723, "mirror_url": null,
                        "archived": false, "disabled": false, "open_issues_count": 1557, "license": null, "allow_forking": true,
                        "is_template": false, "web_commit_signoff_required": false, "topics": [], "visibility": "public", "forks": 2723,
                        "open_issues": 1557, "watchers": 2859, "default_branch": "master", "temp_clone_token": null,
                        "network_count": 2723, "subscribers_count": 1736 }
                        """)));

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
        stubFor(
                get("/octocat/Hello-World/")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            { "id": 1296269, "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5", "name": "Hello-World",
                            "full_name": "octocat/Hello-World", "private": false, "owner":
                            { "login": "octocat", "id": 583231, "node_id": "MDQ6VXNlcjU4MzIzMQ==",
                            "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                            "gravatar_id": "", "url": "https://api.github.com/users/octocat", "html_url":
                            "https://github.com/octocat", "followers_url":
                            "https://api.github.com/users/octocat/followers", "following_url":
                            "https://api.github.com/users/octocat/following{/other_user}",
                            "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
                            "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
                            "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
                            "organizations_url": "https://api.github.com/users/octocat/orgs", "repos_url":
                            "https://api.github.com/users/octocat/repos", "events_url":
                            "https://api.github.com/users/octocat/events{/privacy}", "received_events_url":
                            "https://api.github.com/users/octocat/received_events", "type": "User",
                            "user_view_type": "public", "site_admin": false }, "html_url":
                            "https://github.com/octocat/Hello-World", "description": "My first repository on GitHub!",
                             "fork": false, "url": "https://api.github.com/repos/octocat/Hello-World",
                             "forks_url": "https://api.github.com/repos/octocat/Hello-World/forks", "keys_url":
                             "https://api.github.com/repos/octocat/Hello-World/keys{/key_id}", "collaborators_url":
                             "https://api.github.com/repos/octocat/Hello-World/collaborators{/collaborator}", "teams_url":
                             "https://api.github.com/repos/octocat/Hello-World/teams", "hooks_url":
                             "https://api.github.com/repos/octocat/Hello-World/hooks", "issue_events_url":
                             "https://api.github.com/repos/octocat/Hello-World/issues/events{/number}", "events_url":
                             "https://api.github.com/repos/octocat/Hello-World/events", "assignees_url":
                             "https://api.github.com/repos/octocat/Hello-World/assignees{/user}", "branches_url":
                             "https://api.github.com/repos/octocat/Hello-World/branches{/branch}", "tags_url":
                             "https://api.github.com/repos/octocat/Hello-World/tags", "blobs_url":
                             "https://api.github.com/repos/octocat/Hello-World/git/blobs{/sha}", "git_tags_url":
                             "https://api.github.com/repos/octocat/Hello-World/git/tags{/sha}", "git_refs_url":
                             "https://api.github.com/repos/octocat/Hello-World/git/refs{/sha}", "trees_url":
                             "https://api.github.com/repos/octocat/Hello-World/git/trees{/sha}", "statuses_url":
                             "https://api.github.com/repos/octocat/Hello-World/statuses/{sha}", "languages_url":
                             "https://api.github.com/repos/octocat/Hello-World/languages", "stargazers_url":
                             "https://api.github.com/repos/octocat/Hello-World/stargazers", "contributors_url":
                             "https://api.github.com/repos/octocat/Hello-World/contributors", "subscribers_url":
                             "https://api.github.com/repos/octocat/Hello-World/subscribers", "subscription_url":
                             "https://api.github.com/repos/octocat/Hello-World/subscription", "commits_url":
                             "https://api.github.com/repos/octocat/Hello-World/commits{/sha}", "git_commits_url":
                             "https://api.github.com/repos/octocat/Hello-World/git/commits{/sha}", "comments_url":
                             "https://api.github.com/repos/octocat/Hello-World/comments{/number}", "issue_comment_url":
                             "https://api.github.com/repos/octocat/Hello-World/issues/comments{/number}", "contents_url":
                             "https://api.github.com/repos/octocat/Hello-World/contents/{+path}", "compare_url":
                             "https://api.github.com/repos/octocat/Hello-World/compare/{base}...{head}", "merges_url":
                             "https://api.github.com/repos/octocat/Hello-World/merges", "archive_url":
                             "https://api.github.com/repos/octocat/Hello-World/{archive_format}{/ref}",
                             "downloads_url": "https://api.github.com/repos/octocat/Hello-World/downloads",
                             "issues_url": "https://api.github.com/repos/octocat/Hello-World/issues{/number}",
                             "pulls_url": "https://api.github.com/repos/octocat/Hello-World/pulls{/number}",
                             "milestones_url": "https://api.github.com/repos/octocat/Hello-World/milestones{/number}",
                             "notifications_url": "https://api.github.com/repos/octocat/Hello-World/notifications{?since,all,participating}",
                             "labels_url": "https://api.github.com/repos/octocat/Hello-World/labels{/name}",
                             "releases_url": "https://api.github.com/repos/octocat/Hello-World/releases{/id}",
                             "deployments_url": "https://api.github.com/repos/octocat/Hello-World/deployments",
                             "created_at": "2011-01-26T19:01:12Z", "updated_at": "2011-01-26T19:01:12Z", "pushed_at":
                             "2024-08-20T23:54:42Z", "git_url": "git://github.com/octocat/Hello-World.git", "ssh_url":
                             "git@github.com:octocat/Hello-World.git", "clone_url": "https://github.com/octocat/Hello-World.git",
                             "svn_url": "https://github.com/octocat/Hello-World", "homepage": "", "size": 1,
                             "stargazers_count": 2859, "watchers_count": 2859, "language": null, "has_issues": true,
                             "has_projects": true, "has_downloads": true, "has_wiki": true, "has_pages": false, "has_discussions":
                             false, "forks_count": 2723, "mirror_url": null, "archived": false, "disabled": false, "open_issues_count":
                             1557, "license": null, "allow_forking": true, "is_template": false, "web_commit_signoff_required": false,
                             "topics": [], "visibility": "public", "forks": 2723, "open_issues": 1557, "watchers": 2859,
                             "default_branch": "master", "temp_clone_token": null, "network_count": 2723, "subscribers_count": 1736 }
                            """)));

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
        stubFor(
                get("/octocat/Hello-Worldd")
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                        { "message": "Not Found", "documentation_url": "https://docs.github.com/rest/repos/repos#get-a-repository", "status": "404" }
                        """)));

        List<String> updates = gitHubRepositoryClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
