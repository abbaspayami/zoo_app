package com.eurail.zoo_app.service;

import com.eurail.zoo_app.respository.entity.Room;


public interface RoomService {

    Room create(Room room);

    Room get(String id);

    Room update(String id, Room changes);

    void delete(String id);

    boolean exists(String id);

}
