package ru.practicum.main.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.main.entity.Request;
import ru.practicum.main.entity.RequestStatus;
import ru.practicum.main.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestMapper {

    private final EventRepository eventRepository;

    public ParticipationRequestDto toDto(Request request) {
        EventShortDto eventShortDto = eventRepository.findById(request.getEventId())
                .map(EventMapper::toEventShortDto)
                .orElse(createStubEvent(request.getEventId()));

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(eventShortDto)
                .requester(request.getRequesterId())
                .status(request.getStatus().name())
                .build();
    }

    public List<ParticipationRequestDto> toDtoList(List<Request> requests) {
        return requests.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public static Request toEntity(Long userId, Long eventId) {
        return Request.builder()
                .requesterId(userId)
                .eventId(eventId)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();
    }

    private EventShortDto createStubEvent(Long eventId) {
        return EventShortDto.builder()
                .id(eventId)
                .title("Event #" + eventId)
                .annotation("Event not found")
                .build();
    }
}
