package ru.practicum.explorewithme.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.location.LocationDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String annotation;
    private CategoryDto category;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer confirmedRequests;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createdOn;
    private String description;
    private String eventDate;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private String state;
    private String title;
    private Integer views;
}
