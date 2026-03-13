package ru.practicum.explorewithme.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMapper {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event mapToNewEvent(NewEventDto newEventDto) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), formatter))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .state(State.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventFullDto mapToFullDto(Event event) {
        EventFullDto dto = EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn().format(formatter))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(formatter))
                .initiator(UserMapper.mapToShortDto(event.getInitiator()))
                .location(LocationMapper.mapToDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .build();

        String publishedOn = event.getPublishedOn() == null ? null : event.getPublishedOn().format(formatter);
        dto.setPublishedOn(publishedOn);

        return dto;
    }

    public static void updateEventData(Event currentEvent, UpdateEventUserRequest updatedEvent) {
        if (updatedEvent.hasAnnotation()) {
            currentEvent.setAnnotation(updatedEvent.getAnnotation());
        }
        if (updatedEvent.hasDescription()) {
            currentEvent.setDescription(updatedEvent.getDescription());
        }
        if (updatedEvent.hasEventDate()) {
            currentEvent.setEventDate(LocalDateTime.parse(updatedEvent.getEventDate(), formatter));
        }
        if (updatedEvent.hasLocation()) {
            currentEvent.setLocation(LocationMapper.mapToLocation(updatedEvent.getLocation()));
        }
        if (updatedEvent.hasPaid()) {
            currentEvent.setPaid(updatedEvent.getPaid());
        }
        if (updatedEvent.hasParticipantLimit()) {
            currentEvent.setParticipantLimit(updatedEvent.getParticipantLimit());
        }
        if (updatedEvent.hasStateAction()) {
            switch (updatedEvent.getStateAction()) {
                case CANCEL_REVIEW -> currentEvent.setState(State.CANCELED);
                case SEND_TO_REVIEW -> currentEvent.setState(State.PENDING);
                default -> currentEvent.setState(State.PENDING);
            }
        }
        if (updatedEvent.hasTitle()) {
            currentEvent.setTitle(updatedEvent.getTitle());
        }
    }
}
