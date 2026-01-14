package ru.practicum.ewm.mapper;

import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.ewm.entity.ParticipationRequest;

public class ParticipationRequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }
}

