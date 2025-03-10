package backend.academy.clients.stackoverflow.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SoQuestionsListDTO(@JsonProperty("items") List<SoQuestionDTO> items) {}
