package org.example.uvi.App.Infrastructure.Http.Mapper.UserMapper;

import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Infrastructure.Http.Dto.UserDto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUsername(),
                user.getDateOfBirth(),
                user.getRole(),
                user.getStatus(),
                user.getCity(),
                user.getLatitude(),
                user.getLongitude(),
                user.isPhoneVerified(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
