package backend.academy.bot;

import backend.academy.processor.Processor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Bot {
    private final TelegramBot bot;

    @Autowired
    public Bot(BotConfig botConfig, Processor tgChatProcessor) {
        this.bot = new TelegramBot(botConfig.telegramToken());
        this.bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                SendMessage message = tgChatProcessor.process(update);
                execute(message);
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void execute(SendMessage message) {
        bot.execute(message);
    }
}
