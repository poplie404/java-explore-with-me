package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.main.controller.public_.PublicEventController;
import ru.practicum.main.entity.Category;
import ru.practicum.main.entity.Event;
import ru.practicum.main.entity.EventState;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EventFullDto create(NewEventDto dto, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User id=" + userId + " not found");
        }

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("Category id=" + dto.getCategory() + " not found"));

        Event event = EventMapper.toEntity(dto, category, userId);
        event = eventRepository.save(event);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getOwnEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventUser(Long eventId, UpdateEventUserRequest request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new IllegalArgumentException("Event does not belong to user " + userId);
        }

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());

        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public void cancelEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new IllegalArgumentException("Event does not belong to user " + userId);
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalStateException("Cannot cancel published event");
        }

        event.setState(EventState.CANCELED);
        eventRepository.save(event);
    }

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states,
                                             List<Long> categories, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, Integer from, Integer size) {
        return eventRepository.findAll().stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getTitle() != null) event.setTitle(request.getTitle());

        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public void publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        if (event.getState() != EventState.PENDING) {
            throw new IllegalStateException("Cannot publish event in state " + event.getState());
        }

        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void rejectEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalStateException("Cannot reject published event");
        }

        event.setState(EventState.CANCELED);
        eventRepository.save(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, PublicEventController.EventSort sort,
                                               Integer from, Integer size) {
        return eventRepository.findAllByState(EventState.PUBLISHED).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }
}
