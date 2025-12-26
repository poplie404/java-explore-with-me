package ru.practicum.main.mapper;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserShortDto;
import ru.practicum.main.entity.User;

public final class UserMapper {
    private UserMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static User toEntity(NewUserRequest dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static UserShortDto toShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
