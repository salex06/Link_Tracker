package backend.academy.clients.stackoverflow.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SoQuestionsListDTO(List<SoQuestionDTO> items) {}
