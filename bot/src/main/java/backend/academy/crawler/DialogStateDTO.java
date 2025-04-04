package backend.academy.crawler;

import com.pengrad.telegrambot.request.SendMessage;

public record DialogStateDTO(SendMessage message, boolean isCompleted) {}
