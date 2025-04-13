package backend.academy.dto;

import java.time.Instant;

public record LinkUpdateInfo(
        String url, String authorName, String title, String body, Instant updateTime, String commonInfo) {}
