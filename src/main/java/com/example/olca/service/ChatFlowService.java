package com.example.olca.service;

import com.example.olca.common.VTuberWebSocketClient;
import com.example.olca.domain.ChatFlow;
import com.example.olca.domain.ChatMessage;
import com.example.olca.repository.ChatFlowRepository;
import com.example.olca.repository.ChatMessageRepository;
import com.example.olca.repository.ChatTagRepository;
import com.example.olca.repository.KnowledgeBaseRepository;
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

    // 메인 처리 파이프라인
    public Mono<String> process(String question, Long userId, Long sessionId) {
        // 1. 캐시 확인 (최적화)
        return chatFlowRepository.findCachedAnswer(question, userId)
                .map(ChatFlow::getAnswer)
                .switchIfEmpty(
                        // 2. 캐시 없으면 전체 처리
                        processNewQuestion(question, userId, sessionId)
                );
    }

    // 새 질문 처리 (검증³ → 처리³ → 출력)
    private Mono<String> processNewQuestion(String question, Long userId, Long sessionId) {
        return Mono.zip(
                        // 검증 1: 과거 대화
                        validatePastMessages(userId, sessionId),
                        // 검증 2: 관련 태그
                        validateTags(question),
                        // 검증 3: 관련 지식
                        validateKnowledge(question)
                )
                .flatMap(tuple -> {
                    List<Long> relatedMessageIds = tuple.getT1();
                    List<Long> relatedTagIds = tuple.getT2();
                    List<String> relatedKnowledgeIds = tuple.getT3();

                    // 처리 1-3: 조합 + 추론 + 답변 생성 (메모리에서만)
                    return generateAnswer(question, relatedMessageIds, relatedTagIds, relatedKnowledgeIds)
                            .flatMap(answer -> {
                                // ChatFlow 저장 (답변만)
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

    // 검증 1: 과거 대화 검증
    private Mono<List<Long>> validatePastMessages(Long userId, Long sessionId) {
        return Mono.fromCallable(() ->
                chatMessageRepository.findRecent(userId, sessionId, 10)
                        .stream()
                        .map(ChatMessage::getId)
                        .toList()
        ).subscribeOn(Schedulers.boundedElastic());
    }


    // 검증 2: 태그 검증
    private Mono<List<Long>> validateTags(String question) {
        return Mono.<List<Long>>fromCallable(() -> {
            List<String> keywords = extractKeywords(question);
            // 키워드 추출

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

    // 키워드 추출 (불용어 제거)
    private List<String> extractKeywords(String question) {
        // 불용어 목록
        List<String> stopWords = List.of(
                "은", "는", "이", "가", "을", "를", "에", "의", "와", "과",
                "도", "만", "라", "이랑", "하고", "어", "아", "요"
        );

        return List.of(question.split("\\s+"))
                .stream()
                .filter(word -> word.length() > 1)  // 1글자 제거
                .filter(word -> !stopWords.contains(word))  // 불용어 제거
                .toList();
    }

    // 검증 3: 지식 검증
    private Mono<List<String>> validateKnowledge(String question) {
        return knowledgeBaseRepository.textSearch(question)
                .map(kb -> kb.getId())
                .collectList();
    }

    // 처리 1-3: 답변 생성 (추론은 메모리에서만)
    private Mono<String> generateAnswer(String question, List<Long> messageIds,
                                        List<Long> tagIds, List<String> knowledgeIds) {
        return vtuberClient.sendMessage(question);
    }


}