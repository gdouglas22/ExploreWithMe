package ru.practicum.explorewithme.dto.event;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.practicum.explorewithme.dto.location.LocationDto;
import ru.practicum.explorewithme.model.event.StateAction;
import ru.practicum.explorewithme.utils.validation.annotation.DateTimeTwoHoursLater;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000)
    private String description;
    @DateTimeTwoHoursLater
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
    @Size(min = 3, max = 120)
    private String title;

    public boolean hasAnnotation() {
        return !(annotation == null || annotation.isBlank());
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasEventDate() {
        return !(eventDate == null || eventDate.isBlank());
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
        return !(title == null || title.isBlank());
    }
}
