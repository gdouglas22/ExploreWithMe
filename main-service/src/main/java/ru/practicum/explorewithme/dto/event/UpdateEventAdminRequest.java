package ru.practicum.explorewithme.dto.event;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.explorewithme.model.location.Location;
import ru.practicum.explorewithme.utils.validation.annotation.DateTimeTwoHoursLater;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @DateTimeTwoHoursLater
    private String eventDate;
    private Location location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;

    public boolean hasAnnotation() {
        return annotation != null;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public boolean hasEventDate() {
        return eventDate != null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasStateAction() {
        return stateAction != null;
    }

    public boolean hasTitle() {
        return title != null;
    }
}
