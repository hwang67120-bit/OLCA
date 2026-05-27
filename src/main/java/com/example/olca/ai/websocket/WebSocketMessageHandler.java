package com.example.olca.ai.websocket;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class WebSocketMessageHandler {

    private final ObjectMapper objectMapper;
    private final CompletableFuture<String> responseFuture;
    private final StringBuilder fullResponse = new StringBuilder();  // ✅ 응답 누적

    public WebSocketMessageHandler(ObjectMapper objectMapper, CompletableFuture<String> responseFuture) {
        this.objectMapper = objectMapper;
        this.responseFuture = responseFuture;
    }

    public void handleOpen(WebSocketClient client, String message) {
        log.info("✅ WebSocket 연결 성공");

        try {
            Map<String, Object> payload = Map.of(
                    "type", "text-input",
                    "text", message
            );
            String jsonMessage = objectMapper.writeValueAsString(payload);
            log.info("📤 전송 메시지: {}", jsonMessage);
            client.send(jsonMessage);
        } catch (Exception e) {
            log.error("❌ 메시지 전송 실패", e);
            responseFuture.completeExceptionally(e);
        }
    }

    public void handleMessage(String message, WebSocketClient client) {
        try {
            // ✅ UTF-8 디코딩
            String decodedMessage = new String(
                    message.getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8
            );

            Map<String, Object> response = objectMapper.readValue(decodedMessage, Map.class);
            String type = (String) response.get("type");

            log.info("📥 수신 타입: {}", type);

            // ✅ audio 메시지: 텍스트 누적
            if ("audio".equals(type)) {
                Map<String, Object> displayText = (Map<String, Object>) response.get("display_text");
                if (displayText != null) {
                    String text = (String) displayText.get("text");
                    if (text != null && !text.startsWith("[")) {
                        fullResponse.append(text).append(" ");
                        log.info("📝 텍스트 누적: {}", text);
                    }
                }
            }

            // ✅ 완료 신호: 응답 반환 및 종료
            if ("backend-synth-complete".equals(type)) {
                String finalResponse = fullResponse.toString().trim();
                log.info("✅ 전체 응답: {}", finalResponse);
                responseFuture.complete(finalResponse);
                client.close();
            }

        } catch (Exception e) {
            log.error("❌ 메시지 처리 실패", e);
            responseFuture.completeExceptionally(e);
            client.close();
        }
    }

    public void handleError(Exception ex) {
        log.error("❌ WebSocket 에러", ex);
        responseFuture.completeExceptionally(ex);
    }

    public void handleClose(String reason) {
        log.info("🔌 WebSocket 연결 종료: {}", reason);
    }
}