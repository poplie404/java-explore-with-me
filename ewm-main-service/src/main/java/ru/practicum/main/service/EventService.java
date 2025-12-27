package ru.practicum.main.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.dto.EventShortDto;
import ru.practicum.main.controller.public_.PublicEventController;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto create(NewEventDto dto, Long userId);

    List<EventFullDto> getOwnEvents(Long userId, Integer from, Integer size);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    EventFullDto updateEventUser(Long eventId, UpdateEventUserRequest request, Long userId);

    void cancelEvent(Long eventId, Long userId);

    List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states,
                                      List<Long> categories, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    void publishEvent(Long eventId);

    void rejectEvent(Long eventId);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, PublicEventController.EventSort sort,
                                        Integer from, Integer size);
}
