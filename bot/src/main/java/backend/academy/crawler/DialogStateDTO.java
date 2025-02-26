package backend.academy.crawler;

import backend.academy.crawler.impl.TrackMessageCrawler;
import com.pengrad.telegrambot.request.SendMessage;

public record DialogStateDTO(SendMessage message, TrackMessageCrawler.TrackMessageState state) {}
