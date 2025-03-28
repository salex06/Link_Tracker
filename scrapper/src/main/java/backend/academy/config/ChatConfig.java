package backend.academy.config;

import backend.academy.repository.ChatRepository;
import backend.academy.repository.impl.MapTgChatRepository;
import backend.academy.service.ChatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean
    ChatRepository chatRepository() {
        return new MapTgChatRepository();
    }

    @Bean
    ChatService chatService(ChatRepository chatRepository) {
        return new ChatService(chatRepository);
    }
}
