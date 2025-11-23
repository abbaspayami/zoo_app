package com.eurail.zoo_app.controller.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public class AnimalResponseDto {

    private String id;
    private String title;
    private Instant created;
    private Instant updated;
    private LocalDate located;
    private String currentRoomId;
    private Set<String> favouriteRoomIds;

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
