package com.paymedia.administrations.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymedia.administrations.entity.DualAuthData;
import com.paymedia.administrations.entity.Permission;
import com.paymedia.administrations.entity.Role;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.model.request.RoleRequest;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.repository.DualAuthDataRepository;
import com.paymedia.administrations.repository.PermissionRepository;
import com.paymedia.administrations.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private DualAuthDataRepository dualAuthDataRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticationService authenticationService;

    public DualAuthData createRole(RoleRequest roleRequest) {
        try {
            Integer adminId = authenticationService.getLoggedInUserId();
            if (adminId == null) {
                throw new RuntimeException("Logged-in user ID is null");
            }

            // Fetch permissions by their IDs
            Set<Permission> permissions = roleRequest.getPermissions().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId)))
                    .collect(Collectors.toSet());

            // Create a new Role entity
            Role role = new Role();
            role.setRolename(roleRequest.getRolename());
            role.setPermissions(permissions);



            // Convert Role entity to JSON and save it to dual_auth_data
            String newData = objectMapper.writeValueAsString(role);
            DualAuthData dualAuthData = DualAuthData.builder()
                    .entity("Role")
                    .newData(newData)
                    .createdBy(adminId)
                    .status("Pending")
                    .action("Create")
                    .build();

            DualAuthData savedDualAuthData = dualAuthDataRepository.save(dualAuthData);
            return savedDualAuthData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create role: " + e.getMessage());
        }
    }


    public void approveRole(Integer dualAuthDataId) {
        try {
            // Retrieve the DualAuthData record
            DualAuthData dualAuthData = dualAuthDataRepository.findById(dualAuthDataId)
                    .orElseThrow(() -> new RuntimeException("DualAuthData not found: " + dualAuthDataId));

            if (!"Pending".equals(dualAuthData.getStatus())) {
                throw new RuntimeException("Role approval request is not in pending status");
            }

            // Deserialize the newData JSON to RoleRequest
            RoleRequest roleRequest = objectMapper.readValue(dualAuthData.getNewData(), RoleRequest.class);

            // Check if the role already exists
            Role role;
            if (roleRequest.getRoleId() == null) {
                // Create a new Role if roleId is null
                role = new Role();
                role.setRolename(roleRequest.getRolename());
            } else {
                // Update the existing Role
                role = roleRepository.findById(roleRequest.getRoleId())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleRequest.getRoleId()));
                role.setRolename(roleRequest.getRolename());
            }

            // Save or update the Role entity
            role = roleRepository.save(role);

            // Update the Role's permissions
            Set<Permission> permissions = roleRequest.getPermissions().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId)))
                    .collect(Collectors.toSet());

            // Clear existing permissions and add new ones
            role.getPermissions().clear();
            role.getPermissions().addAll(permissions);

            // Save the updated role
            roleRepository.save(role);

            // Update the DualAuthData record status to approved
            dualAuthData.setStatus("Approved");
            dualAuthData.setReviewedBy(authenticationService.getLoggedInUserId()); // Set the ID of the user who approved
            dualAuthDataRepository.save(dualAuthData);

        } catch (Exception e) {
            throw new RuntimeException("Failed to approve role: " + e.getMessage());
        }
    }

}
