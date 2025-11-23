package com.eurail.zoo_app.service;

import com.eurail.zoo_app.controller.dto.FavouriteRoomStatsDto;
import com.eurail.zoo_app.respository.entity.Animal;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AnimalService {
    Animal create(Animal animal);
    Animal get(String id);
    Animal update(String id, Animal changes);
    void delete(String id);
    Animal assignAnimalToRoom(String animalId, String roomId);
    Animal removeAnimalFromRoom(String animalId);
    Animal assignFavouriteRoom(String animalId, String roomId);
    Animal unassignFavouriteRoom(String animalId, String roomId);
    Page<Animal> listAnimalsInRoom(String roomId, String sortBy, String order, int page, int size);
    List<FavouriteRoomStatsDto> favouriteRoomStats();
}
