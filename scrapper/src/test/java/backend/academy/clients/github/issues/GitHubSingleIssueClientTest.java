package backend.academy.clients.github.issues;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.model.plain.Link;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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
        stubFor(
                get("/octocat/Hello-World/issues/3")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            { "url": "https://api.github.com/repos/octocat/Hello-World/issues/3",
                            "repository_url": "https://api.github.com/repos/octocat/Hello-World",
                            "labels_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/labels{/name}",
                             "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/comments",
                             "events_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/events",
                              "html_url": "https://github.com/octocat/Hello-World/pull/3", "id": 1023235, "node_id":
                               "MDExOlB1bGxSZXF1ZXN0MTc1MTQx", "number": 3, "title": "Edited README via GitHub",
                                "user": { "login": "shailendra75", "id": 831975, "node_id": "MDQ6VXNlcjgzMTk3NQ==",
                                "avatar_url": "https://avatars.githubusercontent.com/u/831975?v=4", "gravatar_id": "",
                                "url": "https://api.github.com/users/shailendra75", "html_url": "https://github.com/shailendra75",
                                "followers_url": "https://api.github.com/users/shailendra75/followers", "following_url":
                                "https://api.github.com/users/shailendra75/following{/other_user}", "gists_url":
                                "https://api.github.com/users/shailendra75/gists{/gist_id}", "starred_url":
                                "https://api.github.com/users/shailendra75/starred{/owner}{/repo}", "subscriptions_url":
                                "https://api.github.com/users/shailendra75/subscriptions", "organizations_url":
                                "https://api.github.com/users/shailendra75/orgs", "repos_url": "https://api.github.com/users/shailendra75/repos",
                                "events_url": "https://api.github.com/users/shailendra75/events{/privacy}", "received_events_url":
                                 "https://api.github.com/users/shailendra75/received_events", "type": "User", "user_view_type":
                                 "public", "site_admin": false }, "labels": [], "state": "closed", "locked": false, "assignee": null,
                                 "assignees": [], "milestone": null, "comments": 1, "created_at": "2011-06-08T10:58:32Z",
                                 "updated_at":"+999999999-12-31T23:59:59Z", "closed_at": "2011-06-08T10:58:32Z", "author_association":
                                 "NONE", "sub_issues_summary": { "total": 0, "completed": 0, "percent_completed": 0 },
                                 "active_lock_reason": null, "draft": false, "pull_request": { "url":
                                 "https://api.github.com/repos/octocat/Hello-World/pulls/3", "html_url":
                                 "https://github.com/octocat/Hello-World/pull/3", "diff_url":
                                 "https://github.com/octocat/Hello-World/pull/3.diff", "patch_url":
                                 "https://github.com/octocat/Hello-World/pull/3.patch", "merged_at": null },
                                 "body": "", "closed_by": { "login": "shailendra75", "id": 831975, "node_id":
                                 "MDQ6VXNlcjgzMTk3NQ==", "avatar_url": "https://avatars.githubusercontent.com/u/831975?v=4",
                                 "gravatar_id": "", "url": "https://api.github.com/users/shailendra75", "html_url":
                                 "https://github.com/shailendra75", "followers_url": "https://api.github.com/users/shailendra75/followers",
                                 "following_url": "https://api.github.com/users/shailendra75/following{/other_user}", "gists_url":
                                 "https://api.github.com/users/shailendra75/gists{/gist_id}", "starred_url":
                                 "https://api.github.com/users/shailendra75/starred{/owner}{/repo}", "subscriptions_url":
                                 "https://api.github.com/users/shailendra75/subscriptions", "organizations_url":
                                 "https://api.github.com/users/shailendra75/orgs", "repos_url":
                                 "https://api.github.com/users/shailendra75/repos", "events_url":
                                 "https://api.github.com/users/shailendra75/events{/privacy}", "received_events_url":
                                 "https://api.github.com/users/shailendra75/received_events", "type": "User",
                                 "user_view_type": "public", "site_admin": false }, "reactions": { "url":
                                 "https://api.github.com/repos/octocat/Hello-World/issues/3/reactions",
                                 "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0, "rocket": 0, "eyes": 0 },
                                  "timeline_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/timeline",
                                  "performed_via_github_app": null, "state_reason": null }
                        """)));

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
        stubFor(
                get("/octocat/Hello-World/issues/3")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                { "url": "https://api.github.com/repos/octocat/Hello-World/issues/3",
                                "repository_url": "https://api.github.com/repos/octocat/Hello-World",
                                "labels_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/labels{/name}",
                                "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/comments",
                                "events_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/events",
                                 "html_url": "https://github.com/octocat/Hello-World/pull/3", "id": 1023235,
                                 "node_id": "MDExOlB1bGxSZXF1ZXN0MTc1MTQx", "number": 3, "title": "Edited README via GitHub",
                                  "user": { "login": "shailendra75", "id": 831975, "node_id": "MDQ6VXNlcjgzMTk3NQ==",
                                   "avatar_url": "https://avatars.githubusercontent.com/u/831975?v=4", "gravatar_id": "",
                                    "url": "https://api.github.com/users/shailendra75", "html_url": "https://github.com/shailendra75",
                                     "followers_url": "https://api.github.com/users/shailendra75/followers",
                                     "following_url": "https://api.github.com/users/shailendra75/following{/other_user}",
                                      "gists_url": "https://api.github.com/users/shailendra75/gists{/gist_id}",
                                      "starred_url": "https://api.github.com/users/shailendra75/starred{/owner}{/repo}",
                                       "subscriptions_url": "https://api.github.com/users/shailendra75/subscriptions",
                                       "organizations_url": "https://api.github.com/users/shailendra75/orgs",
                                       "repos_url": "https://api.github.com/users/shailendra75/repos",
                                       "events_url": "https://api.github.com/users/shailendra75/events{/privacy}",
                                       "received_events_url": "https://api.github.com/users/shailendra75/received_events",
                                       "type": "User", "user_view_type": "public", "site_admin": false }, "labels": [],
                                       "state": "closed", "locked": false, "assignee": null, "assignees": [], "milestone": null,
                                       "comments": 1, "created_at": "2011-06-08T10:58:32Z", "updated_at": "2011-06-08T10:58:32Z",
                                       "closed_at": "2011-06-08T10:58:32Z", "author_association": "NONE", "sub_issues_summary":
                                       { "total": 0, "completed": 0, "percent_completed": 0 }, "active_lock_reason": null,
                                       "draft": false, "pull_request": { "url": "https://api.github.com/repos/octocat/Hello-World/pulls/3",
                                       "html_url": "https://github.com/octocat/Hello-World/pull/3", "diff_url":
                                       "https://github.com/octocat/Hello-World/pull/3.diff", "patch_url":
                                       "https://github.com/octocat/Hello-World/pull/3.patch", "merged_at": null },
                                       "body": "", "closed_by": { "login": "shailendra75", "id": 831975, "node_id":
                                       "MDQ6VXNlcjgzMTk3NQ==", "avatar_url": "https://avatars.githubusercontent.com/u/831975?v=4",
                                       "gravatar_id": "", "url": "https://api.github.com/users/shailendra75", "html_url":
                                       "https://github.com/shailendra75", "followers_url": "https://api.github.com/users/shailendra75/followers",
                                       "following_url": "https://api.github.com/users/shailendra75/following{/other_user}",
                                       "gists_url": "https://api.github.com/users/shailendra75/gists{/gist_id}", "starred_url":
                                       "https://api.github.com/users/shailendra75/starred{/owner}{/repo}", "subscriptions_url":
                                       "https://api.github.com/users/shailendra75/subscriptions", "organizations_url":
                                       "https://api.github.com/users/shailendra75/orgs", "repos_url": "https://api.github.com/users/shailendra75/repos",
                                       "events_url": "https://api.github.com/users/shailendra75/events{/privacy}", "received_events_url":
                                       "https://api.github.com/users/shailendra75/received_events", "type": "User", "user_view_type":
                                       "public", "site_admin": false }, "reactions": { "url":
                                       "https://api.github.com/repos/octocat/Hello-World/issues/3/reactions",
                                       "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0, "rocket": 0, "eyes": 0 },
                                        "timeline_url": "https://api.github.com/repos/octocat/Hello-World/issues/3/timeline",
                                        "performed_via_github_app": null, "state_reason": null }
                            """)));

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
        stubFor(
                get("/octocat/Hello-World/issues/3")
                        .willReturn(
                                aResponse()
                                        .withStatus(404)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                        {"message":"Not Found","documentation_url":"https://docs.github.com/rest/issues/issues#get-an-issue","status":"404"}
                        """)));

        List<String> updates = gitHubSingleIssueClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
