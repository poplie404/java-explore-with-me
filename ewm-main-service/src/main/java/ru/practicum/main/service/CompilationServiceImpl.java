package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.main.entity.Compilation;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            for (Long eventId : dto.getEvents()) {
                if (!eventRepository.existsById(eventId)) {
                    throw new IllegalArgumentException("Event id=" + eventId + " not found");
                }
            }
        }
        Compilation compilation = compilationMapper.toEntity(dto);
        compilation = repository.save(compilation);
        return compilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = findById(compId);
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            for (Long eventId : request.getEvents()) {
                if (!eventRepository.existsById(eventId)) {
                    throw new IllegalArgumentException("Event id=" + eventId + " not found");
                }
            }
        }
        compilationMapper.updateEntity(compilation, request);
        compilation = repository.save(compilation);
        return compilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        repository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (pinned != null) {
            return repository.findAllByPinned(pinned, pageable).stream()
                    .map(compilationMapper::toDto)
                    .collect(Collectors.toList());
        }
        return repository.findAll(pageable).stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = findById(compId);
        return compilationMapper.toDto(compilation);
    }

    private Compilation findById(Long compId) {
        return repository.findById(compId)
                .orElseThrow(() -> new IllegalArgumentException("Compilation id=" + compId + " not found"));
    }
}
