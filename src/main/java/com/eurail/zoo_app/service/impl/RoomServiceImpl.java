package com.eurail.zoo_app.service.impl;

import com.eurail.zoo_app.exception.ResourceNotFoundException;
import com.eurail.zoo_app.respository.RoomRepository;
import com.eurail.zoo_app.respository.entity.Room;
import com.eurail.zoo_app.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RoomServiceImpl implements RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository repo;

    public RoomServiceImpl(RoomRepository repo) {
        this.repo = repo;
    }

    /**
     * Creates a new room and sets creation and update timestamps.
     *
     * @param room the room to create
     * @return the created {@link Room}
     */
    @Override
    public Room create(Room room) {
        log.debug("Creating new room: {}", room);
        room.setCreated(Instant.now());
        room.setUpdated(Instant.now());
        return repo.save(room);
    }

    /**
     * Retrieves a room by its ID.
     *
     * @param id the ID of the room
     * @return the {@link Room} entity
     * @throws ResourceNotFoundException if no room with the given ID exists
     */
    @Override
    public Room get(String id) {
        log.debug("Getting room by id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + id));
    }

    /**
     * Updates an existing room by ID.
     *
     * @param id      the ID of the room to update
     * @param changes the new values for the room
     * @return the updated {@link Room}
     * @throws ResourceNotFoundException if no room with the given ID exists
     */
    @Override
    public Room update(String id, Room changes) {
        log.debug("Updating room id={} with changes={}", id, changes);
        Room existing = get(id);
        existing.setTitle(changes.getTitle());
        existing.setUpdated(Instant.now());
        return repo.save(existing);
    }

    /**
     * Deletes a room by its ID.
     *
     * @param id the ID of the room to delete
     * @throws ResourceNotFoundException if no room with the given ID exists
     */
    @Override
    public void delete(String id) {
        log.debug("Deleting room id={}", id);
        get(id); // validate existence
        repo.deleteById(id);
    }

    public boolean exists(String id) {
        return repo.existsById(id);
    }

}

