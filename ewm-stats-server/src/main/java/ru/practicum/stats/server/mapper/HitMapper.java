package ru.practicum.stats.server.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.stats.server.entity.Hit;

public class HitMapper {
    public static Hit toEntity(EndpointHitDto dto) {
        Hit hit = new Hit();
        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());
        return hit;
    }

    public static EndpointHitDto toDto(Hit hit) {
        return EndpointHitDto.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }
}
