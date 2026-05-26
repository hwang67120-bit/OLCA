package com.example.olca.common.websocket;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class WebSocketMessageHandler {

    private final ObjectMapper objectMapper;
    private final CompletableFuture<String> responseFuture;

    public void  handleOpen(WebSocketClient client, String messge){
        log.info("webSocket 연결성공");

        try {
            Map<String, Object> payload = Map.of(
                    "type", "text-input",
                    "text", messge
            );
            String jsonMessage =objectMapper.writeValueAsString(payload);
            client.send(jsonMessage);
        } catch (Exception e) {
            log.error("메세지 전송 실패", e);
            responseFuture.completeExceptionally(e);
        }
    }

    public void handleMessage(String message, WebSocketClient client) {
        log.info("=== 수신 메시지 ===");
        log.info("{}", message);
        log.info("==================");

        try {
            Map<String, Object> response = objectMapper.readValue(message, Map.class);
            String type = (String) response.get("type");

            if (shouIdIgnore(type)) {
                log.info("시스템 메시지 무식: {}", type);
                return;
            }

            if ("audio".equals(type)) {
                handleAudioMessage(response, client);
                return;
            }

            if ("backend-synth-complete".equals(type)){
                handleCompletion(client);
                return;
            }

            if ("full-text".equals(type) || "ai-response".equals(type)) {
                handleAIResponse(response, client);
                return;
            }
            if (response.containsKey("text")){
                handleTextResponse(response, client);
            }
        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", message);
            responseFuture.complete(message);
            client.close();
        }
    }

    private void handleTextResponse(Map<String, Object> response, WebSocketClient client) {
        String text = (String) response.get("text");
        if (isValidResponse(text)) {
            responseFuture.complete(text);
            client.close();
        }
    }

    private void handleAIResponse(Map<String, Object> response, WebSocketClient client) {
        String text = (String) response.get("text");
        if (isValidResponse(text)) {
            responseFuture.complete(text);
            client.close();
        }
    }

    private boolean isValidResponse(String text) {
        return text != null &&
                !this.equals("Connection established") &&
                !this.equals("ThinKing...") &&
                !text.startsWith("ThinKing");
    }

    private void handleCompletion(WebSocketClient client) {
        log.info("TTS 완료, 응답 종료");
        if (!responseFuture.isDone()) {
            responseFuture.complete("응답 완료");
        }
        client.close();
    }

    private void handleAudioMessage(Map<String, Object> response, WebSocketClient client) {
        Map<String,Object> displayText = (Map<String, Object>) response.get("displayText");
        if (displayText != null){
            String text = (String) displayText.get("text");
            log.info("Audio 텍스트: {}", text);
            if (text != null && !text.startsWith("[")) {
                responseFuture.complete(text);
                client.close();
            }
        }
    }
    public void handleError(Exception ex) {
        log.error("WebSocket 에러", ex);
        responseFuture.completeExceptionally(ex);
    }

    public void handleClose(String reason) {
        log.info("WebSocket 연결 종료: {}", reason);
    }

    private boolean shouIdIgnore(String type) {
        return "group-update".equals(type) ||
                "set-model-and-conf".equals(type)||
                "controll".equals(type);
    }
}
