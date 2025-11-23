package com.eurail.zoo_app.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class RoomCreateDto {
    @NotBlank
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
