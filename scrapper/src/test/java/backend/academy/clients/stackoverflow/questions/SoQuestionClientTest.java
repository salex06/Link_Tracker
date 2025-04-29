package backend.academy.clients.stackoverflow.questions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
class SoQuestionClientTest {
    private int port = 8090;

    @Autowired
    private static RestClient restClient;

    @Autowired
    private RetryTemplate retryTemplate;

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
        soQuestionClient = new SoQuestionClient(
                x -> String.format("http://localhost:" + port + "/questions/6031003"), restClient, retryTemplate);

        String expectedMessage1 = String.format(
                """
                Новый комментарий к вопросу How do I parse a string to a float or int?%nАвтор: InfiniteStack%nВремя создания: 08-02-2277 13:34 (UTC)%nПревью: Also make sure the string is actually can be converted to float[,](https://semicolon.dev/stackoverflow/question/8/how-to-convert-a-string-to-a-float-in-python) one way of doing that is to write a cust""");
        String expectedMessage2 = String.format(
                """
                Новый ответ к вопросу How do I parse a string to a float or int?%nАвтор: Blaze%nВремя создания: 01-01-2278 11:57 (UTC)%nПревью: <p>These two functions can encode <strong>any</strong> string to a big number and vice versa</p>
                <pre><code>alphabet = '0123456789abcdefghijklmnopqrstuvwxyz'

                def string_to_int(string):
                    intstring""");
        String expectedMessage3 = String.format(
                """
                Новый комментарий к вопросу How do I parse a string to a float or int?%nАвтор: Dendi Handian%nВремя создания: 02-09-2275 21:40 (UTC)%nПревью: @ibnɘꟻ I&#39;m aware of it, but the `-&gt;format()` gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t""");
        String expectedMessage4 = String.format(
                """
                Новый комментарий к вопросу How do I parse a string to a float or int?%nАвтор: Joseph Prosper%nВремя создания: 15-10-2277 19:53 (UTC)%nПревью: Body example""");
        Link link = new Link(1L, "https://stackoverflow.com/questions/6031003");
        stubFor(
                get("/questions/6031003?site=stackoverflow")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "items": [
                                        {
                                            "owner": {
                                                "account_id": 14717,
                                                "reputation": 69398,
                                                "user_id": 30529,
                                                "user_type": "registered",
                                                "accept_rate": 94,
                                                "profile_image": "https://www.gravatar.com/avatar/f3a3491c1c83e8c62151752162184627?s=256&d=identicon&r=PG",
                                                "display_name": "Tristan Havelick",
                                                "link": "https://stackoverflow.com/users/30529/tristan-havelick"
                                            },
                                            "score": 2764,
                                            "last_edit_date": 1700761208,
                                            "last_activity_date": 1720472120,
                                            "creation_date": 1229651546,
                                            "post_type": "question",
                                            "post_id": 379906,
                                            "title": "How do I parse a string to a float or int?",
                                            "content_license": "CC BY-SA 4.0",
                                            "link": "https://stackoverflow.com/q/379906"
                                        }
                                    ]
                                }""")));

        stubFor(
                get("/questions/6031003/answers?site=stackoverflow&filter=!nNPvSNe7D9")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "items": [
                                        {
                                            "owner": {
                                                "account_id": 809,
                                                "reputation": 182116,
                                                "user_id": 1057,
                                                "user_type": "registered",
                                                "accept_rate": 80,
                                                "profile_image": "https://www.gravatar.com/avatar/e5778b659d144e38ed982c3f4e566089?s=256&d=identicon&r=PG",
                                                "display_name": "Harley Holcombe",
                                                "link": "https://stackoverflow.com/users/1057/harley-holcombe"
                                            },
                                            "is_accepted": true,
                                            "score": 3100,
                                            "last_activity_date": 1720472120,
                                            "last_edit_date": 1720472120,
                                            "creation_date": 1229651691,
                                            "answer_id": 379910,
                                            "question_id": 379906,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "    &gt;&gt;&gt; a = &quot;545.2222&quot;\\r\\n    &gt;&gt;&gt; float(a)\\r\\n    545.22220000000004\\r\\n    &gt;&gt;&gt; int(float(a))\\r\\n    545",
                                            "body": "<pre><code>&gt;&gt;&gt; a = &quot;545.2222&quot;\\n&gt;&gt;&gt; float(a)\\n545.22220000000004\\n&gt;&gt;&gt; int(float(a))\\n545\\n</code></pre>\\n"
                                        },
                                        {
                                            "owner": {
                                                "account_id": 6915203,
                                                "reputation": 939,
                                                "user_id": 5468048,
                                                "user_type": "registered",
                                                "profile_image": "https://www.gravatar.com/avatar/2b065a7ccc825411060cd6bde87ebb66?s=256&d=identicon&r=PG&f=y&so-version=2",
                                                "display_name": "Blaze",
                                                "link": "https://stackoverflow.com/users/5468048/blaze"
                                            },
                                            "is_accepted": false,
                                            "score": 0,
                                            "last_activity_date": 1719612194,
                                            "last_edit_date": 1719612194,
                                            "creation_date": 9719611053,
                                            "answer_id": 78684527,
                                            "question_id": 379906,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "These two functions can encode **any** string to a big number and vice versa\\r\\n\\r\\n    alphabet = &#39;0123456789abcdefghijklmnopqrstuvwxyz&#39;\\r\\n\\r\\n    def string_to_int(string):\\r\\n        intstring = []\\r\\n        for i in range(len(string)):\\r\\n            n = int(string[i], 36)\\r\\n            sn = str(n)\\r\\n            if len(sn) == 1:\\r\\n                intstring.append(&#39;0&#39;)\\r\\n            intstring.append(sn)\\r\\n        return int(&#39;&#39;.join(intstring))\\r\\n    \\r\\n    \\r\\n    def int_to_string(integer):\\r\\n        global alphabet\\r\\n        string = str(integer)\\r\\n        result = []\\r\\n        for i in range(0, len(string), 2):\\r\\n            c1 = string[i]\\r\\n            c2 = &#39;&#39;\\r\\n            if len(string) &gt;= i:\\r\\n                c2 = string[i + 1]\\r\\n            code = int(c1 + c2)\\r\\n            result.append(alphabet[code])\\r\\n        return &#39;&#39;.join(result)\\r\\n\\r\\nTest them with this print\\r\\n\\r\\n    print(int_to_string(string_to_int(&#39;apple12345&#39;)))",
                                            "body": "<p>These two functions can encode <strong>any</strong> string to a big number and vice versa</p>\\n<pre><code>alphabet = '0123456789abcdefghijklmnopqrstuvwxyz'\\n\\ndef string_to_int(string):\\n    intstring = []\\n    for i in range(len(string)):\\n        n = int(string[i], 36)\\n        sn = str(n)\\n        if len(sn) == 1:\\n            intstring.append('0')\\n        intstring.append(sn)\\n    return int(''.join(intstring))\\n\\n\\ndef int_to_string(integer):\\n    global alphabet\\n    string = str(integer)\\n    result = []\\n    for i in range(0, len(string), 2):\\n        c1 = string[i]\\n        c2 = ''\\n        if len(string) &gt;= i:\\n            c2 = string[i + 1]\\n        code = int(c1 + c2)\\n        result.append(alphabet[code])\\n    return ''.join(result)\\n</code></pre>\\n<p>Test them with this print</p>\\n<pre><code>print(int_to_string(string_to_int('apple12345')))\\n</code></pre>\\n"
                                        }]}""")));

        stubFor(
                get("/questions/6031003/comments?site=stackoverflow&filter=!nNPvSN_LI9")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "items": [
                                        {
                                            "owner": {
                                                "account_id": 1292874,
                                                "reputation": 458,
                                                "user_id": 1244611,
                                                "user_type": "registered",
                                                "accept_rate": 0,
                                                "profile_image": "https://www.gravatar.com/avatar/e5b6a8e239f3eba9cd5855596a77a0b5?s=256&d=identicon&r=PG",
                                                "display_name": "InfiniteStack",
                                                "link": "https://stackoverflow.com/users/1244611/infinitestack"
                                            },
                                            "edited": false,
                                            "score": 0,
                                            "creation_date": 9691364055,
                                            "post_id": 379906,
                                            "comment_id": 135477565,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "Also make sure the string is actually can be converted to float[,](https://semicolon.dev/stackoverflow/question/8/how-to-convert-a-string-to-a-float-in-python) one way of doing that is to write a custom function with a try/except block, that checks for `return float(str_value)` inside try scope.",
                                            "body": "Also make sure the string is actually can be converted to float<a href=\\"https://semicolon.dev/stackoverflow/question/8/how-to-convert-a-string-to-a-float-in-python\\" rel=\\"nofollow noreferrer\\">,</a> one way of doing that is to write a custom function with a try/except block, that checks for <code>return float(str_value)</code> inside try scope."
                                        },
                                        {
                                            "owner": {
                                                "account_id": 117351,
                                                "reputation": 722,
                                                "user_id": 307363,
                                                "user_type": "registered",
                                                "accept_rate": 100,
                                                "profile_image": "https://www.gravatar.com/avatar/e8a78675b2e22a5eda938f7146af689c?s=256&d=identicon&r=PG",
                                                "display_name": "robertlayton",
                                                "link": "https://stackoverflow.com/users/307363/robertlayton"
                                            },
                                            "edited": false,
                                            "score": 20,
                                            "creation_date": 1530753492,
                                            "post_id": 379906,
                                            "comment_id": 89346797,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "As a general rule, if you have an object in Python, and want to convert *to* that type of object, call `type(my_object)` on it. The result can usually be called as a function to do the conversion. For instance `type(100)` results in `int`, so you can call `int(my_object)` to try convert `my_object` to an integer. This doesn&#39;t always work, but is a good &quot;first guess&quot; when coding.",
                                            "body": "As a general rule, if you have an object in Python, and want to convert <i>to</i> that type of object, call <code>type(my_object)</code> on it. The result can usually be called as a function to do the conversion. For instance <code>type(100)</code> results in <code>int</code>, so you can call <code>int(my_object)</code> to try convert <code>my_object</code> to an integer. This doesn&#39;t always work, but is a good &quot;first guess&quot; when coding."
                                        }
                                    ]}""")));

        stubFor(
                get("/answers/379910/comments?site=stackoverflow&filter=!nNPvSN_LEO")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "items": [
                                        {
                                            "owner": {
                                                "account_id": 10732388,
                                                "reputation": 72,
                                                "user_id": 7898071,
                                                "user_type": "registered",
                                                "profile_image": "https://lh6.googleusercontent.com/-sf8U4MFB0xI/AAAAAAAAAAI/AAAAAAAACOE/pJF-RSVL1BM/photo.jpg?sz=256",
                                                "display_name": "Joseph Prosper",
                                                "link": "https://stackoverflow.com/users/7898071/joseph-prosper"
                                            },
                                            "edited": false,
                                            "score": 0,
                                            "creation_date": 1712900420,
                                            "post_id": 50854594,
                                            "comment_id": 138066063,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "Also you can use `-&gt;ceilYears() ` example `$period = CarbonPeriod::create(&#39;2020-06-06&#39;, &#39;2028-06-06&#39;)-&gt;ceilYears()` Period will return only array of years",
                                            "body": "Also you can use <code>-&gt;ceilYears() </code> example <code>$period = CarbonPeriod::create(&#39;2020-06-06&#39;, &#39;2028-06-06&#39;)-&gt;ceilYears()</code> Period will return only array of years"
                                        },
                                        {
                                            "owner": {
                                                "account_id": 5538948,
                                                "reputation": 374,
                                                "user_id": 4396293,
                                                "user_type": "registered",
                                                "accept_rate": 0,
                                                "profile_image": "https://i.sstatic.net/RnD0n.jpg?s=256",
                                                "display_name": "Dendi Handian",
                                                "link": "https://stackoverflow.com/users/4396293/dendi-handian"
                                            },
                                            "reply_to_user": {
                                                "account_id": 5684115,
                                                "reputation": 910,
                                                "user_id": 4494207,
                                                "user_type": "registered",
                                                "profile_image": "https://lh4.googleusercontent.com/-PkjKUkAKyIs/AAAAAAAAAAI/AAAAAAAAAgQ/ZPkuZojj5OQ/photo.jpg?sz=256",
                                                "display_name": "ibnɘꟻ",
                                                "link": "https://stackoverflow.com/users/4494207/ibn%c9%98%ea%9f%bb"
                                            },
                                            "edited": false,
                                            "score": 0,
                                            "creation_date": 9646033220,
                                            "post_id": 50854594,
                                            "comment_id": 126015776,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "@ibnɘꟻ I&#39;m aware of it, but the `-&gt;format()` gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere.",
                                            "body": "@ibnɘꟻ I&#39;m aware of it, but the <code>-&gt;format()</code> gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere."
                                        }\
                                ]}""")));

        stubFor(
                get("/answers/78684527/comments?site=stackoverflow&filter=!nNPvSN_LEO")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "items": [
                                        {
                                            "owner": {
                                                "account_id": 10732388,
                                                "reputation": 72,
                                                "user_id": 7898071,
                                                "user_type": "registered",
                                                "profile_image": "https://lh6.googleusercontent.com/-sf8U4MFB0xI/AAAAAAAAAAI/AAAAAAAACOE/pJF-RSVL1BM/photo.jpg?sz=256",
                                                "display_name": "Joseph Prosper",
                                                "link": "https://stackoverflow.com/users/7898071/joseph-prosper"
                                            },
                                            "edited": false,
                                            "score": 0,
                                            "creation_date": 9712900420,
                                            "post_id": 50854594,
                                            "comment_id": 138066063,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "Body example",
                                            "body": "Body example"
                                        },
                                        {
                                            "owner": {
                                                "account_id": 5538948,
                                                "reputation": 374,
                                                "user_id": 4396293,
                                                "user_type": "registered",
                                                "accept_rate": 0,
                                                "profile_image": "https://i.sstatic.net/RnD0n.jpg?s=256",
                                                "display_name": "Dendi Handian",
                                                "link": "https://stackoverflow.com/users/4396293/dendi-handian"
                                            },
                                            "reply_to_user": {
                                                "account_id": 5684115,
                                                "reputation": 910,
                                                "user_id": 4494207,
                                                "user_type": "registered",
                                                "profile_image": "https://lh4.googleusercontent.com/-PkjKUkAKyIs/AAAAAAAAAAI/AAAAAAAAAgQ/ZPkuZojj5OQ/photo.jpg?sz=256",
                                                "display_name": "ibnɘꟻ",
                                                "link": "https://stackoverflow.com/users/4494207/ibn%c9%98%ea%9f%bb"
                                            },
                                            "edited": false,
                                            "score": 0,
                                            "creation_date": 1646033220,
                                            "post_id": 50854594,
                                            "comment_id": 126015776,
                                            "content_license": "CC BY-SA 4.0",
                                            "body_markdown": "@ibnɘꟻ I&#39;m aware of it, but the `-&gt;format()` gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere.",
                                            "body": "@ibnɘꟻ I&#39;m aware of it, but the <code>-&gt;format()</code> gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere."
                                        }\
                                ]}""")));

        List<LinkUpdateInfo> updates = soQuestionClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.size()).isEqualTo(4);
        assertThat(updates.getFirst().commonInfo().trim()).isEqualTo(expectedMessage1.trim());
        assertThat(updates.get(1).commonInfo().trim()).isEqualTo(expectedMessage2.trim());
        assertThat(updates.get(2).commonInfo().trim()).isEqualTo(expectedMessage3.trim());
        assertThat(updates.get(3).commonInfo().trim()).isEqualTo(expectedMessage4.trim());
    }

    @Test
    void getUpdates_WhenRequestError_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        soQuestionClient = new SoQuestionClient(
                x -> String.format("http://localhost:" + port + "/questions/79461427"), restClient, retryTemplate);

        Link link = new Link(1L, "https://stackoverflow.com/questions/79461427");
        stubFor(
                get("/questions/79461427")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "items":[],
                                    "has_more":false,
                                    "quota_max":300,
                                    "quota_remaining":290
                                }
                                """)));

        List<LinkUpdateInfo> updates = soQuestionClient.getUpdates(link);

        assertThat(updates).isEmpty();
    }
}
