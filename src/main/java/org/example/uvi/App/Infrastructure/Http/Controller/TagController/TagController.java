package org.example.uvi.App.Infrastructure.Http.Controller.TagController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Services.TagService.TagService;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto.CreateTagRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto.TagDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.TagMapper.TagMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Place tag management")
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

    @GetMapping
    @Operation(summary = "Get all tags", description = "Returns a list of all available tags, optionally filtered by interest category.")
    public ResponseEntity<List<TagDto>> getAllTags(
            @Parameter(description = "Filter by interest category", example = "FAMILY")
            @RequestParam(required = false) Interest interest) {
        List<TagDto> tags = interest != null
                ? tagService.getTagsByInterest(interest).stream().map(tagMapper::toDto).toList()
                : tagService.getAllTags().stream().map(tagMapper::toDto).toList();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<TagDto> getTag(
            @Parameter(description = "ID of the tag", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(tagMapper.toDto(tagService.getTagById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag", description = "Removes a tag from the system. Warning: this will remove the tag from all places using it.")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
