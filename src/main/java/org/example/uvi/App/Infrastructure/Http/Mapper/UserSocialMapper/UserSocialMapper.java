package org.example.uvi.App.Infrastructure.Http.Mapper.UserSocialMapper;

import org.example.uvi.App.Domain.Models.UserFavorite.UserFavorite;
import org.example.uvi.App.Domain.Models.UserVisit.UserVisit;
import org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto.UserFavoriteDto;
import org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto.UserVisitDto;
import org.springframework.stereotype.Component;

@Component
public class UserSocialMapper {

    public UserFavoriteDto toFavoriteDto(UserFavorite favorite) {
        if (favorite == null) return null;
        return new UserFavoriteDto(
                favorite.getId(),
                favorite.getPlace().getId(),
                favorite.getPlace().getName(),
                favorite.getPlace().getMainPhotoUrl(),
                favorite.getCreatedAt()
        );
    }

    public UserVisitDto toVisitDto(UserVisit visit) {
        if (visit == null) return null;
        return new UserVisitDto(
                visit.getId(),
                visit.getPlace().getId(),
                visit.getPlace().getName(),
                visit.getPlace().getMainPhotoUrl(),
                visit.getVisitedAt(),
                visit.getComment(),
                visit.getRating()
        );
    }
}
