package backend.academy.clients.stackoverflow.answers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Объект передачи данных для получения ответа от StackOverflow
 *
 * @param items ответы на StackOverflow
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoAnswersListDTO(List<SoAnswerDTO> items) {}
