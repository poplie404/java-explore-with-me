package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats.server.entity.Hit;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.repository.HitRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final HitRepository repository;
    private final HitMapper mapper;

    @Transactional
    public EndpointHitDto save(EndpointHitDto dto) {
        if (dto.getTimestamp() == null) {
            dto.setTimestamp(LocalDateTime.now());
        }

        Hit hit = mapper.toEntity(dto);
        repository.save(hit);
        return mapper.toDto(hit);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        // 🔥 ВСЕГДА запрашиваем БЕЗ фильтра uris, потом фильтруем в памяти
        List<ViewStatsDto> allStats = unique
                ? repository.findUniqueStats(start, end)
                : repository.findStats(start, end);

        System.out.println("🔥 ALL STATS FROM DB: " + allStats);  // ← ДОБАВЬ!

        if (uris != null && !uris.isEmpty()) {
            Map<String, Long> hitsMap = allStats.stream()
                    .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));

            return uris.stream()
                    .map(uri -> ViewStatsDto.builder()
                            .app("ewm-main-service")
                            .uri(uri)
                            .hits(hitsMap.getOrDefault(uri, 0L))
                            .build())
                    .collect(Collectors.toList());
        }

        return allStats;
    }
}
