package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.server.entity.Hit;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {
    private final HitRepository repository;
    private final HitMapper mapper;

    @Transactional
    public EndpointHitDto save(EndpointHitDto dto) {
        Hit hit = mapper.toEntity(dto);
        Hit saved = repository.save(hit);
        return mapper.toDto(saved);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        validateDates(start, end);

        if (uris != null && !uris.isEmpty()) {
            return unique
                    ? repository.findUniqueStatsByUris(start, end, uris)
                    : repository.findStatsByUris(start, end, uris);
        } else {
            return unique
                    ? repository.findUniqueStats(start, end)
                    : repository.findStats(start, end);
        }
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }
}