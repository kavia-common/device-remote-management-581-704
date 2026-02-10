package com.example.BackendServicesContainer.service;

import com.example.BackendServicesContainer.entity.Device;
import com.example.BackendServicesContainer.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceService {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    // PUBLIC_INTERFACE
    /**
     * Get all devices for a user with pagination
     */
    public Page<Device> getAllDevices(String userId, Pageable pageable) {
        return deviceRepository.findByUserId(userId, pageable);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Get device by ID for a specific user
     */
    public Optional<Device> getDeviceById(Long id, String userId) {
        return deviceRepository.findByIdAndUserId(id, userId);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Create a new device
     */
    public Device createDevice(Device device, String userId) {
        device.setUserId(userId);
        device.setStatus("Online"); // Default status
        return deviceRepository.save(device);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Update an existing device
     */
    public Device updateDevice(Long id, Device updatedDevice, String userId) {
        Optional<Device> existingDevice = deviceRepository.findByIdAndUserId(id, userId);
        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            device.setName(updatedDevice.getName());
            device.setIpAddress(updatedDevice.getIpAddress());
            device.setProtocol(updatedDevice.getProtocol());
            device.setDescription(updatedDevice.getDescription());
            if (updatedDevice.getStatus() != null) {
                device.setStatus(updatedDevice.getStatus());
            }
            return deviceRepository.save(device);
        }
        throw new RuntimeException("Device not found with id: " + id);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Delete a device
     */
    public void deleteDevice(Long id, String userId) {
        Optional<Device> device = deviceRepository.findByIdAndUserId(id, userId);
        if (device.isPresent()) {
            deviceRepository.delete(device.get());
        } else {
            throw new RuntimeException("Device not found with id: " + id);
        }
    }
    
    // PUBLIC_INTERFACE
    /**
     * Search devices by term
     */
    public Page<Device> searchDevices(String userId, String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllDevices(userId, pageable);
        }
        return deviceRepository.searchDevices(userId, searchTerm.trim(), pageable);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Get devices by protocol
     */
    public List<Device> getDevicesByProtocol(String protocol, String userId) {
        return deviceRepository.findByProtocolAndUserId(protocol, userId);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Get device statistics for a user
     */
    public List<Object[]> getDeviceStatistics(String userId) {
        return deviceRepository.countDevicesByProtocol(userId);
    }
    
    // PUBLIC_INTERFACE
    /**
     * Update device status
     */
    public Device updateDeviceStatus(Long id, String status, String userId) {
        Optional<Device> existingDevice = deviceRepository.findByIdAndUserId(id, userId);
        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            device.setStatus(status);
            return deviceRepository.save(device);
        }
        throw new RuntimeException("Device not found with id: " + id);
    }
}
