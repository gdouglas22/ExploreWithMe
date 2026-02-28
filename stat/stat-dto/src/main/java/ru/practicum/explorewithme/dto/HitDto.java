package ru.practicum.explorewithme.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class HitDto {
    private String app;
    private String uri;
    private Long hits;
}
