package backend.academy.clients.stackoverflow.questions;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.LinkUpdateInfo;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@SuppressWarnings("PMD")
public class SoQuestionClient extends Client {
    private static final Pattern SUPPORTED_LINK = Pattern.compile("^https://stackoverflow\\.com/questions/(\\w+)$");

    private final ApplicationStabilityProperties stabilityProperties;

    @Autowired
    public SoQuestionClient(
            @Qualifier("soQuestionLinkConverter") LinkToApiLinkConverter converter,
            @Qualifier("stackOverflowClient") RestClient stackoverflowClient,
            ApplicationStabilityProperties stabilityProperties) {
        super(SUPPORTED_LINK, converter, stackoverflowClient);
        this.stabilityProperties = stabilityProperties;
    }

    @Override
    public List<LinkUpdateInfo> getUpdates(Link link) {
        String url = linkConverter.convert(link.getUrl());
        if (url == null) {
            return null;
        }

        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();

        // Получаем информацию о вопросе
        SoQuestionDTO questionDTO = getQuestions(
                objectMapper,
                Long.valueOf(Arrays.stream(url.split("/")).toList().getLast()));
        if (questionDTO == null) {
            return List.of();
        }

        // Получаем комментарии к вопросу
        SoCommentListDTO commentListDTO = getCommentsForQuestion(objectMapper, url);
        List<LinkUpdateInfo> commentUpdates = new ArrayList<>();
        if (commentListDTO != null
                && commentListDTO.items() != null
                && !commentListDTO.items().isEmpty()) {
            commentUpdates = generateUpdateTextForComments(commentListDTO, link, questionDTO);
        }

        // Получаем ответы к вопросу
        SoAnswersListDTO answersListDTO = getAnswers(objectMapper, url);
        List<LinkUpdateInfo> answersUpdates = new ArrayList<>();
        if (answersListDTO != null
                && answersListDTO.items() != null
                && !answersListDTO.items().isEmpty()) {
            answersUpdates = generateUpdateTextForAnswers(answersListDTO, link, questionDTO);

            // Получаем комментарии к ответам
            for (SoAnswerDTO answer : answersListDTO.items()) {
                SoCommentListDTO comments = getCommentsForAnswers(objectMapper, answer.answerId());
                answersUpdates.addAll(generateUpdateTextForComments(comments, link, questionDTO));
            }
        }

        List<LinkUpdateInfo> allUpdates = new ArrayList<>();
        allUpdates.addAll(commentUpdates);
        allUpdates.addAll(answersUpdates);
        return allUpdates;
    }

    @Retry(name = "default", fallbackMethod = "onErrorQuestionList")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBErrorQuestionList")
    private SoQuestionDTO getQuestions(ObjectMapper mapper, Long postId) {
        return client.method(HttpMethod.GET)
                .uri("/questions/" + postId + "?site=stackoverflow")
                .header("Accept", "application/json")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return mapper.readValue(response.getBody(), SoQuestionsListDTO.class)
                                .items()
                                .getFirst();
                    } else if (stabilityProperties
                            .getRetry()
                            .getHttpCodes()
                            .contains(response.getStatusCode().value())) {
                        throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                    }
                    return null;
                });
    }

    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    private SoCommentListDTO getCommentsForQuestion(ObjectMapper objectMapper, String baseurl) {
        String url = baseurl + "/comments?site=stackoverflow&filter=!nNPvSN_LI9";
        log.atInfo()
                .setMessage("Обращение к StackOverflow Api для получения комментариев к вопросу")
                .addKeyValue("url", url)
                .log();
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), SoCommentListDTO.class);
            } else if (stabilityProperties
                    .getRetry()
                    .getHttpCodes()
                    .contains(response.getStatusCode().value())) {
                throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
            }
            log.atWarn()
                    .setMessage("Некорректные параметры запроса к StackOverflow API (комментарии к вопросу)")
                    .addKeyValue("url", url)
                    .log();
            return null;
        });
    }

    @Retry(name = "default", fallbackMethod = "onErrorAnswerList")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBErrorAnswerList")
    private SoAnswersListDTO getAnswers(ObjectMapper objectMapper, String baseurl) {
        String url = baseurl + "/answers?site=stackoverflow&filter=!nNPvSNe7D9";
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), SoAnswersListDTO.class);
            } else if (stabilityProperties
                    .getRetry()
                    .getHttpCodes()
                    .contains(response.getStatusCode().value())) {
                throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
            }
            return null;
        });
    }

    @Retry(name = "default", fallbackMethod = "onErrorCommentList")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBErrorCommentList")
    private SoCommentListDTO getCommentsForAnswers(ObjectMapper mapper, Long answerId) {
        return client.get()
                .uri("/answers/" + answerId + "/comments?site=stackoverflow&filter=!nNPvSN_LEO")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return mapper.readValue(response.getBody(), SoCommentListDTO.class);
                    } else if (stabilityProperties
                            .getRetry()
                            .getHttpCodes()
                            .contains(response.getStatusCode().value())) {
                        throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                    }
                    return null;
                });
    }

    private List<LinkUpdateInfo> generateUpdateTextForComments(
            SoCommentListDTO comments, Link link, SoQuestionDTO question) {
        List<LinkUpdateInfo> updates = new ArrayList<>();

        for (SoCommentDTO comment : comments.items()) {
            if (wasUpdated(link.getLastUpdateTime(), comment.createdAt())) {
                updates.add(formatCommentUpdate(comment, question));
            }
        }

        return updates;
    }

    private List<LinkUpdateInfo> generateUpdateTextForAnswers(
            SoAnswersListDTO answers, Link link, SoQuestionDTO question) {
        List<LinkUpdateInfo> updates = new ArrayList<>();

        for (SoAnswerDTO answer : answers.items()) {
            if (wasUpdated(link.getLastUpdateTime(), answer.creationDate())) {
                updates.add(formatAnswerUpdate(answer, question));
            }
        }

        return updates;
    }

    private boolean wasUpdated(Instant lastUpdateTime, Instant updateTime) {
        return lastUpdateTime.isBefore(updateTime);
    }

    private LinkUpdateInfo formatCommentUpdate(SoCommentDTO comment, SoQuestionDTO question) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return new LinkUpdateInfo(
                question.linkValue(),
                comment.owner().name(),
                null,
                comment.text(),
                comment.createdAt(),
                String.format(
                        "Новый комментарий к вопросу %s%nАвтор: %s%nВремя создания: %s (UTC)%nПревью: %s",
                        question.title(),
                        comment.owner().name(),
                        formatter.format(LocalDateTime.ofInstant(comment.createdAt(), ZoneId.of("UTC"))),
                        comment.text()));
    }

    private LinkUpdateInfo formatAnswerUpdate(SoAnswerDTO answer, SoQuestionDTO question) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return new LinkUpdateInfo(
                question.linkValue(),
                answer.owner().name(),
                null,
                answer.text(),
                answer.creationDate(),
                String.format(
                        "Новый ответ к вопросу %s%nАвтор: %s%nВремя создания: %s (UTC)%nПревью: %s",
                        question.title(),
                        answer.owner().name(),
                        formatter.format(LocalDateTime.ofInstant(answer.creationDate(), ZoneId.of("UTC"))),
                        answer.text()));
    }

    private SoQuestionDTO onErrorQuestionList(ObjectMapper mapper, Long postId, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении вопроса SO. Неудачный запрос")
                .addKeyValue("post-id", postId)
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return null;
    }

    private SoQuestionDTO onCBErrorQuestionList(ObjectMapper mapper, Long postId, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении вопроса SO. Сервис недоступен")
                .addKeyValue("post-id", postId)
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return null;
    }

    private SoAnswersListDTO onErrorAnswerList(ObjectMapper objectMapper, String baseurl, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении ответов SO. Неудачный запрос")
                .addKeyValue("baseurl", baseurl)
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return null;
    }

    private SoAnswersListDTO onCBErrorAnswerList(
            ObjectMapper objectMapper, String baseurl, CallNotPermittedException t) {
        log.atWarn()
                .setMessage("Ошибка при получении ответов SO. Сервис недоступен")
                .addKeyValue("baseurl", baseurl)
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return null;
    }

    private SoCommentListDTO onErrorCommentList(ObjectMapper mapper, Long answerId, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении комментариев. Неудачный запрос")
                .addKeyValue("answer-id", answerId)
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return null;
    }

    private SoCommentListDTO onErrorCBCommentList(ObjectMapper mapper, Long answerId, CallNotPermittedException t) {
        log.atWarn()
                .setMessage("Ошибка при получении комментариев. Сервис недоступен")
                .addKeyValue("answer-id", answerId)
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return null;
    }
}
