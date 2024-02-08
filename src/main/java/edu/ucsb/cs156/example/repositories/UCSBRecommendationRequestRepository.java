package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.UCSBRecommendationRequest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UCSBRecommendationRequestRepository extends CrudRepository<UCSBRecommendationRequest, Long> {
  
}