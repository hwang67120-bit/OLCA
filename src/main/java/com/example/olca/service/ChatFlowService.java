package com.example.olca.service;

import com.example.olca.domain.ChatFlow;
import com.example.olca.repository.ChatFlowRepository;
import com.example.olca.repository.ChatMessageRepository;
import com.example.olca.repository.ChatTagRepository;
import com.example.olca.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatFlowService {

    private final ChatFlowRepository chatFlowRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatTagRepository chatTagRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;

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
        // TODO: ChatMessage 조회 로직 (내일 Python 연결 시 구현)
        return Mono.just(List.of());
    }

    // 검증 2: 태그 검증
    private Mono<List<Long>> validateTags(String question) {
        // TODO: Tag 패턴 검색 로직 (내일 Python 연결 시 구현)
        return Mono.just(List.of());
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
        // TODO: Ollama AI 호출 (내일 Python 연결 시 구현)
        return Mono.just("임시 답변: " + question);
    }
}