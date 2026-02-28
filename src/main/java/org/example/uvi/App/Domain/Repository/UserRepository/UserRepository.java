package org.example.uvi.App.Domain.Repository.UserRepository;

import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    List<User> findByStatus(UserStatus status);

    List<User> findByCityAndStatus(String city, UserStatus status);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.deletedAt IS NULL")
    Optional<User> findByPhoneNumberActive(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL " +
            "AND u.status = 'ACTIVE' AND u.deletedAt IS NULL")
    List<User> findAllWithLocation();

    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND u.deletedAt IS NULL")
    List<User> findByNameContaining(@Param("name") String name);
}
