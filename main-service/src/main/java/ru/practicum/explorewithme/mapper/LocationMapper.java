package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.location.LocationDto;
import ru.practicum.explorewithme.model.location.Location;

public final class LocationMapper {
    public static Location mapToLocation(LocationDto dto) {
        return Location.builder()
                .lon(dto.getLon())
                .lat(dto.getLat())
                .build();
    }

    public static LocationDto mapToDto(Location location) {
        return LocationDto.builder()
                .lon(location.getLon())
                .lat(location.getLat())
                .build();
    }
}
