package com.eurail.zoo_app.respository.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("rooms")
public class Room {

    @Id
    private String id;

    @NotBlank
    private String title;

    @NotNull
    private Instant created;

    @NotNull
    private Instant updated;

    public Room() {
    }

    public Room(String id, String title, Instant created, Instant updated) {
        this.id = id;
        this.title = title;
        this.created = created;
        this.updated = updated;
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
}
