package org.example.uvi.App.Domain.Services.TagService;

import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Domain.Repository.TagRepository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = Tag.builder()
                .id(1L)
                .name("Jazz Music")
                .interest(Interest.JAZZ)
                .description("Jazz venues")
                .usageCount(10)
                .build();
    }

    @Test
    void getTagById_WhenExists_ReturnsTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        Tag result = tagService.getTagById(1L);

        assertNotNull(result);
        assertEquals("Jazz Music", result.getName());
    }

    @Test
    void getTagById_WhenNotFound_ThrowsException() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tagService.getTagById(99L));
    }

    @Test
    void createTag_WhenNameUnique_CreatesTag() {
        when(tagRepository.existsByName("New Tag")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.createTag("New Tag", Interest.JAZZ, "A new tag");

        assertNotNull(result);
        assertEquals("New Tag", result.getName());
        assertEquals(Interest.JAZZ, result.getInterest());
    }

    @Test
    void createTag_WhenNameExists_ThrowsException() {
        when(tagRepository.existsByName("Jazz Music")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> tagService.createTag("Jazz Music", Interest.JAZZ, "desc"));
    }

    @Test
    void updateTag_WhenValid_UpdatesTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(tagRepository.existsByName("Updated Name")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Tag result = tagService.updateTag(1L, "Updated Name", "Updated desc");

        assertEquals("Updated Name", result.getName());
        assertEquals("Updated desc", result.getDescription());
    }

    @Test
    void deleteTag_WhenExists_DeletesTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

        tagService.deleteTag(1L);

        verify(tagRepository).delete(testTag);
    }

    @Test
    void getAllTags_ReturnsList() {
        when(tagRepository.findAll()).thenReturn(List.of(testTag));

        List<Tag> result = tagService.getAllTags();

        assertEquals(1, result.size());
    }

    @Test
    void getTagsByInterest_ReturnsMatchingTags() {
        when(tagRepository.findByInterest(Interest.JAZZ)).thenReturn(List.of(testTag));

        List<Tag> result = tagService.getTagsByInterest(Interest.JAZZ);

        assertEquals(1, result.size());
        assertEquals(Interest.JAZZ, result.get(0).getInterest());
    }

    @Test
    void searchTags_ReturnsMatchingTags() {
        when(tagRepository.searchTags("jazz")).thenReturn(List.of(testTag));

        List<Tag> result = tagService.searchTags("jazz");

        assertEquals(1, result.size());
    }

    @Test
    void getPopularTags_ReturnsTopTags() {
        when(tagRepository.findTopByOrderByUsageCountDesc(any(Pageable.class)))
                .thenReturn(List.of(testTag));

        List<Tag> result = tagService.getPopularTags(10);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getUsageCount());
    }
}
