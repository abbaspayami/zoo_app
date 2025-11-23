package com.eurail.zoo_app.controller.dto;

import jakarta.validation.constraints.NotBlank;


public class PlaceRequestDto {
    @NotBlank
    private String roomId;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
