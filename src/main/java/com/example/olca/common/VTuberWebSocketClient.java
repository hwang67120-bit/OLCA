package com.example.olca.common;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class VTuberWebSocketClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketClient client;
    private CompletableFuture<String> responseFuture;

    public Mono<String> sendMessage(String message) {
        responseFuture = new CompletableFuture<>();

        try {
         

            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log.info("WebSocket 연결 성공");

                    try {
                        // ✅ 수정: text-input으로 변경
                        Map<String, Object> payload = Map.of(
                                "type", "text-input",
                                "text", message
                        );
                        String jsonMessage = objectMapper.writeValueAsString(payload);
                        log.info("전송 메시지: {}", jsonMessage);
                        send(jsonMessage);
                    } catch (Exception e) {
                        log.error("메시지 전송 실패", e);
                        responseFuture.completeExceptionally(e);
                    }
                }

                @Override
                public void onMessage(String message) {
                    log.info("=== 수신 메시지 ===");
                    log.info("{}", message);
                    log.info("==================");

                    try {
                        Map<String, Object> response = objectMapper.readValue(message, Map.class);
                        String type = (String) response.get("type");

                        log.info("메시지 타입: {}", type);

                        // 무시할 메시지
                        if ("group-update".equals(type) ||
                                "set-model-and-conf".equals(type) ||
                                "control".equals(type)) {
                            log.info("시스템 메시지 무시: {}", type);
                            return;
                        }

                        // AI 응답
                        if ("full-text".equals(type) || "ai-response".equals(type)) {
                            String text = (String) response.get("text");
                            if (text != null && !text.equals("Connection established")) {
                                log.info("AI 응답: {}", text);
                                responseFuture.complete(text);
                                close();
                                return;
                            }
                        }

                        // text 필드가 있으면 사용
                        if (response.containsKey("text")) {
                            String text = (String) response.get("text");
                            if (text != null && !text.equals("Connection established")) {
                                responseFuture.complete(text);
                                close();
                            }
                        }

                    } catch (Exception e) {
                        log.warn("JSON 파싱 실패: {}", message);
                        responseFuture.complete(message);
                        close();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info("WebSocket 연결 종료: {}", reason);
                }

                @Override
                public void onError(Exception ex) {
                    log.error("WebSocket 에러", ex);
                    responseFuture.completeExceptionally(ex);
                }
            };

            client.connect();

            return Mono.fromFuture(responseFuture);

        } catch (Exception e) {
            log.error("WebSocket 연결 실패", e);
            return Mono.error(e);
        }
    }
}
