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
import java.util.List;
import java.util.Map;
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
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found. id = " + id));
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
     * Calculates statistics for favourite rooms.
     * Each room is returned with the number of animals marking it as favourite.
     * Rooms with no favourites are excluded.
     *
     * @return list of FavouriteRoomStatsDto containing room title and favourite count
     */
    @Override
    public List<FavouriteRoomStatsDto> favouriteRoomStats() {
        List<Animal> all = repository.findAll();

        Map<String, Long> counts = all.stream()
                .flatMap(a -> a.getFavouriteRoomIds().stream())
                .collect(Collectors.groupingBy(r -> r, Collectors.counting()));

        return counts.entrySet().stream()
                .filter(e -> roomService.exists(e.getKey()))
                .map(entry -> {
                    String roomId = entry.getKey();
                    Long count = entry.getValue();
                    String title = roomService.get(roomId).getTitle();
                    return new FavouriteRoomStatsDto(title, count);
                })
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


}
