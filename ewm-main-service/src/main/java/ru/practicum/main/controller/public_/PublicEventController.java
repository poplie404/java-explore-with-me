package ru.practicum.main.controller.public_;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.main.service.EventService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    // GET /events/{id} — увеличивает хит
    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.getEventById(eventId, request);
    }

    @GetMapping
    public List<EventShortDto> getEvents(HttpServletRequest request,
                                         @RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(required = false) Boolean onlyAvailable,
                                         @RequestParam(required = false) EventSort sort,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size) {

        List<EventShortDto> events = eventService.getPublicEvents(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        // GET /events — тоже новый хит
        statsClient.hit(EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri("/events")
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());

        return events;
    }

    public enum EventSort { EVENT_DATE, VIEWS }
}

