package com.eurail.zoo_app.controller;

import com.eurail.zoo_app.controller.dto.*;
import com.eurail.zoo_app.controller.mapper.AnimalMapper;
import com.eurail.zoo_app.respository.entity.Animal;
import com.eurail.zoo_app.service.AnimalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/animals",
        produces = MediaType.APPLICATION_JSON_VALUE,  // All methods return JSON
        consumes = MediaType.APPLICATION_JSON_VALUE)  // All methods accept JSON
@Validated
public class AnimalController {

    private static final Logger log = LoggerFactory.getLogger(AnimalController.class);

    private final AnimalService service;
    private final AnimalMapper mapper;

    public AnimalController(AnimalService service, AnimalMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Creates a new Animal.
     *
     * @param dto        the input data for creating the animal
     * @param uriBuilder used to build the Location URI of the created resource
     * @return 201 Created with the created Animal representation
     */
    @PostMapping
    public ResponseEntity<AnimalResponseDto> create(@Valid @RequestBody AnimalCreateDto dto,
                                                    UriComponentsBuilder uriBuilder) {
        log.info("Creating new animal: {}", dto);
        Animal entity = mapper.toEntity(dto);
        Animal created = service.create(entity);
        AnimalResponseDto resp = mapper.toResponseDto(created);

        URI location = uriBuilder.path("/animals/{id}")
                .buildAndExpand(created.getId()).toUri();
        log.info("Animal created with ID: {}", created.getId());

        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Retrieves an animal by its ID.
     *
     * @param id the animal ID
     * @return 200 OK with the animal data
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDto> get(@PathVariable String id) {
        log.info("Fetching animal with ID: {}", id);
        Animal a = service.get(id);
        return ResponseEntity.ok(mapper.toResponseDto(a));
    }

    /**
     * Updates an existing animal.
     *
     * @param id  the animal ID
     * @param dto the fields to update
     * @return 200 OK with the updated animal data
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponseDto> update(@PathVariable String id,
                                                    @Valid @RequestBody AnimalUpdateDto dto) {
        log.debug("Updating animal ID {}: {}", id, dto);
        Animal existing = service.get(id);
        mapper.updateFromDto(dto, existing);
        Animal updated = service.update(id, existing);
        log.debug("Animal updated ID: {}", updated.getId());
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }


    /**
     * Deletes an animal by its ID.
     *
     * @param id the animal ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting animal with ID: {}", id);
        service.delete(id);
        log.info("Animal deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Place an animal into a room (first placement or move).
     *
     * @param animalId the animal ID
     * @param dto contains roomId
     * @return 200 OK with updated animal
     */
    @PostMapping("/{animalId}/place")
    public ResponseEntity<AnimalResponseDto> placeAnimal(@PathVariable String animalId,
                                                         @Valid @RequestBody PlaceRequestDto dto) {
        log.info("Placing animal {} into room {}", animalId, dto.getRoomId());
        Animal updated = service.assignAnimalToRoom(animalId, dto.getRoomId());
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    /**
     * Move an animal from one room to another.
     *
     * @param animalId the animal ID
     * @param dto contains target roomId
     * @return 200 OK with updated animal
     */
    @PutMapping("/{animalId}/move")
    public ResponseEntity<AnimalResponseDto> moveAnimal(@PathVariable String animalId,
                                                        @Valid @RequestBody PlaceRequestDto dto) {
        log.info("Moving animal {} to room {}", animalId, dto.getRoomId());
        Animal updated = service.assignAnimalToRoom(animalId, dto.getRoomId());
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    /**
     * Remove an animal from its current room.
     *
     * @param id the animal ID
     * @return 200 OK with updated animal (room cleared)
     */
    @DeleteMapping("/{id}/room")
    public ResponseEntity<AnimalResponseDto> removeFromRoom(@PathVariable String id) {
        log.info("Removing animal ID {} from current room", id);
        Animal updated = service.removeAnimalFromRoom(id);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    /**
     * Assign a room to an animal as a favourite.
     *
     * @param id  the ID of the animal
     * @param dto contains the roomId to be added as favourite
     * @return 200 OK with the updated Animal data
     */
    @PostMapping("/{id}/favourites")
    public ResponseEntity<AnimalResponseDto> assignFavouriteRoom(@PathVariable String id,
                                                                 @Valid @RequestBody FavouriteRequestDto dto) {
        log.info("Assigning room ID {} as favourite to animal ID {}", dto.getRoomId(), id);
        Animal updated = service.assignFavouriteRoom(id, dto.getRoomId());
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }


    /**
     * Remove a room from the animal's favourite rooms.
     *
     * @param id     animal ID
     * @param roomId room ID to remove
     * @return 200 OK with updated animal
     */
    @DeleteMapping("/{id}/favourites/{roomId}")
    public ResponseEntity<AnimalResponseDto> unassignFavouriteRoom(@PathVariable String id,
                                                             @PathVariable String roomId) {
        log.info("Removing room {} from favourites of animal {}", roomId, id);
        Animal updated = service.unassignFavouriteRoom(id, roomId);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    /**
     * Returns paginated and sorted list of animals in a specific room.
     *
     * @param roomId the room ID to filter animals by
     * @param sortBy allowed values: "title" or "located"
     * @param order  allowed values: "asc" or "desc"
     * @param page   zero-based page index
     * @param size   number of elements per page
     * @return a structured paginated response containing animals
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<AnimalPageResponseDto> listAnimalsInRoom(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching animals for roomId={}, sortBy={}, order={}, page={}, size={}",
                roomId, sortBy, order, page, size);

        Page<Animal> result = service.listAnimalsInRoom(roomId, sortBy, order, page, size);

        List<AnimalResponseDto> mapped = result.getContent()
                .stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        AnimalPageResponseDto response = new AnimalPageResponseDto(
                mapped,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }


}
