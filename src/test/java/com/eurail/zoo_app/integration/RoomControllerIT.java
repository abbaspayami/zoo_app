package com.eurail.zoo_app.integration;

import com.eurail.zoo_app.controller.dto.RoomCreateDto;
import com.eurail.zoo_app.controller.dto.RoomUpdateDto;
import com.eurail.zoo_app.respository.entity.Room;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RoomControllerIT {

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
    private RoomRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void createRoom_shouldReturnCreatedRoom() throws Exception {
        RoomCreateDto dto = new RoomCreateDto();
        dto.setTitle("Safari Room");

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Safari Room"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        Room saved = repository.findAll().get(0);
        assertThat(saved.getTitle()).isEqualTo("Safari Room");
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void getRoom_shouldReturnExistingRoom() throws Exception {
        Room room = new Room();
        room.setTitle("Jungle Room");
        room = repository.save(room);

        mockMvc.perform(get("/rooms/{id}", room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Jungle Room"))
                .andExpect(jsonPath("$.id").value(room.getId()));
    }

    @Test
    void updateRoom_shouldReturnUpdatedRoom() throws Exception {
        Room room = new Room();
        room.setTitle("Old Room");
        room = repository.save(room);

        RoomUpdateDto dto = new RoomUpdateDto();
        dto.setTitle("Updated Room");

        mockMvc.perform(put("/rooms/{id}", room.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Room"));

        Room updated = repository.findById(room.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Room");
    }

    @Test
    void deleteRoom_shouldRemoveRoom() throws Exception {
        Room room = new Room();
        room.setTitle("To Delete Room");
        room = repository.save(room);

        mockMvc.perform(delete("/rooms/{id}", room.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.existsById(room.getId())).isFalse();
    }

    @Test
    void favouriteStats_shouldReturnEmptyListIfNoAnimals() throws Exception {
        mockMvc.perform(get("/rooms/favourites/stats"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
