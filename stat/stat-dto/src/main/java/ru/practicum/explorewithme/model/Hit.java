package ru.practicum.explorewithme.model;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Hit {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private LocalDateTime created;
}
