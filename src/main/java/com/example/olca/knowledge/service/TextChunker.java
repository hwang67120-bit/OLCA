package com.example.olca.knowledge.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TextChunker {

    public List<String> chunk(String text){
        return Arrays.stream(text.split("\\R\\s*\\R"))
                .map(String::trim)
                .filter(chunk -> !chunk.isBlank())
                .toList();
    }
}
