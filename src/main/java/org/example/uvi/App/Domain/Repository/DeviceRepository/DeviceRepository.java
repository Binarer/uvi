package org.example.uvi.App.Domain.Repository.DeviceRepository;

import org.example.uvi.App.Domain.Models.Device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findAllByUserId(Long userId);

    Optional<Device> findByDeviceToken(String deviceToken);

    void deleteAllByUserId(Long userId);
}
