package backend.academy.bot;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Класс для представления конфигурации телеграм-бота
 *
 * @param telegramToken токен для управления ботом
 * @param telegramName имя бота
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken, @NotEmpty String telegramName) {}
