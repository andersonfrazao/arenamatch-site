package br.com.arenamatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Esse é o endereço que o navegador vai usar para abrir a conexão
        registry.addEndpoint("/ws-arenamatch").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefixo para os "tópicos" (salas). 
        // O cliente vai se inscrever em algo como: /topic/notificacoes/15 (onde 15 é o ID do time)
        registry.enableSimpleBroker("/topic");
    }
}