package backend.academy.clients.stackoverflow.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Объект передачи данных для получения данных о вопросе на StackOverflow
 *
 * @param items список вопросов на StackOverflow
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoQuestionsListDTO(List<SoQuestionDTO> items) {}
