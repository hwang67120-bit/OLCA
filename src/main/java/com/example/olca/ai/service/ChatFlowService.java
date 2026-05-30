package com.example.olca.ai.service;

import com.example.olca.ai.domain.ChatFlow;
import com.example.olca.ai.dto.Chatstsettings;
import com.example.olca.ai.dto.PromptContext;
import com.example.olca.ai.promptBuilder.PromptBuilder;
import com.example.olca.ai.repository.ChatFlowRepository;
import com.example.olca.ai.websocket.VTuberWebSocketClient;
import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.chat.repository.ChatMessageRepository;
import com.example.olca.chat.repository.ChatTagRepository;
import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import com.example.olca.knowledge.service.KnowledgeBaseService;
import com.example.olca.tag.domain.Tag;
import com.example.olca.tag.repository.TagRepository;
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

    private final KnowledgeBaseService knowledgeBaseService;
    private final OllamaService ollamaService;
    private final PromptBuilder promptBuilder;

    private final Chatstsettings settings;
    private final List<String> stopWords;
    private final TagRepository tagRepository;

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

    private Mono<String> generateAnswer(String question, List<Long> messageIds,
                                        List<Long> tagIds, List<String> knowledgeIds) {
        return knowledgeBaseService.vectorSearch(question, 3)
                .flatMap(knowledgeDocs -> {
                    List<ChatMessage> messages = messageIds.isEmpty() ? List.of()
                            : chatMessageRepository.findAllById(messageIds);

                    List<Tag> tags = tagIds.isEmpty() ? List.of()
                            : tagRepository.findAllById(tagIds);

                    String systemPrompt = promptBuilder.buildSystemPrompt();
                    String userPrompt = promptBuilder.buildUserPrompt(
                            new PromptContext(question, messages, tags, knowledgeDocs)
                    );

                    return Mono.fromCallable(() ->
                            ollamaService.chat(systemPrompt, userPrompt)
                    ).subscribeOn(Schedulers.boundedElastic());
                });
    }

    private Mono<List<ChatMessage>> fetchMessages(List<Long> ids) {
        if (ids.isEmpty()) return Mono.just(List.of());
        return Mono.fromCallable(() -> chatMessageRepository.findAllById(ids))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<List<Tag>> fetchTags(List<Long> ids) {
        if (ids.isEmpty()) return Mono.just(List.of());
        return Mono.fromCallable(() -> tagRepository.findAllById(ids))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<List<KnowledgeBase>> fetchKnowledge(List<String> ids) {
        if (ids.isEmpty()) return Mono.just(List.of());
        return knowledgeBaseRepository.findAllById(ids).collectList();
    }
}