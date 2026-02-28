package org.example.uvi.App.Infrastructure.Http.Mapper.DeviceMapper;

import org.example.uvi.App.Domain.Models.Device.Device;
import org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public DeviceDto toDto(Device device) {
        if (device == null) return null;
        return new DeviceDto(
                device.getId(),
                device.getUser().getId(),
                device.getDeviceToken(),
                device.getOsType(),
                device.getLastActiveAt()
        );
    }
}
