package com.example.olca.ai.service;

import com.example.olca.ai.domain.ChatFlow;
import com.example.olca.ai.dto.Chatstsettings;
import com.example.olca.ai.dto.PromptContext;
import com.example.olca.ai.promptBuilder.PromptBuilder;
import com.example.olca.ai.repository.ChatFlowRepository;
import com.example.olca.ai.routing.ExecutionPolicy;
import com.example.olca.ai.routing.IntentRoutingDecision;
import com.example.olca.ai.routing.IntentRouter;
import com.example.olca.ai.websocket.VTuberWebSocketClient;
import com.example.olca.chat.domain.ChatMessage;
import com.example.olca.chat.repository.ChatMessageRepository;
import com.example.olca.chat.repository.ChatTagRepository;
import com.example.olca.global.trace.TraceKeys;
import com.example.olca.global.trace.TraceLog;
import com.example.olca.knowledge.domain.KnowledgeBase;
import com.example.olca.knowledge.repository.KnowledgeBaseRepository;
import com.example.olca.knowledge.service.KnowledgeBaseService;
import com.example.olca.tag.domain.Tag;
import com.example.olca.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@Slf4j
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
    private final IntentRouter intentRouter;

    private final Chatstsettings settings;
    private final List<String> stopWords;
    private final TagRepository tagRepository;

    @TraceLog("ChatFlowService.process")
    public Mono<String> process(String question, Long userId, Long sessionId) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        long start = System.currentTimeMillis();

        return Mono.defer(() -> {
                    log.info("[AI_TRACE_START] traceId={} userId={}(사용자) sessionId={}(대화) questionLength={}(질문길이)",
                            traceId, userId, sessionId, question == null ? 0 : question.length());

                    if (question == null || question.isBlank()) {
                        log.info("[AI_EXCEPTION] traceId={} type=EMPTY_QUESTION(빈질문)", traceId);
                        return Mono.just("질문이 비어 있습니다. 궁금한 내용을 한 문장으로 말씀해 주세요.");
                    }

                    IntentRoutingDecision decision = intentRouter.route(question);
                    logRoute(decision);

                    if (!decision.policy().ragAllowed()) {
                        log.info("[AI_CACHE] traceId={} skip=true(캐시건너뜀) reason={}", traceId, decision.policy().cacheSkipReason());
                        return processNewQuestion(question, userId, sessionId, decision);
                    }

                    log.info("[AI_CACHE] traceId={} skip=true(캐시건너뜀) reason=route_first_then_policy({})", traceId, decision.route());
                    return processNewQuestion(question, userId, sessionId, decision);
                })
                .doOnSuccess(answer -> log.info("[AI_TRACE_END] traceId={} totalMs={}(총소요시간ms) success=true(성공)",
                        traceId, System.currentTimeMillis() - start))
                .doOnError(error -> log.warn("[AI_TRACE_END] traceId={} totalMs={}(총소요시간ms) success=false(실패) error={}",
                        traceId, System.currentTimeMillis() - start, error.getClass().getSimpleName()))
                .contextWrite(context -> context.put(TraceKeys.TRACE_ID, traceId));
    }

    private Mono<String> processNewQuestion(String question, Long userId, Long sessionId, IntentRoutingDecision decision) {
        return Mono.zip(
                        validatePastMessages(userId, sessionId),
                        validateTags(question),
                        decision.policy().ragAllowed() ? validateKnowledge(question) : Mono.<List<String>>just(List.of())
                )
                .flatMap(tuple -> {
                    List<Long> relatedMessageIds = tuple.getT1();
                    List<Long> relatedTagIds = tuple.getT2();
                    List<String> relatedKnowledgeIds = tuple.getT3();

                    return generateAnswer(question, relatedMessageIds, relatedTagIds, relatedKnowledgeIds, decision)
                            .flatMap(result -> {
                                if (!result.cacheable()) {
                                    log.info("[AI_CACHE] save=false(캐시저장안함) reason={}", result.cacheSkipReason());
                                    return Mono.just(result.answer());
                                }

                                ChatFlow chatFlow = ChatFlow.builder()
                                        .sessionId(sessionId)
                                        .userId(userId)
                                        .question(question)
                                        .relareMessageIds(relatedMessageIds)
                                        .relatedTagIds(relatedTagIds)
                                        .relatedKnowLedgeIds(relatedKnowledgeIds)
                                        .answer(result.answer())
                                        .cacheable(true)
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

    private List<String> extractKeywords(String question) {
        return List.of(question.split("\\s+"))
                .stream()
                .filter(word -> word.length() > settings.minWordLength())
                .filter(word -> !stopWords.contains(word))
                .toList();
    }

    private Mono<List<String>> validateKnowledge(String question) {
        return knowledgeBaseRepository.textSearch(question)
                .map(KnowledgeBase::getId)
                .collectList();
    }

    private Mono<AnswerResult> generateAnswer(String question, List<Long> messageIds,
                                              List<Long> tagIds, List<String> knowledgeIds,
                                              IntentRoutingDecision decision) {
        ExecutionPolicy policy = decision.policy();

        if (policy.preResponse() != null) {
            sendPreResponse(policy.preResponse());
        }

        Mono<List<KnowledgeBase>> knowledgeDocsMono = policy.ragAllowed()
                ? knowledgeBaseService.vectorSearch(question, 3)
                : Mono.just(List.of());

        return knowledgeDocsMono
                .flatMap(knowledgeDocs -> {
                    if (policy.ragRequired() && knowledgeDocs.isEmpty()) {
                        log.info("[AI_EXCEPTION] type=KNOWLEDGE_NOT_FOUND(지식검색결과없음)");
                        return Mono.just(new AnswerResult(
                                policy.notFoundAnswer(),
                                false,
                                "knowledge_not_found"
                        ));
                    }

                    if (!policy.ragAllowed() && isTooShortConversation(question)) {
                        log.info("[AI_EXCEPTION] type=SHORT_CONVERSATION(짧은대화형입력)");
                        return Mono.just(new AnswerResult(
                                "조금만 더 구체적으로 말씀해 주세요. 궁금한 주제나 원하는 작업을 함께 알려주시면 좋습니다.",
                                false,
                                "short_conversation"
                        ));
                    }

                    log.info("[AI_CONTEXT] route={}({}) knowledgeCount={}(지식문서수) messageCount={}(대화수) tagCount={}(태그수) textSearchCount={}(텍스트검색수)",
                            decision.route(),
                            decision.route().label(),
                            knowledgeDocs.size(), messageIds.size(), tagIds.size(), knowledgeIds.size());

                    List<ChatMessage> messages = messageIds.isEmpty() ? List.of()
                            : chatMessageRepository.findAllById(messageIds);

                    List<Tag> tags = tagIds.isEmpty() ? List.of()
                            : tagRepository.findAllById(tagIds);

                    String systemPrompt = promptBuilder.buildSystemPrompt();
                    String userPrompt = promptBuilder.buildUserPrompt(
                            new PromptContext(question, messages, tags, knowledgeDocs),
                            decision
                    );

                    return Mono.fromCallable(() -> {
                        String answer = ollamaService.chat(systemPrompt, userPrompt);
                        return new AnswerResult(
                                answer,
                                policy.cacheableAnswer(),
                                policy.cacheSkipReason()
                        );
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    private void logRoute(IntentRoutingDecision decision) {
        log.info("[AI_ROUTE] route={}({}) reason={}({})",
                decision.route(),
                decision.route().label(),
                decision.reason(),
                decision.reasonLabel());
    }

    private record AnswerResult(String answer, boolean cacheable, String cacheSkipReason) {
    }

    private boolean isTooShortConversation(String question) {
        String normalized = question == null ? "" : question.trim();
        return normalized.length() <= 1;
    }

    private void sendPreResponse(String message) {
        log.info("[AI_PRE_RESPONSE] message={}(선응답)", message);
        vtuberClient.sendTtsOnly(message)
                .doOnError(error -> log.warn("[AI_PRE_RESPONSE] send=false(전송실패) error={}",
                        error.getClass().getSimpleName()))
                .subscribe(
                        ignored -> log.info("[AI_PRE_RESPONSE] send=true(전송성공)"),
                        error -> {
                        }
                );
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
