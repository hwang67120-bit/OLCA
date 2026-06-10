package com.example.olca.ai.service;

import com.example.olca.global.trace.TraceLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @TraceLog("EmbeddingService.embed")
    public List<Double> embed(String text) {
        log.info("[EMBEDDING] inputLength={}(입력길이)", text == null ? 0 : text.length());

        float[] vector = embeddingModel.embed(text);

        List<Double> result = new ArrayList<>();
        for (float value : vector) {
            result.add((double) value);
        }

        log.info("[EMBEDDING] vectorSize={}(벡터크기)", result.size());
        return result;
    }
}
