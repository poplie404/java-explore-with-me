package ru.practicum.main.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.NewUserRequest;
import ru.practicum.dto.UserShortDto;
import ru.practicum.main.service.UserService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserService userService;

    // 1. Создать пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserShortDto create(@Valid @RequestBody NewUserRequest dto) {
        return userService.create(dto);
    }

    // 2. Получить пользователей по ID
    @GetMapping
    public List<UserShortDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUsers(ids, from, size);
    }

    // 3. Удалить пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
