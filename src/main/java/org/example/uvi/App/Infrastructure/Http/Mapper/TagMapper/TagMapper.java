package org.example.uvi.App.Infrastructure.Http.Mapper.TagMapper;

import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagDto toDto(Tag tag) {
        if (tag == null) return null;
        return new TagDto(
                tag.getId(),
                tag.getName(),
                tag.getInterest(),
                tag.getDescription(),
                tag.getUsageCount(),
                tag.getCreatedAt()
        );
    }
}
