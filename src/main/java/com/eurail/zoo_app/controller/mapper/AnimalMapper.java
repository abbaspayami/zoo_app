package com.eurail.zoo_app.controller.mapper;

import com.eurail.zoo_app.controller.dto.AnimalCreateDto;
import com.eurail.zoo_app.controller.dto.AnimalResponseDto;
import com.eurail.zoo_app.controller.dto.AnimalUpdateDto;
import com.eurail.zoo_app.respository.entity.Animal;
import org.mapstruct.*;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnimalMapper {

    /**
     * Map AnimalCreateDto to Animal entity.
     * Sets created and updated timestamps to now.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", expression = "java(java.time.Instant.now())")
    @Mapping(target = "updated", expression = "java(java.time.Instant.now())")
    Animal toEntity(AnimalCreateDto dto);

    /**
     * Map Animal entity to response DTO.
     */
    AnimalResponseDto toResponseDto(Animal entity);

    /**
     * Update existing Animal from DTO.
     * Null fields in DTO are ignored.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AnimalUpdateDto dto, @MappingTarget Animal target);
}
