package com.eurail.zoo_app.respository;

import com.eurail.zoo_app.respository.entity.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnimalRepository extends MongoRepository<Animal, String> {
    Page<Animal> findByCurrentRoomId(String roomId, Pageable pageable);
}
