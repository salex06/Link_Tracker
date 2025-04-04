package backend.academy.notifications.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.NotificationSender;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class HttpNotificationSenderTest {
    private final int port = 8090;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static NotificationSender notificationSender;

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
    public void sendWorksCorrectly_When400Response() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        notificationSender = new HttpNotificationSender(restClient);
        stubFor(
                post("/updates")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                        {
                            "description": "Некорректные параметры запроса",
                            "code": "400",
                            "exceptionName": "ApiErrorException",
                            "exceptionMessage": null,
                            "stacktrace": [
                                "backend.academy.api.BotController.update(BotController.java:49)",
                                "java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)",
                                "java.base/java.lang.reflect.Method.invoke(Method.java:580)",
                                "org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:257)",
                                "org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190)",
                                "org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)",
                                "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:986)",
                                "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:891)",
                                "org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)",
                                "org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1088)",
                                "org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:978)",
                                "org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)",
                                "org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:914)",
                                "jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590)",
                                "org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)",
                                "jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)",
                                "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195)",
                                "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)",
                                "org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)",
                                "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)",
                                "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)",
                                "org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)",
                                "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)",
                                "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)",
                                "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)",
                                "org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)",
                                "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)",
                                "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)",
                                "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)",
                                "org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:114)",
                                "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)",
                                "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)",
                                "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)",
                                "org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)",
                                "org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)",
                                "org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)",
                                "org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)",
                                "org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)",
                                "org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)",
                                "org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483)",
                                "org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)",
                                "org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)",
                                "org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)",
                                "org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)",
                                "org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)",
                                "org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)",
                                "org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:905)",
                                "org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1741)",
                                "org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)",
                                "org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1190)",
                                "org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)",
                                "org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)",
                                "java.base/java.lang.Thread.run(Thread.java:1575)"
                            ]
                        }
                    """)));

        assertDoesNotThrow(() -> notificationSender.send(new LinkUpdate(null, "", "", new ArrayList<>())));

        WireMock.verify(1, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void sendWorksCorrectly_When200Response() {
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        notificationSender = new HttpNotificationSender(restClient);
        stubFor(post("/updates")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        assertDoesNotThrow(() -> notificationSender.send(new LinkUpdate(1L, "url", "descr", List.of(1L, 2L))));

        WireMock.verify(1, postRequestedFor(urlEqualTo("/updates")));
    }
}
