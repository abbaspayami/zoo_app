package com.eurail.zoo_app.respository;

import com.eurail.zoo_app.respository.entity.Room;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface RoomRepository extends MongoRepository<Room, String> {
}
