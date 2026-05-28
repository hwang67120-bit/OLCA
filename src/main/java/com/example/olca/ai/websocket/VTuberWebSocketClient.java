package com.example.olca.ai.websocket;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class VTuberWebSocketClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vtuber.websocket.url}")
    private String websocketUrl;

    private WebSocketClient client;
    private CompletableFuture<String> responseFuture;

    public Mono<String> sendMessage(String message) {
        responseFuture = new CompletableFuture<>();
        WebSocketMessageHandler handler = new WebSocketMessageHandler(objectMapper, responseFuture);

        try {
            URI uri = new URI(websocketUrl);
            log.info("🔌 WebSocket 연결 시도: {}", uri);

            client = new WebSocketClient(uri) {

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    handler.handleOpen(this, message);
                }

                @Override
                public void onMessage(String msg) {
                    handler.handleMessage(msg, this);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    handler.handleClose(reason);
                }

                @Override
                public void onError(Exception ex) {
                    handler.handleError(ex);
                }
            };

            client.setConnectionLostTimeout(120);
            client.connect();

            return Mono.fromFuture(responseFuture)
                    .timeout(Duration.ofSeconds(120));

        } catch (Exception e) {
            log.error("WebSocket 연결 실패", e);
            return Mono.error(e);
        }
    }
    public Mono<String> sendTtsOnly(String text) {
        responseFuture = new CompletableFuture<>();
        WebSocketMessageHandler handler = new WebSocketMessageHandler(objectMapper, responseFuture);

        try {
            URI uri = new URI(websocketUrl);
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    handler.handleOpenTts(this, text);
                }

                @Override
                public void onMessage(String msg) {
                    handler.handleMessage(msg, this);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    handler.handleClose(reason);
                }

                @Override
                public void onError(Exception ex) {
                    handler.handleError(ex);
                }
            };
            client.setConnectionLostTimeout(120);
            client.connect();

            return Mono.fromFuture(responseFuture)
                    .timeout(Duration.ofSeconds(120));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}