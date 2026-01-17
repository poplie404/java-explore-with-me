package ru.practicum.ewm.mapper;

import ru.practicum.dto.LocationDto;
import ru.practicum.ewm.entity.Location;

public class LocationMapper {
    public static LocationDto toDto(Location entity) {
        return LocationDto.builder()
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }

    public static Location toEntity(LocationDto dto) {
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }
}