package org.example.uvi.App.Infrastructure.Http.Mapper.UserInterestMapper;

import org.example.uvi.App.Domain.Models.UserInterest.UserInterest;
import org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto;
import org.springframework.stereotype.Component;

@Component
public class UserInterestMapper {

    public UserInterestDto toDto(UserInterest ui) {
        if (ui == null) return null;
        return new UserInterestDto(
                ui.getId(),
                ui.getInterest(),
                ui.getInterest().getDisplayName(),
                ui.getPreferenceLevel(),
                ui.getCreatedAt(),
                ui.getLastUsedAt()
        );
    }
}
