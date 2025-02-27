package backend.academy.crawler;

import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Update;

/**
 * Интерфейс, предоставляющий контракт для построения запроса в формате диалога между пользователем и ботом.
 * Используется для работы с составными командами, т.е. такими командами, для формирования ответа на которые требуется
 * несколько сообщений
 */
public interface MessageCrawler {
    /**
     * Обработать текущее сообщение пользователя, в случае, если сообщение является продолжением диалога (составной
     * команды - например, отслеживания ресурса) - дополнить информацию о текущем его состоянии
     *
     * @param update информация о сообщении пользователя
     * @return {@code DialogStateDTO} - состояние диалога
     */
    DialogStateDTO crawl(Update update);

    /**
     * Получить конечное состояние диалога
     *
     * @param id идентификатор чата
     * @return {@code AddLinkRequest} данные, полученные в ходе диалога
     */
    AddLinkRequest terminate(Long id);
}
