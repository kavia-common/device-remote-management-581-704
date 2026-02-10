package com.example.BackendServicesContainer.repository;

import com.example.BackendServicesContainer.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    // Find all devices for a specific user (multi-tenant support)
    Page<Device> findByUserId(String userId, Pageable pageable);
    
    // Find device by ID and user ID
    Optional<Device> findByIdAndUserId(Long id, String userId);
    
    // Search devices by name or IP address
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.ipAddress) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.protocol) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Device> searchDevices(@Param("userId") String userId, 
                              @Param("searchTerm") String searchTerm, 
                              Pageable pageable);
    
    // Find devices by protocol
    List<Device> findByProtocolAndUserId(String protocol, String userId);
    
    // Find devices by status
    List<Device> findByStatusAndUserId(String status, String userId);
    
    // Count devices by protocol for a user
    @Query("SELECT d.protocol, COUNT(d) FROM Device d WHERE d.userId = :userId GROUP BY d.protocol")
    List<Object[]> countDevicesByProtocol(@Param("userId") String userId);
}
