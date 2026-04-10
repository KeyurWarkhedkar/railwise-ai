package com.keyur.railwiseai.repositories;

import com.keyur.railwiseai.entities.Train;
import com.keyur.railwiseai.enums.TrainType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainRepository extends JpaRepository<Train, String> {
    List<Train> findByType(TrainType type);
}
