package com.example.olca.service;

import com.example.olca.domain.Tag;
import com.example.olca.dto.response.TagResponse;
import com.example.olca.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public TagResponse createOrIncrement(String name) {
        Tag tag = tagRepository.findByName(name)
                .map(existingTag -> {
                    existingTag.incrementCount();
                    return existingTag;
                })
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .name(name)
                            .count(1)
                            .build();
                    return tagRepository.save(newTag);
                });

        return TagResponse.from(tag);
    }
    public TagResponse findByName(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("태그을 찿을 없습니다 "));
        return TagResponse.from(tag);
    }
    public List<TagResponse> getTopTag(int limit){
        List<Tag> tags = tagRepository.findTopTags(limit);
        List<TagResponse> responses = new ArrayList<>();
        for (Tag tag : tags){
            responses.add(TagResponse.from(tag));
        }
        return responses;
    }

    public List<TagResponse> findAll(){
        List<Tag> tags = tagRepository.findAll();
        List<TagResponse> responses = new ArrayList<>();
        for (Tag tag : tags) {
            responses.add(TagResponse.from(tag));

        }
        return responses;
    }
}
