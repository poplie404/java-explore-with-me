package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.*;
import ru.practicum.ewm.entity.Category;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.User;
import ru.practicum.ewm.entity.enums.EventState;
import ru.practicum.ewm.entity.enums.RequestStatus;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.LocationMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APP_NAME = "ewm-main-service";
    private static final LocalDateTime MIN_DATETIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    private static final LocalDateTime MAX_DATETIME = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto eventDto) {
        if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + eventDto.getEventDate());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + eventDto.getCategory() + " was not found"));

        Event event = Event.builder()
                .annotation(eventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(eventDto.getDescription())
                .eventDate(eventDto.getEventDate())
                .initiator(user)
                .location(LocationMapper.toEntity(eventDto.getLocation()))
                .paid(eventDto.getPaid() != null ? eventDto.getPaid() : false)
                .participantLimit(eventDto.getParticipantLimit() != null ? eventDto.getParticipantLimit() : 0)
                .requestModeration(eventDto.getRequestModeration() != null ? eventDto.getRequestModeration() : true)
                .state(EventState.PENDING)
                .title(eventDto.getTitle())
                .build();

        Event saved = eventRepository.save(event);
        return EventMapper.toFullDto(saved, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Pageable pageable) {
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();
        return getEventShortDtos(events);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        return EventMapper.toFullDto(event, confirmedRequests, 0L);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
        }

        updateEventFields(event, request);

        if ("SEND_TO_REVIEW".equals(request.getStateAction())) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(request.getStateAction())) {
            event.setState(EventState.CANCELED);
        }

        Event saved = eventRepository.save(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        return EventMapper.toFullDto(saved, confirmedRequests, 0L);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size, Pageable pageable) {
        if (from < 0) {
            throw new BadRequestException("from must be >= 0");
        }
        if (size <= 0) {
            throw new BadRequestException("size must be > 0");
        }

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        LocalDateTime start = rangeStart != null ? rangeStart : MIN_DATETIME;
        LocalDateTime end = rangeEnd != null ? rangeEnd : MAX_DATETIME;
        List<Long> usersParam = (users != null && !users.isEmpty()) ? users : null;
        List<EventState> statesParam = (states != null && !states.isEmpty()) ? states : null;
        List<Long> categoriesParam = (categories != null && !categories.isEmpty()) ? categories : null;
        List<Event> events = eventRepository.findEventsByAdminFilters(usersParam, statesParam, categoriesParam, start, end, pageable).getContent();
        return getEventFullDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (request.getStateAction() != null) {
            if ("PUBLISH_EVENT".equals(request.getStateAction())) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if ("REJECT_EVENT".equals(request.getStateAction())) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it's already published");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
            }
            if (event.getPublishedOn() != null && request.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour after publication");
            }
        }

        updateEventFields(event, request);

        Event saved = eventRepository.save(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        return EventMapper.toFullDto(saved, confirmedRequests, 0L);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Pageable pageable, String ip) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end = rangeEnd != null ? rangeEnd : MAX_DATETIME;

        Sort sortBy = Sort.by("eventDate");
        if ("VIEWS".equals(sort)) {
            sortBy = Sort.by("id");
        }

        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), sortBy);

        String textPattern = text != null ? "%" + text.toLowerCase() + "%" : null;
        List<Long> categoriesParam = (categories != null && !categories.isEmpty()) ? categories : null;

        List<Event> events = eventRepository.findPublishedEventsByFilters(
                text, textPattern, categoriesParam, paid, start, end, sortedPageable).getContent();

        if (onlyAvailable != null && onlyAvailable) {
            events = events.stream()
                    .filter(e -> {
                        Long confirmed = requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED);
                        return e.getParticipantLimit() == 0 || confirmed < e.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        saveHit("/events", ip);

        List<EventShortDto> result = getEventShortDtos(events);

        if ("VIEWS".equals(sort)) {
            result.sort((a, b) -> Long.compare(b.getViews() != null ? b.getViews() : 0L,
                                                a.getViews() != null ? a.getViews() : 0L));
        }

        return result;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, String ip) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        Long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        Long currentViews = getViews(eventId);

        saveHit("/events/" + eventId, ip);

        Long views = currentViews + 1;

        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    private void updateEventFields(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(LocationMapper.toEntity(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(LocationMapper.toEntity(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private List<EventShortDto> getEventShortDtos(List<Event> events) {
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(events);
        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(e -> EventMapper.toShortDto(e,
                        confirmedRequestsMap.getOrDefault(e.getId(), 0L),
                        viewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private List<EventFullDto> getEventFullDtos(List<Event> events) {
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(events);
        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(e -> EventMapper.toFullDto(e,
                        confirmedRequestsMap.getOrDefault(e.getId(), 0L),
                        viewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> map = new HashMap<>();
        for (Long eventId : eventIds) {
            map.put(eventId, requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
        }
        return map;
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        Map<Long, Long> viewsMap = new HashMap<>();
        if (events.isEmpty()) {
            return viewsMap;
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);

        try {
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);
            for (ViewStatsDto stat : stats) {
                String uri = stat.getUri();
                if (uri.startsWith("/events/")) {
                    try {
                        Long eventId = Long.parseLong(uri.substring("/events/".length()));
                        viewsMap.put(eventId, stat.getHits());
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        } catch (Exception ignore) {
        }

        for (Event event : events) {
            viewsMap.putIfAbsent(event.getId(), 0L);
        }

        return viewsMap;
    }

    private Long getViews(Long eventId) {
        try {
            String uri = "/events/" + eventId;
            List<String> uris = Collections.singletonList(uri);
            LocalDateTime start = LocalDateTime.now().minusYears(1);
            LocalDateTime end = LocalDateTime.now().plusYears(1);
            List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);
            if (stats == null || stats.isEmpty()) {
                return 0L;
            }
            return stats.stream()
                    .filter(s -> uri.equals(s.getUri()))
                    .findFirst()
                    .map(ViewStatsDto::getHits)
                    .orElse(0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    private void saveHit(String uri, String ip) {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        try {
            statsClient.hit(hit);
        } catch (Exception ignore) {
        }
    }
}

