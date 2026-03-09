package ru.practicum.explorewithme.dto.common;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.explorewithme.model.category.Category;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.location.Location;
import ru.practicum.explorewithme.model.user.User;

@Getter
@Setter
public class EventFullDtoData {
    private Event event;
    private Category category;
    private User initiator;
    private Location location;

    public EventFullDtoData(Event event) {
        this.event = event;
    }

    public EventFullDtoData(Event event, Category category, User initiator, Location location) {
        this.event = event;
        this.category = category;
        this.initiator = initiator;
        this.location = location;
    }
}
