package backend.academy.handler;

import backend.academy.bot.commands.Command;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HandlerManager {
    private final List<Handler> handlerList;

    @Autowired
    public HandlerManager(List<Handler> handlerList) {
        this.handlerList = handlerList;
    }

    public Handler manageHandler(Command command) {
        for (Handler handler : handlerList) {
            if (handler.supportCommand(command)) {
                return handler;
            }
        }
        return null;
    }
}
