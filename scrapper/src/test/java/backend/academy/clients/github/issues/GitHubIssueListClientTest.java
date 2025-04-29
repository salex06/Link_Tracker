package backend.academy.clients.github.issues;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.dto.LinkUpdateInfo;
import backend.academy.model.plain.Link;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

@SpringBootTest
class GitHubIssueListClientTest {
    private int port;

    @Autowired
    private static RestClient restClient;

    @Autowired
    private RetryTemplate retryTemplate;

    private WireMockServer wireMockServer;

    private static GitHubIssueListClient gitHubIssueListClient;

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

    @BeforeAll
    public static void setUp() {
        restClient = RestClient.create();
    }

    @Test
    void getUpdates_WhenNewComment_ThenReturnUpdateMessage() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubIssueListClient = new GitHubIssueListClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues"),
                restClient,
                retryTemplate);

        String expectedMessage1 = String.format(
                """
            Новый issue new issue%nАвтор: salex06%nВремя создания: 31-12-+999999999 23:59 (UTC)%nПревью: issue description""");
        String expectedMessage2 = String.format(
                """
            Новый комментарий к issue README file modified %nАвтор: mattstifanelli%nВремя создания: 31-12-+999999999 23:59 (UTC)%nПревью: Let's try again via Issue tacker...
            """);
        String expectedMessage3 = String.format(
                """
            Новый комментарий к issue Edited README via GitHub%nАвтор: masonzou%nВремя создания: 31-12-+999999999 23:59 (UTC)%nПревью: test
            """);
        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues");
        stubFor(get("/octocat/Hello-World/issues")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                [
                                    {
                                        "url": "https://api.github.com/repos/salex06/testrepo/issues/5",
                                        "repository_url": "https://api.github.com/repos/salex06/testrepo",
                                        "labels_url": "https://api.github.com/repos/salex06/testrepo/issues/5/labels{/name}",
                                        "comments_url": "http://localhost:%d/octocat/Hello-World/issues/5/comments",
                                        "events_url": "https://api.github.com/repos/salex06/testrepo/issues/5/events",
                                        "html_url": "https://github.com/salex06/testrepo/issues/5", "id": 2905544102,
                                        "node_id": "I_kwDON6S4cc6tLxWm", "number": 5, "title": "new issue", "user": { "login":
                                        "salex06", "id": 180034077, "node_id": "U_kgDOCrsaHQ", "avatar_url":
                                        "https://avatars.githubusercontent.com/u/180034077?v=4", "gravatar_id": "", "url":
                                        "https://api.github.com/users/salex06", "html_url": "https://github.com/salex06",
                                        "followers_url": "https://api.github.com/users/salex06/followers", "following_url":
                                        "https://api.github.com/users/salex06/following{/other_user}", "gists_url":
                                        "https://api.github.com/users/salex06/gists{/gist_id}", "starred_url":
                                        "https://api.github.com/users/salex06/starred{/owner}{/repo}", "subscriptions_url":
                                        "https://api.github.com/users/salex06/subscriptions", "organizations_url":
                                        "https://api.github.com/users/salex06/orgs", "repos_url":
                                        "https://api.github.com/users/salex06/repos", "events_url":
                                        "https://api.github.com/users/salex06/events{/privacy}", "received_events_url":
                                        "https://api.github.com/users/salex06/received_events", "type": "User", "user_view_type":
                                        "public", "site_admin": false }, "labels": [], "state": "open", "locked": false,
                                        "assignee": null, "assignees": [], "milestone": null, "comments": 0, "created_at":
                                        "+999999999-12-31T23:59:59Z", "updated_at": "2025-03-09T16:57:04Z", "closed_at": null,
                                        "author_association": "OWNER", "sub_issues_summary": { "total": 0, "completed": 0,
                                        "percent_completed": 0 }, "active_lock_reason": null, "body": "issue description",
                                        "closed_by": null, "reactions": { "url":
                                        "https://api.github.com/repos/salex06/testrepo/issues/5/reactions", "total_count": 0,
                                        "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0, "rocket": 0,
                                        "eyes": 0 }, "timeline_url":
                                        "https://api.github.com/repos/salex06/testrepo/issues/5/timeline",
                                        "performed_via_github_app": null, "state_reason": null
                                    }
                                ]
                                """
                                        .formatted(port))));

        stubFor(get("/octocat/Hello-World/issues/5/comments")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                [
                                    {
                                        "url": "https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825",
                                        "html_url": "https://github.com/octocat/Hello-World/pull/2#issuecomment-1146825",
                                        "issue_url": "http://localhost:%d/repos/octocat/Hello-World/issues/2", "id": 1146825,
                                        "node_id": "MDEyOklzc3VlQ29tbWVudDExNDY4MjU=", "user": { "login": "mattstifanelli",
                                        "id": 783382, "node_id": "MDQ6VXNlcjc4MzM4Mg==", "avatar_url":
                                        "https://avatars.githubusercontent.com/u/783382?v=4", "gravatar_id": "", "url":
                                        "https://api.github.com/users/mattstifanelli", "html_url": "https://github.com/mattstifanelli",
                                        "followers_url": "https://api.github.com/users/mattstifanelli/followers", "following_url":
                                        "https://api.github.com/users/mattstifanelli/following{/other_user}", "gists_url":
                                        "https://api.github.com/users/mattstifanelli/gists{/gist_id}", "starred_url":
                                        "https://api.github.com/users/mattstifanelli/starred{/owner}{/repo}", "subscriptions_url":
                                        "https://api.github.com/users/mattstifanelli/subscriptions", "organizations_url":
                                        "https://api.github.com/users/mattstifanelli/orgs", "repos_url":
                                        "https://api.github.com/users/mattstifanelli/repos", "events_url":
                                        "https://api.github.com/users/mattstifanelli/events{/privacy}",
                                        "received_events_url": "https://api.github.com/users/mattstifanelli/received_events",
                                        "type": "User", "user_view_type": "public", "site_admin": false },
                                        "created_at": "+999999999-12-31T23:59:59Z", "updated_at": "2011-05-12T14:34:22Z",
                                        "author_association": "NONE", "body": "Let's try again via Issue tacker...",
                                        "reactions": { "url":
                                        "https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825/reactions",
                                        "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                        "rocket": 0, "eyes": 0 }, "performed_via_github_app": null }, { "url":
                                        "https://api.github.com/repos/octocat/Hello-World/issues/comments/1325876",
                                        "html_url": "https://github.com/octocat/Hello-World/pull/3#issuecomment-1325876",
                                        "issue_url": "https://api.github.com/repos/octocat/Hello-World/issues/3", "id": 1325876,
                                        "node_id": "MDEyOklzc3VlQ29tbWVudDEzMjU4NzY=", "user": { "login": "shailendra75",
                                        "id": 831975, "node_id": "MDQ6VXNlcjgzMTk3NQ==", "avatar_url":
                                        "https://avatars.githubusercontent.com/u/831975?v=4", "gravatar_id": "", "url":
                                        "https://api.github.com/users/shailendra75", "html_url": "https://github.com/shailendra75",
                                        "followers_url": "https://api.github.com/users/shailendra75/followers", "following_url":
                                        "https://api.github.com/users/shailendra75/following{/other_user}", "gists_url":
                                        "https://api.github.com/users/shailendra75/gists{/gist_id}", "starred_url":
                                        "https://api.github.com/users/shailendra75/starred{/owner}{/repo}", "subscriptions_url":
                                        "https://api.github.com/users/shailendra75/subscriptions", "organizations_url":
                                        "https://api.github.com/users/shailendra75/orgs", "repos_url":
                                        "https://api.github.com/users/shailendra75/repos", "events_url":
                                        "https://api.github.com/users/shailendra75/events{/privacy}", "received_events_url":
                                        "https://api.github.com/users/shailendra75/received_events", "type": "User",
                                        "user_view_type": "public", "site_admin": false }, "created_at": "2011-06-08T10:58:32Z",
                                        "updated_at": "2011-06-08T10:58:32Z", "author_association": "NONE", "body":
                                        "getting famlirized with git", "reactions": { "url":
                                        "https://api.github.com/repos/octocat/Hello-World/issues/comments/1325876/reactions",
                                        "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                        "rocket": 0, "eyes": 0 }, "performed_via_github_app": null }, { "url":
                                        "https://api.github.com/repos/octocat/Hello-World/issues/comments/1340258",
                                        "html_url": "https://github.com/octocat/Hello-World/pull/1#issuecomment-1340258",
                                        "issue_url": "http://localhost:%d/repos/octocat/Hello-World/issues/1", "id": 1340258,
                                        "node_id": "MDEyOklzc3VlQ29tbWVudDEzNDAyNTg=", "user": { "login": "masonzou", "id": 841296,
                                        "node_id": "MDQ6VXNlcjg0MTI5Ng==", "avatar_url": "https://avatars.githubusercontent.com/u/841296?v=4",
                                        "gravatar_id": "", "url": "https://api.github.com/users/masonzou", "html_url":
                                        "https://github.com/masonzou", "followers_url": "https://api.github.com/users/masonzou/followers",
                                        "following_url": "https://api.github.com/users/masonzou/following{/other_user}", "gists_url":
                                        "https://api.github.com/users/masonzou/gists{/gist_id}", "starred_url":
                                        "https://api.github.com/users/masonzou/starred{/owner}{/repo}", "subscriptions_url":
                                        "https://api.github.com/users/masonzou/subscriptions", "organizations_url":
                                        "https://api.github.com/users/masonzou/orgs",
                                        "repos_url": "https://api.github.com/users/masonzou/repos", "events_url":
                                        "https://api.github.com/users/masonzou/events{/privacy}", "received_events_url":
                                        "https://api.github.com/users/masonzou/received_events", "type": "User",
                                        "user_view_type": "public", "site_admin": false }, "created_at":
                                        "+999999999-12-31T23:59:59Z", "updated_at": "2011-06-10T07:30:27Z",
                                        "author_association": "NONE", "body": "test", "reactions": { "url":
                                        "https://api.github.com/repos/octocat/Hello-World/issues/comments/1340258/reactions",
                                        "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                        "rocket": 0, "eyes": 0 }, "performed_via_github_app": null
                                    }
                                ]
                                """
                                        .formatted(port, port))));

        stubFor(
                get("/repos/octocat/Hello-World/issues/2")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "url": "https://api.github.com/repos/octocat/Hello-World/issues/2",
                                    "repository_url": "https://api.github.com/repos/octocat/Hello-World",
                                    "labels_url": "https://api.github.com/repos/octocat/Hello-World/issues/2/labels{/name}",
                                    "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/2/comments",
                                    "events_url": "https://api.github.com/repos/octocat/Hello-World/issues/2/events",
                                    "html_url": "https://github.com/octocat/Hello-World/pull/2",
                                    "id": 889727,
                                    "node_id": "MDExOlB1bGxSZXF1ZXN0MTQ0NjQ3",
                                    "number": 2,
                                    "title": "README file modified ",
                                    "user": {
                                        "login": "mattstifanelli",
                                        "id": 783382,
                                        "node_id": "MDQ6VXNlcjc4MzM4Mg==",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/783382?v=4",
                                        "gravatar_id": "",
                                        "url": "https://api.github.com/users/mattstifanelli",
                                        "html_url": "https://github.com/mattstifanelli",
                                        "followers_url": "https://api.github.com/users/mattstifanelli/followers",
                                        "following_url": "https://api.github.com/users/mattstifanelli/following{/other_user}",
                                        "gists_url": "https://api.github.com/users/mattstifanelli/gists{/gist_id}",
                                        "starred_url": "https://api.github.com/users/mattstifanelli/starred{/owner}{/repo}",
                                        "subscriptions_url": "https://api.github.com/users/mattstifanelli/subscriptions",
                                        "organizations_url": "https://api.github.com/users/mattstifanelli/orgs",
                                        "repos_url": "https://api.github.com/users/mattstifanelli/repos",
                                        "events_url": "https://api.github.com/users/mattstifanelli/events{/privacy}",
                                        "received_events_url": "https://api.github.com/users/mattstifanelli/received_events",
                                        "type": "User",
                                        "user_view_type": "public",
                                        "site_admin": false
                                    },
                                    "labels": [],
                                    "state": "closed",
                                    "locked": false,
                                    "assignee": null,
                                    "assignees": [],
                                    "milestone": null,
                                    "comments": 1,
                                    "created_at": "2011-05-12T14:32:47Z",
                                    "updated_at": "2021-05-03T10:48:20Z",
                                    "closed_at": "2011-05-12T14:34:22Z",
                                    "author_association": "NONE",
                                    "sub_issues_summary": {
                                        "total": 0,
                                        "completed": 0,
                                        "percent_completed": 0
                                    },
                                    "active_lock_reason": null,
                                    "draft": false,
                                    "pull_request": {
                                        "url": "https://api.github.com/repos/octocat/Hello-World/pulls/2",
                                        "html_url": "https://github.com/octocat/Hello-World/pull/2",
                                        "diff_url": "https://github.com/octocat/Hello-World/pull/2.diff",
                                        "patch_url": "https://github.com/octocat/Hello-World/pull/2.patch",
                                        "merged_at": null
                                    },
                                    "body": "My first pull request on GitHub! Yep!\\n",
                                    "closed_by": {
                                        "login": "mattstifanelli",
                                        "id": 783382,
                                        "node_id": "MDQ6VXNlcjc4MzM4Mg==",
                                        "avatar_url": "https://avatars.githubusercontent.com/u/783382?v=4",
                                        "gravatar_id": "",
                                        "url": "https://api.github.com/users/mattstifanelli",
                                        "html_url": "https://github.com/mattstifanelli",
                                        "followers_url": "https://api.github.com/users/mattstifanelli/followers",
                                        "following_url": "https://api.github.com/users/mattstifanelli/following{/other_user}",
                                        "gists_url": "https://api.github.com/users/mattstifanelli/gists{/gist_id}",
                                        "starred_url": "https://api.github.com/users/mattstifanelli/starred{/owner}{/repo}",
                                        "subscriptions_url": "https://api.github.com/users/mattstifanelli/subscriptions",
                                        "organizations_url": "https://api.github.com/users/mattstifanelli/orgs",
                                        "repos_url": "https://api.github.com/users/mattstifanelli/repos",
                                        "events_url": "https://api.github.com/users/mattstifanelli/events{/privacy}",
                                        "received_events_url": "https://api.github.com/users/mattstifanelli/received_events",
                                        "type": "User",
                                        "user_view_type": "public",
                                        "site_admin": false
                                    },
                                    "reactions": {
                                        "url": "https://api.github.com/repos/octocat/Hello-World/issues/2/reactions",
                                        "total_count": 0,
                                        "+1": 0,
                                        "-1": 0,
                                        "laugh": 0,
                                        "hooray": 0,
                                        "confused": 0,
                                        "heart": 0,
                                        "rocket": 0,
                                        "eyes": 0
                                    },
                                    "timeline_url": "https://api.github.com/repos/octocat/Hello-World/issues/2/timeline",
                                    "performed_via_github_app": null,
                                    "state_reason": null
                                }
            """)));

        stubFor(
                get("/repos/octocat/Hello-World/issues/1")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            {
                                "url": "https://api.github.com/repos/octocat/Hello-World/issues/1",
                                "repository_url": "https://api.github.com/repos/octocat/Hello-World",
                                "labels_url": "https://api.github.com/repos/octocat/Hello-World/issues/1/labels{/name}",
                                "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/1/comments",
                                "events_url": "https://api.github.com/repos/octocat/Hello-World/issues/1/events",
                                "html_url": "https://github.com/octocat/Hello-World/pull/1",
                                "id": 872858,
                                "node_id": "MDExOlB1bGxSZXF1ZXN0MTQwOTAw",
                                "number": 1,
                                "title": "Edited README via GitHub",
                                "user": {
                                    "login": "unoju",
                                    "id": 777449,
                                    "node_id": "MDQ6VXNlcjc3NzQ0OQ==",
                                    "avatar_url": "https://avatars.githubusercontent.com/u/777449?v=4",
                                    "gravatar_id": "",
                                    "url": "https://api.github.com/users/unoju",
                                    "html_url": "https://github.com/unoju",
                                    "followers_url": "https://api.github.com/users/unoju/followers",
                                    "following_url": "https://api.github.com/users/unoju/following{/other_user}",
                                    "gists_url": "https://api.github.com/users/unoju/gists{/gist_id}",
                                    "starred_url": "https://api.github.com/users/unoju/starred{/owner}{/repo}",
                                    "subscriptions_url": "https://api.github.com/users/unoju/subscriptions",
                                    "organizations_url": "https://api.github.com/users/unoju/orgs",
                                    "repos_url": "https://api.github.com/users/unoju/repos",
                                    "events_url": "https://api.github.com/users/unoju/events{/privacy}",
                                    "received_events_url": "https://api.github.com/users/unoju/received_events",
                                    "type": "User",
                                    "user_view_type": "public",
                                    "site_admin": false
                                },
                                "labels": [],
                                "state": "closed",
                                "locked": false,
                                "assignee": null,
                                "assignees": [],
                                "milestone": null,
                                "comments": 57,
                                "created_at": "2011-05-09T19:10:00Z",
                                "updated_at": "2025-03-28T16:06:05Z",
                                "closed_at": "2011-05-18T20:00:25Z",
                                "author_association": "NONE",
                                "sub_issues_summary": {
                                    "total": 0,
                                    "completed": 0,
                                    "percent_completed": 0
                                },
                                "active_lock_reason": null,
                                "draft": false,
                                "pull_request": {
                                    "url": "https://api.github.com/repos/octocat/Hello-World/pulls/1",
                                    "html_url": "https://github.com/octocat/Hello-World/pull/1",
                                    "diff_url": "https://github.com/octocat/Hello-World/pull/1.diff",
                                    "patch_url": "https://github.com/octocat/Hello-World/pull/1.patch",
                                    "merged_at": null
                                },
                                "body": "",
                                "closed_by": {
                                    "login": "octocat",
                                    "id": 583231,
                                    "node_id": "MDQ6VXNlcjU4MzIzMQ==",
                                    "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                                    "gravatar_id": "",
                                    "url": "https://api.github.com/users/octocat",
                                    "html_url": "https://github.com/octocat",
                                    "followers_url": "https://api.github.com/users/octocat/followers",
                                    "following_url": "https://api.github.com/users/octocat/following{/other_user}",
                                    "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
                                    "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
                                    "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
                                    "organizations_url": "https://api.github.com/users/octocat/orgs",
                                    "repos_url": "https://api.github.com/users/octocat/repos",
                                    "events_url": "https://api.github.com/users/octocat/events{/privacy}",
                                    "received_events_url": "https://api.github.com/users/octocat/received_events",
                                    "type": "User",
                                    "user_view_type": "public",
                                    "site_admin": false
                                },
                                "reactions": {
                                    "url": "https://api.github.com/repos/octocat/Hello-World/issues/1/reactions",
                                    "total_count": 5,
                                    "+1": 0,
                                    "-1": 0,
                                    "laugh": 0,
                                    "hooray": 0,
                                    "confused": 0,
                                    "heart": 1,
                                    "rocket": 4,
                                    "eyes": 0
                                },
                                "timeline_url": "https://api.github.com/repos/octocat/Hello-World/issues/1/timeline",
                                "performed_via_github_app": null,
                                "state_reason": null
                            }
                            """)));

        List<LinkUpdateInfo> updates = gitHubIssueListClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.size()).isEqualTo(3);
        assertThat(updates.get(0).commonInfo().trim()).isEqualTo(expectedMessage1.trim());
        assertThat(updates.get(1).commonInfo().trim()).isEqualTo(expectedMessage2.trim());
        assertThat(updates.get(2).commonInfo().trim()).isEqualTo(expectedMessage3.trim());
    }

    @Test
    void getUpdates_WhenNoNewComments_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubIssueListClient = new GitHubIssueListClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues"),
                restClient,
                retryTemplate);

        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues");
        stubFor(get("/octocat/Hello-World/issues")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                """
                                [
                                { "url": "https://api.github.com/repos/salex06/testrepo/issues/5",
                                "repository_url": "https://api.github.com/repos/salex06/testrepo",
                                "labels_url": "https://api.github.com/repos/salex06/testrepo/issues/5/labels{/name}",
                                "comments_url": "http://localhost:%d/octocat/Hello-World/issues/5/comments",
                                "events_url": "https://api.github.com/repos/salex06/testrepo/issues/5/events",
                                "html_url": "https://github.com/salex06/testrepo/issues/5", "id": 2905544102, "node_id":
                                "I_kwDON6S4cc6tLxWm", "number": 5, "title": "new issue", "user": { "login": "salex06",
                                "id": 180034077, "node_id": "U_kgDOCrsaHQ", "avatar_url":
                                "https://avatars.githubusercontent.com/u/180034077?v=4", "gravatar_id": "", "url":
                                "https://api.github.com/users/salex06", "html_url": "https://github.com/salex06",
                                "followers_url": "https://api.github.com/users/salex06/followers", "following_url":
                                "https://api.github.com/users/salex06/following{/other_user}", "gists_url":
                                "https://api.github.com/users/salex06/gists{/gist_id}", "starred_url":
                                "https://api.github.com/users/salex06/starred{/owner}{/repo}", "subscriptions_url":
                                "https://api.github.com/users/salex06/subscriptions", "organizations_url":
                                "https://api.github.com/users/salex06/orgs", "repos_url":
                                "https://api.github.com/users/salex06/repos", "events_url":
                                "https://api.github.com/users/salex06/events{/privacy}",
                                "received_events_url": "https://api.github.com/users/salex06/received_events",
                                "type": "User", "user_view_type": "public", "site_admin": false }, "labels": [],
                                "state": "open", "locked": false, "assignee": null, "assignees": [], "milestone": null,
                                "comments": 0, "created_at": "0001-01-01T00:00:00Z",
                                "updated_at": "2025-03-09T16:57:04Z", "closed_at": null, "author_association":
                                "OWNER", "sub_issues_summary": { "total": 0, "completed": 0, "percent_completed": 0 },
                                "active_lock_reason": null, "body": "issue description", "closed_by": null, "reactions":
                                { "url": "https://api.github.com/repos/salex06/testrepo/issues/5/reactions",
                                "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                "rocket": 0, "eyes": 0 },
                                "timeline_url": "https://api.github.com/repos/salex06/testrepo/issues/5/timeline",
                                "performed_via_github_app": null, "state_reason": null } ]
                                """
                                        .formatted(port))));

        stubFor(
                get("/octocat/Hello-World/issues/5/comments")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                [
                                {
                                "url": "https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825",
                                "html_url": "https://github.com/octocat/Hello-World/pull/2#issuecomment-1146825",
                                "issue_url": "https://api.github.com/repos/octocat/Hello-World/issues/2", "id": 1146825,
                                "node_id": "MDEyOklzc3VlQ29tbWVudDExNDY4MjU=", "user": { "login": "mattstifanelli",
                                "id": 783382, "node_id": "MDQ6VXNlcjc4MzM4Mg==", "avatar_url":
                                "https://avatars.githubusercontent.com/u/783382?v=4", "gravatar_id": "", "url":
                                "https://api.github.com/users/mattstifanelli", "html_url":
                                "https://github.com/mattstifanelli", "followers_url":
                                "https://api.github.com/users/mattstifanelli/followers", "following_url":
                                "https://api.github.com/users/mattstifanelli/following{/other_user}", "gists_url":
                                "https://api.github.com/users/mattstifanelli/gists{/gist_id}", "starred_url":
                                "https://api.github.com/users/mattstifanelli/starred{/owner}{/repo}",
                                "subscriptions_url": "https://api.github.com/users/mattstifanelli/subscriptions",
                                "organizations_url": "https://api.github.com/users/mattstifanelli/orgs", "repos_url":
                                "https://api.github.com/users/mattstifanelli/repos", "events_url":
                                "https://api.github.com/users/mattstifanelli/events{/privacy}", "received_events_url":
                                "https://api.github.com/users/mattstifanelli/received_events", "type": "User",
                                "user_view_type": "public", "site_admin": false }, "created_at": "0001-01-01T00:00:00Z",
                                "updated_at": "2011-05-12T14:34:22Z", "author_association": "NONE", "body":
                                "Let's try again via Issue tacker...", "reactions": { "url":
                                "https://api.github.com/repos/octocat/Hello-World/issues/comments/1146825/reactions",
                                "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                "rocket": 0, "eyes": 0 }, "performed_via_github_app": null }, { "url":
                                "https://api.github.com/repos/octocat/Hello-World/issues/comments/1325876", "html_url":
                                "https://github.com/octocat/Hello-World/pull/3#issuecomment-1325876", "issue_url":
                                "https://api.github.com/repos/octocat/Hello-World/issues/3", "id": 1325876, "node_id":
                                "MDEyOklzc3VlQ29tbWVudDEzMjU4NzY=", "user": { "login": "shailendra75", "id": 831975,
                                "node_id": "MDQ6VXNlcjgzMTk3NQ==", "avatar_url":
                                "https://avatars.githubusercontent.com/u/831975?v=4", "gravatar_id": "", "url":
                                "https://api.github.com/users/shailendra75",
                                "html_url": "https://github.com/shailendra75", "followers_url":
                                "https://api.github.com/users/shailendra75/followers", "following_url":
                                "https://api.github.com/users/shailendra75/following{/other_user}", "gists_url":
                                "https://api.github.com/users/shailendra75/gists{/gist_id}", "starred_url":
                                "https://api.github.com/users/shailendra75/starred{/owner}{/repo}", "subscriptions_url":
                                "https://api.github.com/users/shailendra75/subscriptions", "organizations_url":
                                "https://api.github.com/users/shailendra75/orgs", "repos_url":
                                "https://api.github.com/users/shailendra75/repos", "events_url":
                                "https://api.github.com/users/shailendra75/events{/privacy}", "received_events_url":
                                "https://api.github.com/users/shailendra75/received_events", "type": "User",
                                "user_view_type": "public", "site_admin": false }, "created_at": "2011-06-08T10:58:32Z",
                                "updated_at": "2011-06-08T10:58:32Z", "author_association": "NONE", "body":
                                "getting famlirized with git", "reactions": { "url":
                                "https://api.github.com/repos/octocat/Hello-World/issues/comments/1325876/reactions",
                                "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                "rocket": 0, "eyes": 0 }, "performed_via_github_app": null }, { "url":
                                "https://api.github.com/repos/octocat/Hello-World/issues/comments/1340258",
                                "html_url": "https://github.com/octocat/Hello-World/pull/1#issuecomment-1340258",
                                "issue_url": "https://api.github.com/repos/octocat/Hello-World/issues/1",
                                "id": 1340258, "node_id": "MDEyOklzc3VlQ29tbWVudDEzNDAyNTg=", "user": { "login":
                                "masonzou", "id": 841296, "node_id": "MDQ6VXNlcjg0MTI5Ng==", "avatar_url":
                                "https://avatars.githubusercontent.com/u/841296?v=4", "gravatar_id": "", "url":
                                "https://api.github.com/users/masonzou", "html_url": "https://github.com/masonzou",
                                "followers_url": "https://api.github.com/users/masonzou/followers", "following_url":
                                "https://api.github.com/users/masonzou/following{/other_user}", "gists_url":
                                "https://api.github.com/users/masonzou/gists{/gist_id}", "starred_url":
                                "https://api.github.com/users/masonzou/starred{/owner}{/repo}", "subscriptions_url":
                                "https://api.github.com/users/masonzou/subscriptions", "organizations_url":
                                "https://api.github.com/users/masonzou/orgs", "repos_url":
                                "https://api.github.com/users/masonzou/repos", "events_url":
                                "https://api.github.com/users/masonzou/events{/privacy}", "received_events_url":
                                "https://api.github.com/users/masonzou/received_events", "type": "User",
                                "user_view_type": "public", "site_admin": false }, "created_at": "0001-01-01T00:00:00Z",
                                "updated_at": "2011-06-10T07:30:27Z", "author_association": "NONE", "body": "test",
                                "reactions": { "url":
                                "https://api.github.com/repos/octocat/Hello-World/issues/comments/1340258/reactions",
                                "total_count": 0, "+1": 0, "-1": 0, "laugh": 0, "hooray": 0, "confused": 0, "heart": 0,
                                "rocket": 0, "eyes": 0 }, "performed_via_github_app": null }]
                                """)));

        List<LinkUpdateInfo> updates = gitHubIssueListClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }

    @Test
    void getUpdates_WhenBadRequest_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        gitHubIssueListClient = new GitHubIssueListClient(
                x -> String.format("http://localhost:" + port + "/octocat/Hello-World/issues"),
                restClient,
                retryTemplate);

        Link link = new Link(1L, "https://github.com/octocat/Hello-World/issues");
        stubFor(get("/octocat/Hello-World/issues")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("bad request")));

        List<LinkUpdateInfo> updates = gitHubIssueListClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
