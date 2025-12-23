package com.eurail.zoo_app.service.impl;

import com.eurail.zoo_app.controller.dto.FavouriteRoomStatsDto;
import com.eurail.zoo_app.exception.BadRequestException;
import com.eurail.zoo_app.exception.ResourceNotFoundException;
import com.eurail.zoo_app.respository.AnimalRepository;
import com.eurail.zoo_app.respository.entity.Animal;
import com.eurail.zoo_app.service.AnimalService;
import com.eurail.zoo_app.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnimalServiceImpl implements AnimalService {

    private static final Logger log = LoggerFactory.getLogger(AnimalServiceImpl.class);

    private final AnimalRepository repository;
    private final RoomService roomService;

    public AnimalServiceImpl(AnimalRepository repository, RoomService roomService) {
        this.repository = repository;
        this.roomService = roomService;
    }

    /**
     * Creates a new animal in the database.
     * Sets the created and updated timestamps to now.
     *
     * @param animal the Animal entity to create
     * @return the created Animal with generated ID and timestamps
     */
    @Override
    public Animal create(Animal animal) {
        log.debug("Creating new animal: {}", animal);

        // Validate room references exist
        validateRoomReferences(animal.getCurrentRoomId(), animal.getFavouriteRoomIds());

        animal.setCreated(Instant.now());
        animal.setUpdated(Instant.now());

        return repository.save(animal);
    }

    /**
     * Retrieves an animal by its ID.
     *
     * @param id the ID of the animal
     * @return the Animal entity
     * @throws ResourceNotFoundException if no animal with the given ID exists
     */
    @Override
    public Animal get(String id) {
        log.debug("Getting animal by id={}", id);
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found: " + id));
    }

    /**
     * Updates an existing animal.
     * Sets the updated timestamp to now.
     *
     * @param id     the ID of the animal to update
     * @param animal the animal object containing updated fields
     * @return the updated Animal
     */
    @Override
    public Animal update(String id, Animal animal) {
        log.debug("Updating animal id={} with animal={}", id, animal);

        // Validate room references exist
        validateRoomReferences(animal.getCurrentRoomId(), animal.getFavouriteRoomIds());

        animal.setUpdated(Instant.now());

        return repository.save(animal);
    }

    /**
     * Deletes an animal by its ID.
     *
     * @param id the ID of the animal to delete
     * @throws ResourceNotFoundException if no animal with the given ID exists
     */
    @Override
    public void delete(String id) {
        log.debug("Deleting animal id={}", id);

        // validate existence (throws 404)
        get(id);

        repository.deleteById(id);
    }

    /**
     * Lists animals in a specific room with pagination and sorting.
     *
     * @param roomId the room ID to filter animals
     * @param sortBy allowed values: "title", "located"
     * @param order  allowed values: "asc", "desc"
     * @param page   zero-based page index
     * @param size   number of items per page
     * @return a Page of Animal entities
     */
    @Override
    public Page<Animal> listAnimalsInRoom(String roomId, String sortBy, String order, int page, int size) {
        log.debug("Listing animals in room={} sortBy={} order={} page={} size={}",
                roomId, sortBy, order, page, size);

        roomService.get(roomId); // validate room exists

        // Validate sortBy
        if (!sortBy.equals("title") && !sortBy.equals("located")) {
            log.error("Invalid sort field: {}", sortBy);
            throw new BadRequestException("Invalid sort field: " + sortBy + ". Allowed: title, located");
        }

        // Validate order
        Sort.Direction direction;
        if (order.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        } else if (order.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        } else {
            log.error("Invalid order: {}", order);
            throw new BadRequestException("Invalid order: " + order + ". Allowed: asc, desc");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return repository.findByCurrentRoomId(roomId, pageable);
    }

    /**
     * Calculates favourite-room statistics grouped by room title.
     * Each returned entry contains the room title and the number of animals
     * that have any room with that title marked as favourite.
     * Rooms with zero favourites are excluded.
     *
     * @return list of FavouriteRoomStatsDto containing room title and favourite count
     */
    @Override
    public List<FavouriteRoomStatsDto> favouriteRoomStats() {
        Map<String, Long> counts = new HashMap<>();

        for (Animal animal : repository.findAll()) {
            if (animal.getFavouriteRoomIds() == null) continue;

            for (String roomId : animal.getFavouriteRoomIds()) {
                if (!roomService.exists(roomId)) continue; // skip stale references
                String title = roomService.get(roomId).getTitle();
                counts.put(title, counts.getOrDefault(title, 0L) + 1);
            }
        }

        return counts.entrySet().stream()
                .map(e -> new FavouriteRoomStatsDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }


    /**
     * Assigns an animal to a room. Can be used for initial placement or moving the animal.
     *
     * @param animalId the ID of the animal
     * @param roomId   the ID of the room
     * @return the updated Animal with the new room assigned
     * @throws ResourceNotFoundException if the animal or room does not exist
     */
    @Override
    public Animal assignAnimalToRoom(String animalId, String roomId) {
        log.debug("Moving animal id={} to room={}", animalId, roomId);

        Animal animal = get(animalId);
        roomService.get(roomId); // Validate room exists

        animal.setCurrentRoomId(roomId);
        animal.setUpdated(Instant.now());

        return repository.save(animal);
    }

    /**
     * Removes an animal from its current room.
     *
     * @param id the ID of the animal
     * @return the updated Animal with room cleared
     * @throws ResourceNotFoundException if the animal does not exist
     */
    @Override
    public Animal removeAnimalFromRoom(String id) {
        log.debug("Removing animal id={} from current room", id);

        Animal a = get(id);
        a.setCurrentRoomId(null);
        a.setUpdated(Instant.now());

        return repository.save(a);
    }

    /**
     * Adds a room to the animal's favourites.
     *
     * @param id     the ID of the animal
     * @param roomId the ID of the room to add
     * @return the updated Animal
     * @throws ResourceNotFoundException if the animal or room does not exist
     */
    @Override
    public Animal assignFavouriteRoom(String id, String roomId) {
        log.debug("Adding favourite room={} to animal={}", roomId, id);

        Animal a = get(id);
        roomService.get(roomId); // validate room existence

        a.getFavouriteRoomIds().add(roomId);
        a.setUpdated(Instant.now());

        return repository.save(a);
    }

    /**
     * Removes a room from the animal's favourites.
     *
     * @param animalId the ID of the animal
     * @param roomId   the ID of the room to remove
     * @return the updated Animal
     * @throws ResourceNotFoundException if the animal or room does not exist
     * @throws BadRequestException       if the room is not currently a favourite
     */
    @Override
    public Animal unassignFavouriteRoom(String animalId, String roomId) {
        log.debug("Removing favourite room={} from animal={}", roomId, animalId);

        Animal animal = get(animalId);

        // validate room exists
        roomService.get(roomId);

        if (!animal.getFavouriteRoomIds().contains(roomId)) {
            throw new BadRequestException(
                    "Room " + roomId + " is not in favourites for animal " + animalId
            );
        }

        animal.getFavouriteRoomIds().remove(roomId);
        animal.setUpdated(Instant.now());

        return repository.save(animal);
    }

    private void validateRoomReferences(String currentRoomId, Set<String> favouriteRoomIds) {
        if (currentRoomId != null && !currentRoomId.isBlank() && !roomService.exists(currentRoomId)) {
                throw new ResourceNotFoundException("Room not found: " + currentRoomId);
            }

        if (favouriteRoomIds != null && !favouriteRoomIds.isEmpty()) {
            for (String roomId : favouriteRoomIds) {
                if (!roomService.exists(roomId)) {
                    throw new ResourceNotFoundException("Room not found: " + roomId);
                }
            }
        }
    }


}
