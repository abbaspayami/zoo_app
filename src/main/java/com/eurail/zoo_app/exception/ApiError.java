package com.eurail.zoo_app.exception;

import java.time.Instant;

public class ApiError {
    private String id;
    private int status;
    private String error;
    private String message;
    private Instant timestamp;



    public ApiError(String id, int status, String error, String message, Instant timestamp) {
        this.id = id;
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
