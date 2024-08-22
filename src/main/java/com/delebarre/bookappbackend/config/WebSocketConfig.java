package com.delebarre.bookappbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final BookWebSocketHandler bookWebSocketHandler;

    public WebSocketConfig(BookWebSocketHandler bookWebSocketHandler) {
        this.bookWebSocketHandler = bookWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(bookWebSocketHandler, "/ws/books").setAllowedOrigins("*");
    }
}
