package com.oronaminc.join.websocket.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CustomHandshakeHandler handshakeHandler;
    private final StompErrorHandler stompErrorHandler;
    private final WebsocketSessionManager sessionManager;
    private final ApplicationEventPublisher publisher;

    @Bean
    public WebSocketHandlerDecoratorFactory webSocketHandlerDecoratorFactory(
            WebsocketSessionManager sessionManager,
            ApplicationEventPublisher publisher
    ) {
        return delegate -> new CustomWebSocketHandlerDecorator(delegate, sessionManager, publisher);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                // websocket 연결 전 쿠키 체크
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                // websocket 연결 후 principal 생성
                .setHandshakeHandler(handshakeHandler)
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setHandshakeHandler(handshakeHandler);

        registry.setErrorHandler(stompErrorHandler);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setDecoratorFactories(webSocketHandlerDecoratorFactory(sessionManager, publisher));
    }
}
