package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.main.entity.Event;
import ru.practicum.main.entity.Request;
import ru.practicum.main.entity.RequestStatus;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.RequestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        // Проверяем событие
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        // Проверяем дубликат
        if (requestRepository.findFirstByEventIdAndRequesterId(eventId, userId) != null) {
            throw new IllegalStateException("Request already exists");
        }

        Request request = RequestMapper.toEntity(userId, eventId);
        request = requestRepository.save(request);
        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getOwnRequests(Long userId) {
        return requestMapper.toDtoList(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        // Проверяем: пользователь владелец события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event id=" + eventId + " not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new IllegalArgumentException("User is not event owner");
        }

        return requestMapper.toDtoList(requestRepository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request id=" + requestId + " not found"));

        if (!request.getRequesterId().equals(userId)) {
            throw new IllegalArgumentException("Request does not belong to user");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel request in status " + request.getStatus());
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        return requestMapper.toDto(request);
    }
}
