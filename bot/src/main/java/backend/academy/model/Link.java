package backend.academy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Представление ссылки на ресурс в системе. Объект класса Link имеет идентификатор, значение ссылки, набор чатов,
 * отслеживающих ссылку
 */
public class Link {
    private Long id;
    private String url;
    private List<Long> tgChatIds;
    private static final AtomicLong nextId = new AtomicLong();

    /**
     * Конструктор со всеми параметрами
     *
     * @param id идентификатор ссылки
     * @param url значение ссылки
     * @param tgChatIds чаты, отслеживающие ссылку
     */
    public Link(Long id, String url, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tgChatIds = tgChatIds;
    }

    /**
     * Конструктор с двумя параметрами
     *
     * @param id идентификатор ссылки
     * @param url значение ссылки
     */
    public Link(Long id, String url) {
        this.id = id;
        this.url = url;
        this.tgChatIds = new ArrayList<>();
    }

    /** Конструктор по умолчанию */
    public Link() {
        this.id = null;
        this.url = null;
        this.tgChatIds = new ArrayList<>();
    }

    /**
     * Конструктор с одним параметром с автоматическим присваиванием идентификатора
     *
     * @param url значение ссылки
     */
    public Link(String url) {
        this.id = nextId.incrementAndGet();
        this.url = url;
        this.tgChatIds = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public List<Long> getTgChatIds() {
        return tgChatIds;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link link)) return false;
        return Objects.equals(url, link.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(url);
    }
}
