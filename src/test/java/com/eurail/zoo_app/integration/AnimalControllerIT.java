package com.eurail.zoo_app.integration;

import com.eurail.zoo_app.controller.dto.AnimalCreateDto;
import com.eurail.zoo_app.controller.dto.AnimalUpdateDto;
import com.eurail.zoo_app.controller.dto.FavouriteRequestDto;
import com.eurail.zoo_app.controller.dto.PlaceRequestDto;
import com.eurail.zoo_app.respository.entity.Animal;
import com.eurail.zoo_app.respository.entity.Room;
import com.eurail.zoo_app.respository.AnimalRepository;
import com.eurail.zoo_app.respository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AnimalControllerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnimalRepository repository;

    @Autowired
    private RoomRepository roomRepository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void createAnimal_shouldReturnCreatedAnimal() throws Exception {
        AnimalCreateDto dto = new AnimalCreateDto();
        dto.setTitle("Lion");
        dto.setLocated(LocalDate.of(2025, 11, 23));
        dto.setCurrentRoomId("r1");
        dto.setFavouriteRoomIds(new HashSet<>());

        mockMvc.perform(post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Lion"))
                .andExpect(jsonPath("$.currentRoomId").value("r1"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        Animal saved = repository.findAll().get(0);
        assertThat(saved.getTitle()).isEqualTo("Lion");
        assertThat(saved.getCurrentRoomId()).isEqualTo("r1");
        assertThat(saved.getId()).isNotNull();
    }


    @Test
    void getAnimal_shouldReturnAnimal() throws Exception {
        Animal animal = new Animal();
        animal.setTitle("Tiger");
        animal.setCurrentRoomId("r2");
        animal = repository.save(animal);

        mockMvc.perform(get("/animals/{id}", animal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tiger"))
                .andExpect(jsonPath("$.currentRoomId").value("r2"))
                .andExpect(jsonPath("$.id").value(animal.getId()));
    }

    @Test
    void updateAnimal_shouldReturnUpdatedAnimal() throws Exception {
        Animal animal = new Animal();
        animal.setTitle("Elephant");
        animal.setCurrentRoomId("r3");
        animal = repository.save(animal);

        AnimalUpdateDto dto = new AnimalUpdateDto();
        dto.setTitle("Elephant Updated");
        dto.setCurrentRoomId("r4");

        mockMvc.perform(put("/animals/{id}", animal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elephant Updated"))
                .andExpect(jsonPath("$.currentRoomId").value("r4"));

        Animal updated = repository.findById(animal.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Elephant Updated");
        assertThat(updated.getCurrentRoomId()).isEqualTo("r4");
    }

    @Test
    void deleteAnimal_shouldRemoveAnimal() throws Exception {
        Animal animal = new Animal();
        animal.setTitle("Monkey");
        animal.setCurrentRoomId("r5");
        animal = repository.save(animal);

        mockMvc.perform(delete("/animals/{id}", animal.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.existsById(animal.getId())).isFalse();
    }

    @Test
    void placeAnimal_shouldAssignRoom() throws Exception {

        Animal animal = new Animal();
        animal.setTitle("Giraffe");
        animal.setLocated(LocalDate.now());
        animal = repository.save(animal);

        Room room = new Room();
        room.setTitle("African Room");
        room = roomRepository.save(room);

        PlaceRequestDto dto = new PlaceRequestDto();
        dto.setRoomId(room.getId()); // مهم !!!

        mockMvc.perform(post("/animals/{animalId}/place", animal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoomId").value(room.getId()));

        Animal updated = repository.findById(animal.getId()).orElseThrow();
        assertThat(updated.getCurrentRoomId()).isEqualTo(room.getId());
    }


    @Test
    void moveAnimal_shouldChangeRoom() throws Exception {

        // Create rooms
        Room room1 = new Room();
        room1.setTitle("Room 1");
        room1 = roomRepository.save(room1);

        Room room2 = new Room();
        room2.setTitle("Room 2");
        room2 = roomRepository.save(room2);

        // Create animal
        Animal animal = new Animal();
        animal.setTitle("Zebra");
        animal.setLocated(LocalDate.now());
        animal.setCurrentRoomId(room1.getId());
        animal = repository.save(animal);

        // DTO
        PlaceRequestDto dto = new PlaceRequestDto();
        dto.setRoomId(room2.getId());

        // Call API
        mockMvc.perform(put("/animals/{animalId}/move", animal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoomId").value(room2.getId()));

        // Validate DB
        Animal updated = repository.findById(animal.getId()).orElseThrow();
        assertThat(updated.getCurrentRoomId()).isEqualTo(room2.getId());
    }


    @Test
    void removeFromRoom_shouldClearRoom() throws Exception {
        Animal animal = new Animal();
        animal.setTitle("Bear");
        animal.setLocated(LocalDate.now());
        animal.setCurrentRoomId("r3");
        animal = repository.save(animal);

        mockMvc.perform(delete("/animals/{id}/room", animal.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoomId").isEmpty());

        Animal updated = repository.findById(animal.getId()).orElseThrow();
        assertThat(updated.getCurrentRoomId()).isNull();
    }

    @Test
    void assignFavouriteRoom_shouldAddRoomToFavourites() throws Exception {
        Room room = new Room();
        room.setTitle("Favourite Room");
        room = roomRepository.save(room);

        Animal animal = new Animal();
        animal.setTitle("Elephant");
        animal.setLocated(LocalDate.now());
        animal.setFavouriteRoomIds(new HashSet<>());
        animal = repository.save(animal);

        FavouriteRequestDto dto = new FavouriteRequestDto();
        dto.setRoomId(room.getId());

        // Call controller
        mockMvc.perform(post("/animals/{id}/favourites", animal.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favouriteRoomIds[0]").value(room.getId()));

        Animal updated = repository.findById(animal.getId()).orElseThrow();
        assertThat(updated.getFavouriteRoomIds()).contains(room.getId());
    }


    @Test
    void unassignFavouriteRoom_shouldRemoveRoomFromFavourites() throws Exception {
        Room room = new Room();
        room.setTitle("Fav Room");
        room = roomRepository.save(room);

        Animal animal = new Animal();
        animal.setTitle("Lion");
        animal.setLocated(LocalDate.now());
        animal.setFavouriteRoomIds(new HashSet<>());
        animal.getFavouriteRoomIds().add(room.getId());
        animal = repository.save(animal);

        mockMvc.perform(delete("/animals/{id}/favourites/{roomId}", animal.getId(), room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favouriteRoomIds").isEmpty());

        Animal updated = repository.findById(animal.getId()).orElseThrow();
        assertThat(updated.getFavouriteRoomIds()).doesNotContain(room.getId());
    }

}
