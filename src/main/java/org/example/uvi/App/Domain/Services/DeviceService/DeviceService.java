package org.example.uvi.App.Domain.Services.DeviceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.OsType.OsType;
import org.example.uvi.App.Domain.Models.Device.Device;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.DeviceRepository.DeviceRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserService userService;

    @Cacheable(value = "devices", key = "#userId")
    public List<Device> getDevicesByUser(Long userId) {
        return deviceRepository.findAllByUserId(userId);
    }

    @Transactional
    @CacheEvict(value = "devices", key = "#userId")
    public Device registerDevice(Long userId, String deviceToken, OsType osType) {
        User user = userService.getUserById(userId);

        // Если устройство уже зарегистрировано — обновляем
        return deviceRepository.findByDeviceToken(deviceToken)
                .map(existing -> {
                    existing.setOsType(osType);
                    return deviceRepository.save(existing);
                })
                .orElseGet(() -> {
                    Device device = Device.builder()
                            .user(user)
                            .deviceToken(deviceToken)
                            .osType(osType)
                            .build();
                    log.info("Registered new device for user {}", userId);
                    return deviceRepository.save(device);
                });
    }

    @Transactional
    @CacheEvict(value = "devices", key = "#userId")
    public void deleteDevice(Long userId, UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        if (!device.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Device does not belong to user");
        }

        deviceRepository.delete(device);
        log.info("Device {} deleted for user {}", deviceId, userId);
    }

    @Transactional
    @CacheEvict(value = "devices", key = "#userId")
    public void deleteAllUserDevices(Long userId) {
        deviceRepository.deleteAllByUserId(userId);
        log.info("All devices deleted for user {}", userId);
    }
}
