package backend.academy.bot;

import backend.academy.handler.Handler;
import backend.academy.handler.impl.DefaultMessageHandler;
import backend.academy.handler.impl.HelpMessageHandler;
import backend.academy.handler.impl.ListMessageHandler;
import backend.academy.handler.impl.StartMessageHandler;
import backend.academy.handler.impl.TrackMessageHandler;
import backend.academy.handler.impl.UntrackMessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Bot {
    private final TelegramBot bot;

    @Autowired
    public Bot(BotConfig botConfig) {
        this.bot = new TelegramBot(botConfig.telegramToken());
        this.bot.setUpdatesListener(updates -> {
            updates.forEach(this::processMessage);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendMessage(SendMessage message) {
        bot.execute(message);
    }

    private void processMessage(Update update) {
        String message = update.message().text();
        Handler messageHandler =
                switch (message.split(" ", 2)[0]) {
                    case "/start" -> new StartMessageHandler();
                    case "/help" -> new HelpMessageHandler();
                    case "/track" -> new TrackMessageHandler();
                    case "/untrack" -> new UntrackMessageHandler();
                    case "/list" -> new ListMessageHandler();
                    default -> new DefaultMessageHandler();
                };
        SendMessage response = messageHandler.handle(update);

        sendMessage(response);
    }
}
