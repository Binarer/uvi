package org.example.uvi.App.Infrastructure.Http.Controller.TagController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Services.TagService.TagService;
import org.example.uvi.App.Infrastructure.Http.Dto.CreateTagRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.TagMapper.TagMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag management")
@SecurityRequirement(name = "bearerAuth")
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @PostMapping
    @Operation(summary = "Create a new tag")
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody CreateTagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                tagMapper.toDto(tagService.createTag(request.name(), request.interest(), request.description())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<TagDto> getTag(@PathVariable Long id) {
        return ResponseEntity.ok(tagMapper.toDto(tagService.getTagById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all tags, optionally filtered by interest")
    public ResponseEntity<List<TagDto>> getAllTags(
            @RequestParam(required = false) Interest interest) {
        List<TagDto> tags = interest != null
                ? tagService.getTagsByInterest(interest).stream().map(tagMapper::toDto).toList()
                : tagService.getAllTags().stream().map(tagMapper::toDto).toList();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/search")
    @Operation(summary = "Search tags by name or description")
    public ResponseEntity<List<TagDto>> searchTags(@RequestParam String query) {
        return ResponseEntity.ok(tagService.searchTags(query).stream().map(tagMapper::toDto).toList());
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most popular tags")
    public ResponseEntity<List<TagDto>> getPopularTags(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(tagService.getPopularTags(limit).stream().map(tagMapper::toDto).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
