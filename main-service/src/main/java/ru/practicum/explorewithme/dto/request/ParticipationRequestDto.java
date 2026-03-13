package ru.practicum.explorewithme.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.model.request.Status;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private Long event;
    private Long requester;
    private String created;
    private Status status;
}
