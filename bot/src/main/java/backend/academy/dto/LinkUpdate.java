package backend.academy.dto;

import java.util.List;

/**
 * DTO для передачи данных об обновлении ресурса
 *
 * @param id идентификатор ссылки
 * @param url ссылка на ресурс
 * @param description описание обновления
 * @param tgChatIds чаты, отслеживающие данный ресурс
 */
public record LinkUpdate(Long id, String url, String description, List<Long> tgChatIds) {
    public LinkUpdate() {
        this(null, null, null, null);
    }

    public static boolean anyFieldIsNull(LinkUpdate linkUpdate) {
        return linkUpdate.url() == null
                || linkUpdate.id() == null
                || linkUpdate.tgChatIds() == null
                || linkUpdate.description() == null;
    }
}
