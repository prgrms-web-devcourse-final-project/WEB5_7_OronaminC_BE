package com.oronaminc.join.websocket.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import com.oronaminc.join.websocket.session.CustomWebSocketHandlerDecorator;
import com.oronaminc.join.websocket.session.WebsocketSessionManager;
import com.oronaminc.join.websocket.stomp.StompErrorHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompErrorHandler stompErrorHandler;
    private final WebsocketSessionManager sessionManager;
    private final ApplicationEventPublisher publisher;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

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
                .withSockJS();

        registry.setErrorHandler(stompErrorHandler);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setDecoratorFactories(webSocketHandlerDecoratorFactory(sessionManager, publisher));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
