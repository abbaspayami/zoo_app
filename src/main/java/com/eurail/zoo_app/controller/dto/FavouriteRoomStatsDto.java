package com.eurail.zoo_app.controller.dto;

public class FavouriteRoomStatsDto {
    private String title;
    private long count;

    public FavouriteRoomStatsDto(String title, long count) {
        this.title = title;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
