package backend.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Объект передачи данных для запроса на удаление ссылки из списка отслеживаемых */
@Getter
@AllArgsConstructor
public class RemoveLinkRequest {
    private String link;
}
