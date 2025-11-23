package com.eurail.zoo_app.service;

import com.eurail.zoo_app.respository.entity.Room;
import com.eurail.zoo_app.exception.ResourceNotFoundException;
import com.eurail.zoo_app.respository.RoomRepository;
import com.eurail.zoo_app.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private RoomRepository repo;

    @InjectMocks
    private RoomServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRoom() {
        Room input = new Room();
        input.setTitle("Hall");

        when(repo.save(any(Room.class))).thenAnswer(inv -> {
            Room saved = inv.getArgument(0);
            saved.setId("1");
            return saved;
        });

        Room result = service.create(input);

        assertEquals("Hall", result.getTitle());
        assertEquals("1", result.getId());
        assertNotNull(result.getCreated());
        assertNotNull(result.getUpdated());
        verify(repo).save(any(Room.class));
    }

    @Test
    void testGetRoom() {
        Room room = new Room();
        room.setId("10");
        room.setTitle("Big Room");

        when(repo.findById("10")).thenReturn(Optional.of(room));

        Room result = service.get("10");

        assertEquals("10", result.getId());
        assertEquals("Big Room", result.getTitle());
    }

    @Test
    void testGetRoomNotFound() {
        when(repo.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.get("99"));
    }

    @Test
    void testUpdateRoom() {
        Room existing = new Room();
        existing.setId("5");
        existing.setTitle("Old");

        Room changes = new Room();
        changes.setTitle("New Title");

        when(repo.findById("5")).thenReturn(Optional.of(existing));
        when(repo.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));

        Room result = service.update("5", changes);

        assertEquals("New Title", result.getTitle());
        assertNotNull(result.getUpdated());
        verify(repo).save(existing);
    }

    @Test
    void testDeleteRoom() {
        Room room = new Room();
        room.setId("7");

        when(repo.findById("7")).thenReturn(Optional.of(room));

        service.delete("7");

        verify(repo).deleteById("7");
    }


    @Test
    void testDeleteRoomNotFound() {
        when(repo.findById("77")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete("77"));
    }

    @Test
    void testExistsTrue() {
        when(repo.existsById("5")).thenReturn(true);

        assertTrue(service.exists("5"));
        verify(repo).existsById("5");
    }

    @Test
    void testExistsFalse() {
        when(repo.existsById("999")).thenReturn(false);

        assertFalse(service.exists("999"));
        verify(repo).existsById("999");
    }

}
