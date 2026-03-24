package org.example.uvi.App.Infrastructure.Http.Dto.TagDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.uvi.App.Domain.Enums.Interest.Interest;

public record CreateTagRequest(
        @NotBlank(message = "Tag name is required")
        @Size(min = 1, max = 100, message = "Tag name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "Interest is required")
        Interest interest,

        @Size(max = 500)
        String description
) {}
