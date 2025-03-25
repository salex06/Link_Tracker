package backend.academy.clients.stackoverflow.answers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SoAnswersListDTO(List<SoAnswerDTO> items) {}
