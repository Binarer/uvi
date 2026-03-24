package org.example.uvi.App.Domain.Repository.UserVisitRepository;

import org.example.uvi.App.Domain.Models.UserVisit.UserVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVisitRepository extends JpaRepository<UserVisit, Long> {
    List<UserVisit> findAllByUserId(Long userId);
    List<UserVisit> findAllByUserIdAndPlaceId(Long userId, Long placeId);
}
