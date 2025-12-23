package com.eurail.zoo_app.service;

import com.eurail.zoo_app.respository.entity.Animal;
import com.eurail.zoo_app.respository.entity.Room;
import com.eurail.zoo_app.exception.BadRequestException;
import com.eurail.zoo_app.exception.ResourceNotFoundException;
import com.eurail.zoo_app.respository.AnimalRepository;
import com.eurail.zoo_app.service.impl.AnimalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnimalServiceImplTest {

    private AnimalRepository repository;
    private RoomService roomService;
    private AnimalServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(AnimalRepository.class);
        roomService = mock(RoomService.class);
        service = new AnimalServiceImpl(repository, roomService);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAnimal() {
        Animal animal = new Animal();
        animal.setTitle("Lion");
        animal.setCurrentRoomId("r1");
        animal.setFavouriteRoomIds(new LinkedHashSet<>());

        Animal savedAnimal = new Animal();
        savedAnimal.setId("a1");
        savedAnimal.setTitle("Lion");
        savedAnimal.setCurrentRoomId("r1");
        savedAnimal.setFavouriteRoomIds(new LinkedHashSet<>());
        savedAnimal.setCreated(Instant.now());
        savedAnimal.setUpdated(Instant.now());

        // Mock roomService.exists() to return true for room "r1"
        when(roomService.exists("r1")).thenReturn(true);
        when(repository.save(any())).thenReturn(savedAnimal);

        Animal result = service.create(animal);

        assertNotNull(result.getId());
        assertEquals("Lion", result.getTitle());
        verify(repository, times(1)).save(any());
        verify(roomService, times(1)).exists("r1");
    }

    @Test
    void testGetAnimalFound() {
        Animal animal = new Animal();
        animal.setId("a1");
        animal.setTitle("Lion");

        when(repository.findById("a1")).thenReturn(Optional.of(animal));

        Animal result = service.get("a1");
        assertEquals("a1", result.getId());
        assertEquals("Lion", result.getTitle());
    }

    @Test
    void testGetAnimalNotFound() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.get("unknown"));
    }

    @Test
    void testUpdateAnimal() {
        Animal existing = new Animal();
        existing.setId("a1");
        existing.setTitle("Lion");
        existing.setCurrentRoomId("r1");
        existing.setFavouriteRoomIds(new LinkedHashSet<>());
        existing.setCreated(Instant.now());
        existing.setUpdated(Instant.now());

        Animal updated = new Animal();
        updated.setTitle("Tiger");
        updated.setCurrentRoomId("r2");

        when(repository.findById("a1")).thenReturn(Optional.of(existing));
        // Mock roomService.exists() for the new room "r2"
        when(roomService.exists("r2")).thenReturn(true);
        when(repository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Animal result = service.update("a1", updated);

        assertEquals("Tiger", result.getTitle());
        assertEquals("r2", result.getCurrentRoomId());

        verify(repository, times(1)).save(any(Animal.class));
        verify(roomService, times(1)).exists("r2");
    }


    @Test
    void testDeleteAnimal() {
        Animal existing = new Animal();
        existing.setId("a1");
        when(repository.findById("a1")).thenReturn(Optional.of(existing));

        service.delete("a1");
        verify(repository, times(1)).deleteById("a1");
    }

    @Test
    void testListAnimalsInRoom() {
        String roomId = "r1";
        int page = 0;
        int size = 2;
        String sortBy = "title";
        String order = "asc";

        Animal a1 = new Animal();
        a1.setId("a1");
        a1.setTitle("Lion");

        Animal a2 = new Animal();
        a2.setId("a2");
        a2.setTitle("Tiger");

        List<Animal> animals = Arrays.asList(a1, a2);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
        Page<Animal> mockPage = new PageImpl<>(animals, pageable, animals.size());

        when(roomService.get(roomId)).thenReturn(new Room());
        when(repository.findByCurrentRoomId(roomId, pageable)).thenReturn(mockPage);

        Page<Animal> result = service.listAnimalsInRoom(roomId, sortBy, order, page, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Lion", result.getContent().get(0).getTitle());
        assertEquals("Tiger", result.getContent().get(1).getTitle());

        verify(roomService, times(1)).get(roomId);
        verify(repository, times(1)).findByCurrentRoomId(roomId, pageable);
    }

    @Test
    void testAssignAnimalToRoom() {
        String animalId = "a1";
        String roomId = "r2";

        Animal existing = new Animal();
        existing.setId(animalId);
        existing.setTitle("Lion");
        existing.setCurrentRoomId("r1");
        existing.setCreated(Instant.now());
        existing.setUpdated(Instant.now());

        Room room = new Room();
        room.setId(roomId);
        room.setTitle("New Room");

        when(repository.findById(animalId)).thenReturn(Optional.of(existing));

        when(roomService.get(roomId)).thenReturn(room);

        when(repository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal result = service.assignAnimalToRoom(animalId, roomId);

        assertNotNull(result);
        assertEquals(roomId, result.getCurrentRoomId(), "Animal must be assigned to new room");

        assertTrue(result.getUpdated().isAfter(existing.getCreated()));

        verify(repository, times(1)).findById(animalId);
        verify(roomService, times(1)).get(roomId);
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testRemoveAnimalFromRoom() {
        String id = "a1";

        Animal existing = new Animal();
        existing.setId(id);
        existing.setTitle("Tiger");
        existing.setCurrentRoomId("r5");
        existing.setCreated(Instant.now());
        existing.setUpdated(Instant.now().minusSeconds(10));

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        when(repository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal result = service.removeAnimalFromRoom(id);

        assertNull(result.getCurrentRoomId(), "Room must be cleared");

        assertTrue(result.getUpdated().isAfter(existing.getCreated()));

        assertEquals("Tiger", result.getTitle());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testAssignFavouriteRoom() {
        String animalId = "a1";
        String roomId = "r10";

        Animal existing = new Animal();
        existing.setId(animalId);
        existing.setTitle("Elephant");
        existing.setFavouriteRoomIds(new LinkedHashSet<>());
        existing.setCreated(Instant.now());
        existing.setUpdated(Instant.now().minusSeconds(20));

        existing.setFavouriteRoomIds(new LinkedHashSet<>());

        when(repository.findById(animalId)).thenReturn(Optional.of(existing));

        // mock roomService.get
        Room room = new Room();
        room.setId(roomId);
        room.setTitle("Big Safari Room");
        when(roomService.get(roomId)).thenReturn(room);

        when(repository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal result = service.assignFavouriteRoom(animalId, roomId);

        assertTrue(result.getFavouriteRoomIds().contains(roomId),
                "Favourite room was not added!");

        verify(repository).findById(animalId);
        verify(roomService).get(roomId);
        verify(repository).save(existing);
    }

    @Test
    void testUnassignFavouriteRoom() {
        String animalId = "a1";
        String roomId = "r10";

        Animal existing = new Animal();
        existing.setId(animalId);
        existing.setTitle("Lion");

        existing.setUpdated(Instant.now().minusSeconds(10));

        existing.setFavouriteRoomIds(new LinkedHashSet<>(Set.of(roomId, "r20")));

        Room room = new Room();
        room.setId(roomId);
        room.setTitle("Desert Room");

        when(repository.findById(animalId)).thenReturn(Optional.of(existing));
        when(roomService.get(roomId)).thenReturn(room);
        when(repository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        Animal result = service.unassignFavouriteRoom(animalId, roomId);

        assertFalse(result.getFavouriteRoomIds().contains(roomId),
                "Favourite room should be removed!");

        assertTrue(result.getFavouriteRoomIds().contains("r20"));

        verify(repository).findById(animalId);
        verify(roomService).get(roomId);
        verify(repository).save(existing);
    }

    @Test
    void testUnassignFavouriteRoom_NotInList_ShouldThrow() {
        String animalId = "a1";
        String roomId = "r10";

        Animal existing = new Animal();
        existing.setId(animalId);
        existing.setFavouriteRoomIds(new LinkedHashSet<>(Set.of("x1", "x2")));

        when(repository.findById(animalId)).thenReturn(Optional.of(existing));

        Room room = new Room();
        room.setId(roomId);
        room.setTitle("Room Title");
        when(roomService.get(roomId)).thenReturn(room);

        assertThrows(BadRequestException.class,
                () -> service.unassignFavouriteRoom(animalId, roomId));

        verify(repository).findById(animalId);
        verify(roomService).get(roomId);
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateAnimal_WithNonExistentRoom_ShouldThrow() {
        Animal animal = new Animal();
        animal.setTitle("Lion");
        animal.setCurrentRoomId("non-existent-room");
        animal.setFavouriteRoomIds(new LinkedHashSet<>());

        // Mock roomService.exists() to return false
        when(roomService.exists("non-existent-room")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(animal));

        verify(roomService, times(1)).exists("non-existent-room");
        verify(repository, never()).save(any());
    }


}
