package backend.academy.clients;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientBeans {
    @Bean
    public ClientManager clientManager(List<Client> clientList) {
        return new ClientManager(clientList);
    }
}
