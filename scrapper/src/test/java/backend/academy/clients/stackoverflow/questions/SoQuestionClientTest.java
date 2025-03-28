package backend.academy.clients.stackoverflow.questions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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

        String expectedMessage1 = "Новый комментарий к вопросу How do I parse a string to a float or int?\r\n"
                + "Автор: InfiniteStack\r\n"
                + "Время создания: 08-02-2277 13:34 (UTC)\r\n"
                + "Превью: Also make sure the string is actually can be converted to float[,](https://semicolon.dev/stackoverflow/question/8/how-to-convert-a-string-to-a-float-in-python) one way of doing that is to write a cust";
        String expectedMessage2 = String.format(
                "Новый ответ к вопросу How do I parse a string to a float or int?%nАвтор: Blaze%nВремя создания: 01-01-2278 11:57 (UTC)%nПревью: <p>These two functions can encode <strong>any</strong> string to a big number and vice versa</p>\n<pre><code>alphabet = '0123456789abcdefghijklmnopqrstuvwxyz'\n"
                        + "\n"
                        + "def string_to_int(string):\n"
                        + "    intstring");
        String expectedMessage3 = "Новый комментарий к вопросу How do I parse a string to a float or int?\r\n"
                + "Автор: Dendi Handian\r\n"
                + "Время создания: 02-09-2275 21:40 (UTC)\r\n"
                + "Превью: @ibnɘꟻ I&#39;m aware of it, but the `-&gt;format()` gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t";
        String expectedMessage4 = "Новый комментарий к вопросу How do I parse a string to a float or int?\r\n"
                + "Автор: Joseph Prosper\r\n"
                + "Время создания: 15-10-2277 19:53 (UTC)\r\n"
                + "Превью: Body example";
        Link link = new Link(1L, "https://stackoverflow.com/questions/6031003");
        stubFor(get("/questions/6031003?site=stackoverflow")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"items\": [\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 14717,\n"
                                + "                \"reputation\": 69398,\n"
                                + "                \"user_id\": 30529,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"accept_rate\": 94,\n"
                                + "                \"profile_image\": \"https://www.gravatar.com/avatar/f3a3491c1c83e8c62151752162184627?s=256&d=identicon&r=PG\",\n"
                                + "                \"display_name\": \"Tristan Havelick\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/30529/tristan-havelick\"\n"
                                + "            },\n"
                                + "            \"score\": 2764,\n"
                                + "            \"last_edit_date\": 1700761208,\n"
                                + "            \"last_activity_date\": 1720472120,\n"
                                + "            \"creation_date\": 1229651546,\n"
                                + "            \"post_type\": \"question\",\n"
                                + "            \"post_id\": 379906,\n"
                                + "            \"title\": \"How do I parse a string to a float or int?\",\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"link\": \"https://stackoverflow.com/q/379906\"\n"
                                + "        }\n"
                                + "    ]\n"
                                + "}")));

        stubFor(get("/questions/6031003/answers?site=stackoverflow&filter=!nNPvSNe7D9")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"items\": [\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 809,\n"
                                + "                \"reputation\": 182116,\n"
                                + "                \"user_id\": 1057,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"accept_rate\": 80,\n"
                                + "                \"profile_image\": \"https://www.gravatar.com/avatar/e5778b659d144e38ed982c3f4e566089?s=256&d=identicon&r=PG\",\n"
                                + "                \"display_name\": \"Harley Holcombe\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/1057/harley-holcombe\"\n"
                                + "            },\n"
                                + "            \"is_accepted\": true,\n"
                                + "            \"score\": 3100,\n"
                                + "            \"last_activity_date\": 1720472120,\n"
                                + "            \"last_edit_date\": 1720472120,\n"
                                + "            \"creation_date\": 1229651691,\n"
                                + "            \"answer_id\": 379910,\n"
                                + "            \"question_id\": 379906,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"    &gt;&gt;&gt; a = &quot;545.2222&quot;\\r\\n    &gt;&gt;&gt; float(a)\\r\\n    545.22220000000004\\r\\n    &gt;&gt;&gt; int(float(a))\\r\\n    545\",\n"
                                + "            \"body\": \"<pre><code>&gt;&gt;&gt; a = &quot;545.2222&quot;\\n&gt;&gt;&gt; float(a)\\n545.22220000000004\\n&gt;&gt;&gt; int(float(a))\\n545\\n</code></pre>\\n\"\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 6915203,\n"
                                + "                \"reputation\": 939,\n"
                                + "                \"user_id\": 5468048,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"profile_image\": \"https://www.gravatar.com/avatar/2b065a7ccc825411060cd6bde87ebb66?s=256&d=identicon&r=PG&f=y&so-version=2\",\n"
                                + "                \"display_name\": \"Blaze\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/5468048/blaze\"\n"
                                + "            },\n"
                                + "            \"is_accepted\": false,\n"
                                + "            \"score\": 0,\n"
                                + "            \"last_activity_date\": 1719612194,\n"
                                + "            \"last_edit_date\": 1719612194,\n"
                                + "            \"creation_date\": 9719611053,\n"
                                + "            \"answer_id\": 78684527,\n"
                                + "            \"question_id\": 379906,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"These two functions can encode **any** string to a big number and vice versa\\r\\n\\r\\n    alphabet = &#39;0123456789abcdefghijklmnopqrstuvwxyz&#39;\\r\\n\\r\\n    def string_to_int(string):\\r\\n        intstring = []\\r\\n        for i in range(len(string)):\\r\\n            n = int(string[i], 36)\\r\\n            sn = str(n)\\r\\n            if len(sn) == 1:\\r\\n                intstring.append(&#39;0&#39;)\\r\\n            intstring.append(sn)\\r\\n        return int(&#39;&#39;.join(intstring))\\r\\n    \\r\\n    \\r\\n    def int_to_string(integer):\\r\\n        global alphabet\\r\\n        string = str(integer)\\r\\n        result = []\\r\\n        for i in range(0, len(string), 2):\\r\\n            c1 = string[i]\\r\\n            c2 = &#39;&#39;\\r\\n            if len(string) &gt;= i:\\r\\n                c2 = string[i + 1]\\r\\n            code = int(c1 + c2)\\r\\n            result.append(alphabet[code])\\r\\n        return &#39;&#39;.join(result)\\r\\n\\r\\nTest them with this print\\r\\n\\r\\n    print(int_to_string(string_to_int(&#39;apple12345&#39;)))\",\n"
                                + "            \"body\": \"<p>These two functions can encode <strong>any</strong> string to a big number and vice versa</p>\\n<pre><code>alphabet = '0123456789abcdefghijklmnopqrstuvwxyz'\\n\\ndef string_to_int(string):\\n    intstring = []\\n    for i in range(len(string)):\\n        n = int(string[i], 36)\\n        sn = str(n)\\n        if len(sn) == 1:\\n            intstring.append('0')\\n        intstring.append(sn)\\n    return int(''.join(intstring))\\n\\n\\ndef int_to_string(integer):\\n    global alphabet\\n    string = str(integer)\\n    result = []\\n    for i in range(0, len(string), 2):\\n        c1 = string[i]\\n        c2 = ''\\n        if len(string) &gt;= i:\\n            c2 = string[i + 1]\\n        code = int(c1 + c2)\\n        result.append(alphabet[code])\\n    return ''.join(result)\\n</code></pre>\\n<p>Test them with this print</p>\\n<pre><code>print(int_to_string(string_to_int('apple12345')))\\n</code></pre>\\n\"\n"
                                + "        }]}")));

        stubFor(get("/questions/6031003/comments?site=stackoverflow&filter=!nNPvSN_LI9")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"items\": [\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 1292874,\n"
                                + "                \"reputation\": 458,\n"
                                + "                \"user_id\": 1244611,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"accept_rate\": 0,\n"
                                + "                \"profile_image\": \"https://www.gravatar.com/avatar/e5b6a8e239f3eba9cd5855596a77a0b5?s=256&d=identicon&r=PG\",\n"
                                + "                \"display_name\": \"InfiniteStack\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/1244611/infinitestack\"\n"
                                + "            },\n"
                                + "            \"edited\": false,\n"
                                + "            \"score\": 0,\n"
                                + "            \"creation_date\": 9691364055,\n"
                                + "            \"post_id\": 379906,\n"
                                + "            \"comment_id\": 135477565,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"Also make sure the string is actually can be converted to float[,](https://semicolon.dev/stackoverflow/question/8/how-to-convert-a-string-to-a-float-in-python) one way of doing that is to write a custom function with a try/except block, that checks for `return float(str_value)` inside try scope.\",\n"
                                + "            \"body\": \"Also make sure the string is actually can be converted to float<a href=\\\"https://semicolon.dev/stackoverflow/question/8/how-to-convert-a-string-to-a-float-in-python\\\" rel=\\\"nofollow noreferrer\\\">,</a> one way of doing that is to write a custom function with a try/except block, that checks for <code>return float(str_value)</code> inside try scope.\"\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 117351,\n"
                                + "                \"reputation\": 722,\n"
                                + "                \"user_id\": 307363,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"accept_rate\": 100,\n"
                                + "                \"profile_image\": \"https://www.gravatar.com/avatar/e8a78675b2e22a5eda938f7146af689c?s=256&d=identicon&r=PG\",\n"
                                + "                \"display_name\": \"robertlayton\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/307363/robertlayton\"\n"
                                + "            },\n"
                                + "            \"edited\": false,\n"
                                + "            \"score\": 20,\n"
                                + "            \"creation_date\": 1530753492,\n"
                                + "            \"post_id\": 379906,\n"
                                + "            \"comment_id\": 89346797,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"As a general rule, if you have an object in Python, and want to convert *to* that type of object, call `type(my_object)` on it. The result can usually be called as a function to do the conversion. For instance `type(100)` results in `int`, so you can call `int(my_object)` to try convert `my_object` to an integer. This doesn&#39;t always work, but is a good &quot;first guess&quot; when coding.\",\n"
                                + "            \"body\": \"As a general rule, if you have an object in Python, and want to convert <i>to</i> that type of object, call <code>type(my_object)</code> on it. The result can usually be called as a function to do the conversion. For instance <code>type(100)</code> results in <code>int</code>, so you can call <code>int(my_object)</code> to try convert <code>my_object</code> to an integer. This doesn&#39;t always work, but is a good &quot;first guess&quot; when coding.\"\n"
                                + "        }\n"
                                + "    ]}")));

        stubFor(get("/answers/379910/comments?site=stackoverflow&filter=!nNPvSN_LEO")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"items\": [\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 10732388,\n"
                                + "                \"reputation\": 72,\n"
                                + "                \"user_id\": 7898071,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"profile_image\": \"https://lh6.googleusercontent.com/-sf8U4MFB0xI/AAAAAAAAAAI/AAAAAAAACOE/pJF-RSVL1BM/photo.jpg?sz=256\",\n"
                                + "                \"display_name\": \"Joseph Prosper\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/7898071/joseph-prosper\"\n"
                                + "            },\n"
                                + "            \"edited\": false,\n"
                                + "            \"score\": 0,\n"
                                + "            \"creation_date\": 1712900420,\n"
                                + "            \"post_id\": 50854594,\n"
                                + "            \"comment_id\": 138066063,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"Also you can use `-&gt;ceilYears() ` example `$period = CarbonPeriod::create(&#39;2020-06-06&#39;, &#39;2028-06-06&#39;)-&gt;ceilYears()` Period will return only array of years\",\n"
                                + "            \"body\": \"Also you can use <code>-&gt;ceilYears() </code> example <code>$period = CarbonPeriod::create(&#39;2020-06-06&#39;, &#39;2028-06-06&#39;)-&gt;ceilYears()</code> Period will return only array of years\"\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 5538948,\n"
                                + "                \"reputation\": 374,\n"
                                + "                \"user_id\": 4396293,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"accept_rate\": 0,\n"
                                + "                \"profile_image\": \"https://i.sstatic.net/RnD0n.jpg?s=256\",\n"
                                + "                \"display_name\": \"Dendi Handian\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/4396293/dendi-handian\"\n"
                                + "            },\n"
                                + "            \"reply_to_user\": {\n"
                                + "                \"account_id\": 5684115,\n"
                                + "                \"reputation\": 910,\n"
                                + "                \"user_id\": 4494207,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"profile_image\": \"https://lh4.googleusercontent.com/-PkjKUkAKyIs/AAAAAAAAAAI/AAAAAAAAAgQ/ZPkuZojj5OQ/photo.jpg?sz=256\",\n"
                                + "                \"display_name\": \"ibnɘꟻ\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/4494207/ibn%c9%98%ea%9f%bb\"\n"
                                + "            },\n"
                                + "            \"edited\": false,\n"
                                + "            \"score\": 0,\n"
                                + "            \"creation_date\": 9646033220,\n"
                                + "            \"post_id\": 50854594,\n"
                                + "            \"comment_id\": 126015776,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"@ibnɘꟻ I&#39;m aware of it, but the `-&gt;format()` gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere.\",\n"
                                + "            \"body\": \"@ibnɘꟻ I&#39;m aware of it, but the <code>-&gt;format()</code> gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere.\"\n"
                                + "        }"
                                + "]}")));

        stubFor(get("/answers/78684527/comments?site=stackoverflow&filter=!nNPvSN_LEO")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" + "    \"items\": [\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 10732388,\n"
                                + "                \"reputation\": 72,\n"
                                + "                \"user_id\": 7898071,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"profile_image\": \"https://lh6.googleusercontent.com/-sf8U4MFB0xI/AAAAAAAAAAI/AAAAAAAACOE/pJF-RSVL1BM/photo.jpg?sz=256\",\n"
                                + "                \"display_name\": \"Joseph Prosper\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/7898071/joseph-prosper\"\n"
                                + "            },\n"
                                + "            \"edited\": false,\n"
                                + "            \"score\": 0,\n"
                                + "            \"creation_date\": 9712900420,\n"
                                + "            \"post_id\": 50854594,\n"
                                + "            \"comment_id\": 138066063,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"Body example\",\n"
                                + "            \"body\": \"Body example\"\n"
                                + "        },\n"
                                + "        {\n"
                                + "            \"owner\": {\n"
                                + "                \"account_id\": 5538948,\n"
                                + "                \"reputation\": 374,\n"
                                + "                \"user_id\": 4396293,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"accept_rate\": 0,\n"
                                + "                \"profile_image\": \"https://i.sstatic.net/RnD0n.jpg?s=256\",\n"
                                + "                \"display_name\": \"Dendi Handian\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/4396293/dendi-handian\"\n"
                                + "            },\n"
                                + "            \"reply_to_user\": {\n"
                                + "                \"account_id\": 5684115,\n"
                                + "                \"reputation\": 910,\n"
                                + "                \"user_id\": 4494207,\n"
                                + "                \"user_type\": \"registered\",\n"
                                + "                \"profile_image\": \"https://lh4.googleusercontent.com/-PkjKUkAKyIs/AAAAAAAAAAI/AAAAAAAAAgQ/ZPkuZojj5OQ/photo.jpg?sz=256\",\n"
                                + "                \"display_name\": \"ibnɘꟻ\",\n"
                                + "                \"link\": \"https://stackoverflow.com/users/4494207/ibn%c9%98%ea%9f%bb\"\n"
                                + "            },\n"
                                + "            \"edited\": false,\n"
                                + "            \"score\": 0,\n"
                                + "            \"creation_date\": 1646033220,\n"
                                + "            \"post_id\": 50854594,\n"
                                + "            \"comment_id\": 126015776,\n"
                                + "            \"content_license\": \"CC BY-SA 4.0\",\n"
                                + "            \"body_markdown\": \"@ibnɘꟻ I&#39;m aware of it, but the `-&gt;format()` gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere.\",\n"
                                + "            \"body\": \"@ibnɘꟻ I&#39;m aware of it, but the <code>-&gt;format()</code> gives you the ability to globally store the string format in config. So, whenever there is a request to change the format everywhere, you don&#39;t need to change the code everywhere.\"\n"
                                + "        }"
                                + "]}")));

        List<String> updates = soQuestionClient.getUpdates(link);

        assertThat(updates).isNotEmpty();
        assertThat(updates.size()).isEqualTo(4);
        assertThat(updates.getFirst().trim()).isEqualTo(expectedMessage1.trim());
        assertThat(updates.get(1).trim()).isEqualTo(expectedMessage2.trim());
        assertThat(updates.get(2).trim()).isEqualTo(expectedMessage3.trim());
        assertThat(updates.get(3).trim()).isEqualTo(expectedMessage4.trim());
    }

    @Test
    void getUpdates_WhenRequestError_ThenReturnEmptyList() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        soQuestionClient = new SoQuestionClient(
                x -> String.format("http://localhost:" + port + "/questions/79461427"), restClient);

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
