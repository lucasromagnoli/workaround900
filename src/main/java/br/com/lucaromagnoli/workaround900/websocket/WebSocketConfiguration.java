package br.com.lucaromagnoli.workaround900.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class WebSocketConfiguration {

    private final MyWebSocketHandler myWebSocketHandler;

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, MyWebSocketHandler> handlerMap = Map.of(
                "/chat", myWebSocketHandler
        );
        return new SimpleUrlHandlerMapping(handlerMap, 1);
    }
}
