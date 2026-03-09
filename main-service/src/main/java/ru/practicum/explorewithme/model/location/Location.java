package ru.practicum.explorewithme.model.location;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lon", "lat"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"lat", "lon"})
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lon", nullable = false)
    private Double lon;
}
