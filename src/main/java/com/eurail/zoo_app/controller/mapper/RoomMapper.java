package com.eurail.zoo_app.controller.mapper;

import com.eurail.zoo_app.controller.dto.RoomCreateDto;
import com.eurail.zoo_app.controller.dto.RoomResponseDto;
import com.eurail.zoo_app.controller.dto.RoomUpdateDto;
import com.eurail.zoo_app.respository.entity.Room;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomMapper {
    RoomResponseDto toResponseDto(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updated", expression = "java(java.time.Instant.now())")
    Room toEntity(RoomCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(RoomUpdateDto dto, @MappingTarget Room target);
}
