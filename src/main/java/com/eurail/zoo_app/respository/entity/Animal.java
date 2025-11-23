package com.eurail.zoo_app.respository.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Document("animals")
public class Animal {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotNull
    private Instant created;

    @NotNull
    private Instant updated;

    @NotNull
    private LocalDate located;

    @Indexed
    private String currentRoomId;

    private Set<String> favouriteRoomIds = new HashSet<>();

    public Animal() {
    }

    public Animal(String id, String title, Instant created, Instant updated, LocalDate located, String currentRoomId, Set<String> favouriteRoomIds) {
        this.id = id;
        this.title = title;
        this.created = created;
        this.updated = updated;
        this.located = located;
        this.currentRoomId = currentRoomId;
        this.favouriteRoomIds = favouriteRoomIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public LocalDate getLocated() {
        return located;
    }

    public void setLocated(LocalDate located) {
        this.located = located;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public Set<String> getFavouriteRoomIds() {
        return favouriteRoomIds;
    }

    public void setFavouriteRoomIds(Set<String> favouriteRoomIds) {
        this.favouriteRoomIds = favouriteRoomIds;
    }
}
