package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.ewm.entity.Compilation;
import ru.practicum.ewm.entity.Event;
import ru.practicum.ewm.entity.enums.RequestStatus;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.ParticipationRequestRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto compilationDto) {
        Set<Event> events = new HashSet<>();
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findByIdIn(new ArrayList<>(compilationDto.getEvents())));
        }

        Compilation compilation = Compilation.builder()
                .events(events)
                .pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false)
                .title(compilationDto.getTitle())
                .build();

        Compilation saved = compilationRepository.save(compilation);
        return getCompilationDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (request.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findByIdIn(new ArrayList<>(request.getEvents())));
            compilation.setEvents(events);
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        Compilation saved = compilationRepository.save(compilation);
        return getCompilationDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilations.stream()
                .map(this::getCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
        return getCompilationDto(compilation);
    }

    private CompilationDto getCompilationDto(Compilation compilation) {
        Map<Long, Long> confirmedRequestsMap = new HashMap<>();
        Map<Long, Long> viewsMap = new HashMap<>();

        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            List<Long> eventIds = compilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());

            for (Long eventId : eventIds) {
                confirmedRequestsMap.put(eventId, requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
                viewsMap.put(eventId, 0L); // Views would be fetched from stats service if needed
            }
        }

        return CompilationMapper.toDto(compilation, confirmedRequestsMap, viewsMap);
    }
}

