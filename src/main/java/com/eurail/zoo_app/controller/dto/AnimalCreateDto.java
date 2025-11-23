package com.eurail.zoo_app.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public class AnimalCreateDto {
    @NotBlank
    private String title;

    @NotNull
    private LocalDate located;

    private String currentRoomId;

    private Set<String> favouriteRoomIds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
