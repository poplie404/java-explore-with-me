package ru.practicum.ewm.entity;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;
}




