package com.example.BackendServicesContainer.controller;

import com.example.BackendServicesContainer.entity.Device;
import com.example.BackendServicesContainer.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/devices")
@CrossOrigin(origins = {"https://vscode-internal-26084-beta.beta01.cloud.kavia.ai:3000", "http://localhost:3000"})
@Tag(name = "Device Management", description = "API endpoints for managing network devices")
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;
    
    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieve all devices with pagination and search support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved devices"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // PUBLIC_INTERFACE
    public ResponseEntity<Map<String, Object>> getAllDevices(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,
            
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            
            @Parameter(description = "Search term for filtering devices")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "User ID for multi-tenant support", required = true)
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
            
            Page<Device> devicePage;
            if (search != null && !search.trim().isEmpty()) {
                devicePage = deviceService.searchDevices(userId, search, pageable);
            } else {
                devicePage = deviceService.getAllDevices(userId, pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("devices", devicePage.getContent());
            response.put("currentPage", devicePage.getNumber());
            response.put("totalItems", devicePage.getTotalElements());
            response.put("totalPages", devicePage.getTotalPages());
            response.put("pageSize", devicePage.getSize());
            response.put("hasNext", devicePage.hasNext());
            response.put("hasPrevious", devicePage.hasPrevious());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", response);
            result.put("success", true);
            result.put("message", "Devices retrieved successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving devices: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieve a specific device by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device"),
        @ApiResponse(responseCode = "404", description = "Device not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // PUBLIC_INTERFACE
    public ResponseEntity<Map<String, Object>> getDeviceById(
            @Parameter(description = "Device ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "User ID for multi-tenant support", required = true)
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId) {
        
        try {
            Optional<Device> device = deviceService.getDeviceById(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            if (device.isPresent()) {
                response.put("data", device.get());
                response.put("success", true);
                response.put("message", "Device retrieved successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Device not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving device: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create new device", description = "Register a new device in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Device created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid device data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // PUBLIC_INTERFACE
    public ResponseEntity<Map<String, Object>> createDevice(
            @Parameter(description = "Device data", required = true)
            @RequestBody Device device,
            
            @Parameter(description = "User ID for multi-tenant support", required = true)
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId) {
        
        try {
            Device createdDevice = deviceService.createDevice(device, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", createdDevice);
            response.put("success", true);
            response.put("message", "Device created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating device: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update device", description = "Update an existing device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device updated successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found"),
        @ApiResponse(responseCode = "400", description = "Invalid device data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // PUBLIC_INTERFACE
    public ResponseEntity<Map<String, Object>> updateDevice(
            @Parameter(description = "Device ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "Updated device data", required = true)
            @RequestBody Device device,
            
            @Parameter(description = "User ID for multi-tenant support", required = true)
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId) {
        
        try {
            Device updatedDevice = deviceService.updateDevice(id, device, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", updatedDevice);
            response.put("success", true);
            response.put("message", "Device updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating device: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device", description = "Remove a device from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // PUBLIC_INTERFACE
    public ResponseEntity<Map<String, Object>> deleteDevice(
            @Parameter(description = "Device ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "User ID for multi-tenant support", required = true)
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId) {
        
        try {
            deviceService.deleteDevice(id, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Device deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error deleting device: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get device statistics", description = "Get device count by protocol for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // PUBLIC_INTERFACE
    public ResponseEntity<Map<String, Object>> getDeviceStatistics(
            @Parameter(description = "User ID for multi-tenant support", required = true)
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String userId) {
        
        try {
            List<Object[]> stats = deviceService.getDeviceStatistics(userId);
            
            Map<String, Long> protocolCounts = new HashMap<>();
            for (Object[] stat : stats) {
                String protocol = (String) stat[0];
                Long count = (Long) stat[1];
                protocolCounts.put(protocol, count);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", protocolCounts);
            response.put("success", true);
            response.put("message", "Statistics retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
