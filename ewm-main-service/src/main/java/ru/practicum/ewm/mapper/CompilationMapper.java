package ru.practicum.ewm.mapper;

import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.ewm.entity.Compilation;
import java.util.Map;

import java.util.Set;
import java.util.stream.Collectors;

public class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation, Map<Long, Long> confirmedRequestsMap, Map<Long, Long> viewsMap) {
        Set<EventShortDto> events = null;
        if (compilation.getEvents() != null) {
            events = compilation.getEvents().stream()
                    .map(event -> EventMapper.toShortDto(
                            event,
                            confirmedRequestsMap.getOrDefault(event.getId(), 0L),
                            viewsMap.getOrDefault(event.getId(), 0L)))
                    .collect(Collectors.toSet());
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}