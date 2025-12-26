package ru.practicum.main.mapper;

import ru.practicum.dto.*;
import ru.practicum.main.entity.Category;
import ru.practicum.main.entity.Event;
import ru.practicum.main.entity.EventState;

import java.time.LocalDateTime;

public final class EventMapper {

    private EventMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(event.getCategory().getId())
                        .name(event.getCategory().getName())
                        .build())
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserShortDto.builder()
                        .id(event.getInitiatorId())
                        .build())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(event.getCategory().getId())
                        .name(event.getCategory().getName())
                        .build())
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(UserShortDto.builder()
                        .id(event.getInitiatorId())
                        .build())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static Event toEntity(NewEventDto dto, Category category, Long userId) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .category(category)
                .initiatorId(userId)
                .paid(dto.getPaid())
                .confirmedRequests(0)
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .views(0L)
                .title(dto.getTitle())
                .build();
    }
}
