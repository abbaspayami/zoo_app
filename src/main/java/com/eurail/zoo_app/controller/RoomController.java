package com.eurail.zoo_app.controller;

import com.eurail.zoo_app.controller.dto.FavouriteRoomStatsDto;
import com.eurail.zoo_app.controller.dto.RoomCreateDto;
import com.eurail.zoo_app.controller.dto.RoomResponseDto;
import com.eurail.zoo_app.controller.dto.RoomUpdateDto;
import com.eurail.zoo_app.respository.entity.Room;
import com.eurail.zoo_app.controller.mapper.RoomMapper;
import com.eurail.zoo_app.service.AnimalService;
import com.eurail.zoo_app.service.RoomService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rooms",
        produces = MediaType.APPLICATION_JSON_VALUE,  // All methods return JSON
        consumes = MediaType.APPLICATION_JSON_VALUE)  // All methods accept JSON)
@Validated  // Required for @PathVariable validation
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService service;
    private final RoomMapper mapper;
    private final AnimalService animalService;

    public RoomController(RoomService service, RoomMapper mapper, AnimalService animalService) {
        this.service = service;
        this.mapper = mapper;
        this.animalService = animalService;
    }

    /**
     * Create a new Room.
     *
     * @param dto the data for creating a new room
     * @return 201 Created with the created Room data
     */
    @PostMapping
    public ResponseEntity<RoomResponseDto> create(@Valid @RequestBody RoomCreateDto dto) {
        log.info("Creating room: {}", dto.getTitle());
        Room room = mapper.toEntity(dto);
        Room created = service.create(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDto(created));
    }

    /**
     * Get a Room by its ID.
     *
     * @param id the room ID
     * @return 200 OK with the Room data
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDto> get(@PathVariable String id) {
        log.info("Fetching room id={}", id);
        Room room = service.get(id);
        return ResponseEntity.ok(mapper.toResponseDto(room));
    }

    /**
     * Update an existing Room.
     *
     * @param id  the room ID
     * @param dto the fields to update
     * @return 200 OK with the updated Room data
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponseDto> update(@PathVariable String id, @Valid @RequestBody RoomUpdateDto dto) {
        log.info("Updating room id={}", id);
        Room existing = service.get(id);
        mapper.updateFromDto(dto, existing);
        Room updated = service.update(id, existing);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    /**
     * Delete a Room by its ID.
     *
     * @param id the room ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting room id={}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get statistics for favourite rooms.
     * Each room in the response contains its title and the number of animals
     * that have this room as a favourite.
     * Rooms with no animals marking them as favourite are not included.
     *
     * @return 200 OK with a list of FavouriteRoomStatsDto
     */
    @GetMapping("/favourites/stats")
    public ResponseEntity<List<FavouriteRoomStatsDto>> getFavouriteRoomStats() {
        log.info("Fetching favourite room statistics");
        List<FavouriteRoomStatsDto> stats = animalService.favouriteRoomStats();
        return ResponseEntity.ok(stats);
    }
}

