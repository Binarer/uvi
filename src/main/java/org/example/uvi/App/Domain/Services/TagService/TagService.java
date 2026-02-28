package org.example.uvi.App.Domain.Services.TagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Domain.Repository.TagRepository.TagRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    @Cacheable(value = "tags", key = "#id")
    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<Tag> getTagsByInterest(Interest interest) {
        return tagRepository.findByInterest(interest);
    }

    public List<Tag> searchTags(String query) {
        return tagRepository.searchTags(query);
    }

    public List<Tag> getPopularTags(int limit) {
        return tagRepository.findTopByOrderByUsageCountDesc(PageRequest.of(0, limit));
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public Tag createTag(String name, Interest interest, String description) {
        if (tagRepository.existsByName(name)) {
            throw new IllegalArgumentException("Tag already exists: " + name);
        }

        Tag tag = Tag.builder()
                .name(name)
                .interest(interest)
                .description(description)
                .usageCount(0)
                .build();

        return tagRepository.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", key = "#id")
    public Tag updateTag(Long id, String name, String description) {
        Tag tag = getTagById(id);

        if (name != null && !name.equals(tag.getName())) {
            if (tagRepository.existsByName(name)) {
                throw new IllegalArgumentException("Tag with name already exists: " + name);
            }
            tag.setName(name);
        }
        if (description != null) tag.setDescription(description);

        return tagRepository.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", key = "#id")
    public void deleteTag(Long id) {
        Tag tag = getTagById(id);
        tagRepository.delete(tag);
        log.info("Tag {} deleted", id);
    }
}
