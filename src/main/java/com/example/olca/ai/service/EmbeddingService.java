package com.example.olca.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel){
        this.embeddingModel = embeddingModel;
    }

    public List<Double> embed(String text) {
        log.info(("임베딩 변환: {} "), text.substring(0, Math.min(50, text.length())));

        float[] vector = embeddingModel.embed(text);

        List<Double> result = new java.util.ArrayList<>();
        for (float f : vector) {
            result.add((double) f);
        }
        log.info(("백터 크기: {}"), result.size());
        return  result;

    }
}
