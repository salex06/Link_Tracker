package backend.academy.clients.stackoverflow.answers;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SoAnswerClient extends Client {
    private static final Pattern SUPPORTED_LINK = Pattern.compile("^https://stackoverflow\\.com/a/(\\w+)$");

    public SoAnswerClient(@Qualifier("soAnswerLinkConverter") LinkToApiLinkConverter converter) {
        super(SUPPORTED_LINK, converter);
    }

    @Override
    public List<String> getUpdates(Link link, RestClient client) {
        String url = linkConverter.convert(link.getUrl());
        if (url == null) {
            return null;
        }
        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();

        SoAnswersListDTO dto = client.method(HttpMethod.GET)
                .uri(url)
                .header("Accept", "application/json")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return objectMapper.readValue(response.getBody(), SoAnswersListDTO.class);
                    }
                    return null;
                });

        if (dto == null) {
            return List.of();
        }
        return generateUpdateText(dto.items().getFirst(), link);
    }

    private List<String> generateUpdateText(SoAnswerDTO body, Link link) {
        if (!wasUpdated(body, link)) {
            return List.of();
        }
        link.setLastUpdateTime(getLatestUpdate(body));
        return formatUpdate(body, link);
    }

    private boolean wasUpdated(SoAnswerDTO body, Link link) {
        return link.getLastUpdateTime() == null
                || link.getLastUpdateTime()
                        .isBefore(body.lastActivity() == null ? LocalDateTime.MIN : body.lastActivity())
                || link.getLastUpdateTime()
                        .isBefore(body.lastEditDate() == null ? LocalDateTime.MIN : body.lastEditDate());
    }

    private LocalDateTime getLatestUpdate(SoAnswerDTO body) {
        LocalDateTime creationDate = body.creationDate() == null ? LocalDateTime.MIN : body.creationDate();
        LocalDateTime editDate = body.lastEditDate() == null ? LocalDateTime.MIN : body.lastEditDate();
        LocalDateTime activityDate = body.lastActivity() == null ? LocalDateTime.MIN : body.lastActivity();
        return Collections.max(List.of(creationDate, editDate, activityDate));
    }

    private List<String> formatUpdate(SoAnswerDTO body, Link link) {
        return List.of(String.format(
                "Обновление ответа пользователя %s по ссылке %s", body.owner().name(), link.getUrl()));
    }
}
