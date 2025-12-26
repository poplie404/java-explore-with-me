package ru.practicum.main.service;

import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserShortDto;

import java.util.List;

public interface UserService {
    UserShortDto create(NewUserRequest dto);

    void delete(Long userId);

    List<UserShortDto> getUsers(List<Long> ids, Integer from, Integer size);
}
