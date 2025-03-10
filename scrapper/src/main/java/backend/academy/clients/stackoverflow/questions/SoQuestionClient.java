package backend.academy.clients.stackoverflow.questions;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
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
public class SoQuestionClient extends Client {
    private static final Pattern SUPPORTED_LINK = Pattern.compile("^https://stackoverflow\\.com/questions/(\\w+)$");

    @Autowired
    public SoQuestionClient(
            @Qualifier("soQuestionLinkConverter") LinkToApiLinkConverter converter,
            @Qualifier("stackOverflowClient") RestClient stackoverflowClient) {
        super(SUPPORTED_LINK, converter, stackoverflowClient);
    }

    @Override
    public List<String> getUpdates(Link link) {
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
        List<String> commentUpdates = new ArrayList<>();
        if (commentListDTO != null
                && commentListDTO.items() != null
                && !commentListDTO.items().isEmpty()) {
            commentUpdates = generateUpdateTextForComments(commentListDTO, link, questionDTO);
        }

        // Получаем ответы к вопросу
        SoAnswersListDTO answersListDTO = getAnswers(objectMapper, url);
        List<String> answersUpdates = new ArrayList<>();
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

        List<String> allUpdates = new ArrayList<>();
        allUpdates.addAll(commentUpdates);
        allUpdates.addAll(answersUpdates);
        link.setLastUpdateTime(LocalDateTime.now());
        return allUpdates;
    }

    private SoQuestionDTO getQuestions(ObjectMapper mapper, Long postId) {
        return client.method(HttpMethod.GET)
                .uri("/questions/" + postId + "?site=stackoverflow")
                .header("Accept", "application/json")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        // System.out.println(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                        return mapper.readValue(response.getBody(), SoQuestionsListDTO.class)
                                .items()
                                .getFirst();
                    }
                    return null;
                });
    }

    private SoCommentListDTO getCommentsForQuestion(ObjectMapper objectMapper, String baseurl) {
        String url = baseurl + "/comments?site=stackoverflow&filter=!nNPvSN_LI9";
        log.atInfo()
                .setMessage("Обращение к StackOverflow Api для получения комментариев к вопросу")
                .addKeyValue("url", url)
                .log();
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), SoCommentListDTO.class);
            }
            log.atWarn()
                    .setMessage("Некорректные параметры запроса к StackOverflow API (комментарии к вопросу)")
                    .addKeyValue("url", url)
                    .log();
            return null;
        });
    }

    private SoAnswersListDTO getAnswers(ObjectMapper objectMapper, String baseurl) {
        String url = baseurl + "/answers?site=stackoverflow&filter=!nNPvSNe7D9";
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), SoAnswersListDTO.class);
            }
            return null;
        });
    }

    private SoCommentListDTO getCommentsForAnswers(ObjectMapper mapper, Long answerId) {
        return client.get()
                .uri("/answers/" + answerId + "/comments?site=stackoverflow&filter=!nNPvSN_LEO")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return mapper.readValue(response.getBody(), SoCommentListDTO.class);
                    }
                    return null;
                });
    }

    private List<String> generateUpdateTextForComments(SoCommentListDTO comments, Link link, SoQuestionDTO question) {
        List<String> updates = new ArrayList<>();

        for (SoCommentDTO comment : comments.items()) {
            if (wasUpdated(link.getLastUpdateTime(), comment.createdAt())) {
                updates.add(formatCommentUpdate(comment, question));
            }
        }

        return updates;
    }

    private List<String> generateUpdateTextForAnswers(SoAnswersListDTO answers, Link link, SoQuestionDTO question) {
        List<String> updates = new ArrayList<>();

        for (SoAnswerDTO answer : answers.items()) {
            if (wasUpdated(link.getLastUpdateTime(), answer.creationDate())) {
                updates.add(formatAnswerUpdate(answer, question));
            }
        }

        return updates;
    }

    private boolean wasUpdated(LocalDateTime lastUpdateTime, LocalDateTime updateTime) {
        return lastUpdateTime.isBefore(updateTime);
    }

    private String formatCommentUpdate(SoCommentDTO comment, SoQuestionDTO question) {
        return String.format(
                "Новый комментарий к вопросу %s%nАвтор: %s%nВремя создания: %s%nПревью: %s",
                question.title(), comment.owner().name(), comment.createdAt(), comment.text());
    }

    private String formatAnswerUpdate(SoAnswerDTO answer, SoQuestionDTO question) {
        return String.format(
                "Новый ответ к вопросу %s%nАвтор: %s%nВремя создания: %s%nПревью: %s",
                question.title(), answer.owner().name(), answer.creationDate(), answer.text());
    }
}
