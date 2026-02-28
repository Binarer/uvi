package org.example.uvi.App.Domain.Repository.SmsVerificationRepository;

import org.example.uvi.App.Domain.Models.SmsVerification.SmsVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SmsVerificationRepository extends JpaRepository<SmsVerification, Long> {

    @Query("SELECT s FROM SmsVerification s WHERE s.phoneNumber = :phoneNumber AND s.verified = false " +
            "AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    Optional<SmsVerification> findLatestUnverifiedByPhoneNumber(String phoneNumber, LocalDateTime now);

    Optional<SmsVerification> findByPhoneNumberAndCodeAndVerifiedFalse(String phoneNumber, String code);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
