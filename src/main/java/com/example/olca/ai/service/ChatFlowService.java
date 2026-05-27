package com.example.olca.ai.service;

import com.example.olca.ai.websocket.VTuberWebSocketClient;
import com.example.olca.ai.domain.ChatFlow;
import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.ai.dto.Chatstsettings;
import com.example.olca.ai.repository.ChatFlowRepository;
import com.example.olca.chat.repository.ChatMessageRepository;
import com.example.olca.chat.repository.ChatTagRepository;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatFlowService {

    private final ChatFlowRepository chatFlowRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatTagRepository chatTagRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final VTuberWebSocketClient vtuberClient;

    private final Chatstsettings settings;
    private final List<String> stopWords;

    // 메인 처리 파이프라인
    public Mono<String> process(String question, Long userId, Long sessionId) {
        return chatFlowRepository.findCachedAnswer(question, userId)
                .map(ChatFlow::getAnswer)
                .switchIfEmpty(
                        processNewQuestion(question, userId, sessionId)
                );
    }

    private Mono<String> processNewQuestion(String question, Long userId, Long sessionId) {
        return Mono.zip(
                        validatePastMessages(userId, sessionId),
                        validateTags(question),
                        validateKnowledge(question)
                )
                .flatMap(tuple -> {
                    List<Long> relatedMessageIds = tuple.getT1();
                    List<Long> relatedTagIds = tuple.getT2();
                    List<String> relatedKnowledgeIds = tuple.getT3();

                    return generateAnswer(question, relatedMessageIds, relatedTagIds, relatedKnowledgeIds)
                            .flatMap(answer -> {
                                ChatFlow chatFlow = ChatFlow.builder()
                                        .sessionId(sessionId)
                                        .userId(userId)
                                        .question(question)
                                        .relareMessageIds(relatedMessageIds)
                                        .relatedTagIds(relatedTagIds)
                                        .relatedKnowLedgeIds(relatedKnowledgeIds)
                                        .answer(answer)
                                        .build();

                                return chatFlowRepository.save(chatFlow)
                                        .map(ChatFlow::getAnswer);
                            });
                });
    }


    private Mono<List<Long>> validatePastMessages(Long userId, Long sessionId) {
        return Mono.fromCallable(() ->
                chatMessageRepository.findRecent(
                                userId,
                                sessionId,
                                settings.maxPastMessages()
                        )
                        .stream()
                        .map(ChatMessage::getId)
                        .toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // 검증 2: 태그
    private Mono<List<Long>> validateTags(String question) {
        return Mono.<List<Long>>fromCallable(() -> {
            List<String> keywords = extractKeywords(question);

            if (keywords.isEmpty()) {
                return List.of();
            }

            return chatTagRepository.findByTagNames(keywords)
                    .stream()
                    .map(chatTag -> chatTag.getTag().getId())
                    .distinct()
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // 키워드 추출 (✅ stopWords 사용)
    private List<String> extractKeywords(String question) {
        return List.of(question.split("\\s+"))
                .stream()
                .filter(word -> word.length() > settings.minWordLength())
                .filter(word -> !stopWords.contains(word))
                .toList();
    }

    // 검증 3: 지식
    private Mono<List<String>> validateKnowledge(String question) {
        return knowledgeBaseRepository.textSearch(question)
                .map(kb -> kb.getId())
                .collectList();
    }

    // 답변 생성
    private Mono<String> generateAnswer(String question, List<Long> messageIds,
                                        List<Long> tagIds, List<String> knowledgeIds) {
        return vtuberClient.sendMessage(question);
    }
}