package backend.academy.clients.converter;

/**
 * Функциональный интерфейс, предоставляющий возможность конверитровать пользовательскую ссылку в ссылку на api ресурса
 */
public interface LinkToApiLinkConverter {
    /**
     * Конвертировать пользовательскую ссылку в ссылку на api
     *
     * @param link ссылка от пользователя
     * @return ссылку на api ресурса
     */
    String convert(String link);
}
