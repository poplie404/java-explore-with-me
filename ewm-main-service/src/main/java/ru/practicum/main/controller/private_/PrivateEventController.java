package ru.practicum.main.controller.private_;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.main.service.EventService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;

    // 1. Создать событие
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto dto) {
        return eventService.create(dto, userId);
    }

    // 2. Получить свои события
    @GetMapping
    public List<EventFullDto> getOwnEvents(@PathVariable Long userId,
                                           @RequestParam(defaultValue = "0") Integer from,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getOwnEvents(userId, from, size);
    }

    // 3. Получить событие по ID
    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId, HttpServletRequest request) {
        return eventService.getEventById(eventId,request);
    }

    // 4. Обновить свое событие
    @PatchMapping("/{eventId}")
    public EventFullDto updateOwnEvent(@PathVariable Long userId,
                                       @PathVariable Long eventId,
                                       @Valid @RequestBody UpdateEventUserRequest request) {
        return eventService.updateEventUser(eventId, request, userId);
    }

    // 5. Отменить событие
    @PatchMapping("/{eventId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        eventService.cancelEvent(eventId, userId);
    }
}
