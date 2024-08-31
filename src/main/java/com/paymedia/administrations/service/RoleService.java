package com.paymedia.administrations.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymedia.administrations.annotations.CheckEntityLock;
import com.paymedia.administrations.annotations.CheckRoleLock;
import com.paymedia.administrations.annotations.CheckUserStatus;
import com.paymedia.administrations.entity.DualAuthData;
import com.paymedia.administrations.entity.Permission;
import com.paymedia.administrations.entity.Role;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.model.request.RoleRequest;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.repository.DualAuthDataRepository;
import com.paymedia.administrations.repository.PermissionRepository;
import com.paymedia.administrations.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
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

            String newData = objectMapper.writeValueAsString(roleRequest);
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
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                RoleRequest roleRequest = objectMapper.readValue(dualAuthData.getNewData(), RoleRequest.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                // Fetch permissions by their IDs from the roleRequest
                Set<Permission> permissions = roleRequest.getPermissions().stream()
                        .map(permissionId -> permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId)))
                        .collect(Collectors.toSet());

                // Create and save the Role entity
                Role role = Role.builder()
                        .rolename(roleRequest.getRolename())
                        .permissions(permissions)
                        .build();

                roleRepository.save(role);

                // Update the DualAuthData status to approved
                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);

            } catch (Exception e) {
                log.error("Error approving role", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }
    }

    public void rejectRole(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);


        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();


            try {
                Integer adminId = authenticationService.getLoggedInUserId();

                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Rejected");
                dualAuthDataRepository.save(dualAuthData);
            } catch (Exception e) {
                log.error("Error approving role", e);
            }
        }
    }

    @CheckEntityLock(entityType = Role.class)
    public DualAuthData updateRole(Integer roleId, RoleRequest roleRequest) {
        try {
            Integer adminId = authenticationService.getLoggedInUserId();
            if (adminId == null) {
                throw new RuntimeException("Logged-in user ID is null");
            }

            // Fetch the existing role
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (roleOptional.isEmpty()) {
                throw new RuntimeException("Role not found for id: " + roleId);
            }

            Role existingRole = roleOptional.get();
            existingRole.setIsLocked(true);
            roleRepository.save(existingRole);

            // Convert the existing role to JSON (old data)
            String oldData = objectMapper.writeValueAsString(existingRole);

            // Convert the updated RoleRequest to JSON (new data)
            String newData = objectMapper.writeValueAsString(roleRequest);

            // Save the old and new data to dual_auth_data
            DualAuthData dualAuthData = DualAuthData.builder()
                    .entity("Role")
                    .oldData(oldData)
                    .newData(newData)
                    .createdBy(adminId)
                    .status("Pending")
                    .action("Update")
                    .build();

            DualAuthData savedDualAuthData = dualAuthDataRepository.save(dualAuthData);
            return savedDualAuthData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update role: " + e.getMessage());
        }
    }

    public void approveRoleUpdate(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                RoleRequest roleRequest = objectMapper.readValue(dualAuthData.getNewData(), RoleRequest.class);
                Optional<Role> roleOptional = roleRepository.findById(roleRequest.getRoleId());

                if (roleOptional.isEmpty()) {
                    throw new RuntimeException("Role not found for id: " + roleRequest.getRoleId());
                }

                Role role = roleOptional.get();

                // Fetch the permissions by their IDs from the roleRequest
                Set<Permission> permissions = roleRequest.getPermissions().stream()
                        .map(permissionId -> permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId)))
                        .collect(Collectors.toSet());

                // Update the role's properties
                role.setRolename(roleRequest.getRolename());
                role.setPermissions(permissions);
                role.setIsLocked(false);
                roleRepository.save(role);

                // Save the updated role to the role table
                roleRepository.save(role);

                // Update the DualAuthData status to approved
                Integer adminId = authenticationService.getLoggedInUserId();
                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);

            } catch (Exception e) {
                log.error("Error approving role update", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }
    }


    @CheckEntityLock(entityType = Role.class)
    public DualAuthData deleteRole(Integer roleId) {
        try {
            Integer adminId = authenticationService.getLoggedInUserId();
            if (adminId == null) {
                throw new RuntimeException("Logged-in user ID is null");
            }

            // Fetch the existing role
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (roleOptional.isEmpty()) {
                throw new RuntimeException("Role not found for id: " + roleId);
            }

            Role role = roleOptional.get();

            // Check if the role is already locked
            if (Boolean.TRUE.equals(role.getIsLocked())) {
                throw new RuntimeException("Role is already locked and pending deletion.");
            }

            // Lock the role to prevent further actions
            role.setIsLocked(true);
            roleRepository.save(role);

            // Convert the role to JSON (this will be stored in the newData column)
            String newData = objectMapper.writeValueAsString(role);

            // Save the request to dual_auth_data for deletion
            DualAuthData dualAuthData = DualAuthData.builder()
                    .entity("Role")
                    .newData(newData)
                    .createdBy(adminId)
                    .status("Pending")
                    .action("Delete")
                    .build();

            DualAuthData savedDualAuthData = dualAuthDataRepository.save(dualAuthData);
            return savedDualAuthData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to request role deletion: " + e.getMessage(), e);
        }
    }



    public void approveRoleDeletion(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                // Deserialize the JSON data to a Role object
                Role role = objectMapper.readValue(dualAuthData.getNewData(), Role.class);

                // Find the role in the database
                Optional<Role> roleOptional = roleRepository.findById(role.getRoleId());
                if (roleOptional.isPresent()) {
                    // Delete the role from the database
                    roleRepository.delete(roleOptional.get());

                    // Update the DualAuthData status to approved
                    Integer adminId = authenticationService.getLoggedInUserId();
                    dualAuthData.setReviewedBy(adminId);
                    dualAuthData.setStatus("Approved");
                    dualAuthDataRepository.save(dualAuthData);

                    log.info("Role with ID {} has been deleted upon approval.", role.getRoleId());
                } else {
                    log.error("Role not found for deletion with ID: {}", role.getRoleId());
                }
            } catch (Exception e) {
                log.error("Error approving role deletion", e);
                throw new RuntimeException("Failed to approve role deletion", e);
            }
        } else {
            log.error("DualAuthData not found for ID: {}", dualAuthDataId);
            throw new EntityNotFoundException("DualAuthData not found for ID: " + dualAuthDataId);
        }
    }


    @CheckEntityLock(entityType = Role.class)
    public String activateRole(Integer roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            try {
                role.setIsLocked(true);
                roleRepository.save(role);
                String roleDataJson = objectMapper.writeValueAsString(roleOptional.get());
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("Role")
                        .newData(roleDataJson)
                        .createdBy(adminId)
                        .action("Activation")
                        .status("Pending")
                        .build();
                dualAuthDataRepository.save(dualAuthData);
                return "Role activation requested successfully";
            } catch (JsonProcessingException e) {
                log.error("Error serializing role data to JSON", e);
//                throw new RuntimeException("Failed to process role data", e);
                return("error") ;
            }
        } else {

            return ("Role not found");
        }
    }

    @CheckEntityLock(entityType = Role.class)
    public void deactivateRole(Integer roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();

            try {
                role.setIsLocked(true);
                roleRepository.save(role);
                String roleDataJson = objectMapper.writeValueAsString(roleOptional.get());
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("Role")
                        .newData(roleDataJson)
                        .createdBy(adminId)
                        .action("Activation")
                        .status("Pending")
                        .build();
                dualAuthDataRepository.save(dualAuthData);
            } catch (JsonProcessingException e) {
                log.error("Error serializing role data to JSON", e);
                throw new RuntimeException("Failed to process role data", e);
            }
        } else {
            throw new EntityNotFoundException("Role not found");
        }
    }



    public void approveDeactivateRole(Integer dualAuthDataId) {

        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                // Deserialize the JSON data to a Role object
                Role role = objectMapper.readValue(dualAuthData.getNewData(), Role.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                // Check if the entity in DualAuthData is "Role"
                if ("Role".equals(dualAuthData.getEntity())) {
                    // Find the role by ID in the database
                    Optional<Role> roleOptional = roleRepository.findById(role.getRoleId());
                    if (roleOptional.isPresent()) {
                        Role existingRole = roleOptional.get();

                        // Set the role status to de-active and unlock it
                        existingRole.setActiveStatus("de-active");
                        existingRole.setIsLocked(false);
                        roleRepository.save(existingRole);

                        // Update DualAuthData status to approved
                        dualAuthData.setReviewedBy(adminId);
                        dualAuthData.setStatus("Approved");
                        dualAuthDataRepository.save(dualAuthData);

                        log.info("Deactivation approved successfully for role ID: {}", existingRole.getRoleId());
                    } else {
                        log.error("Role not found for deactivation with ID: {}", role.getRoleId());
                    }
                } else {
                    log.error("Invalid entity for deactivation: {}", dualAuthData.getEntity());
                }
            } catch (Exception e) {
                log.error("Error approving deactivation", e);
                throw new RuntimeException("Failed to approve role deactivation", e);
            }
        } else {
            log.error("DualAuthData not found for ID: {}", dualAuthDataId);
            throw new EntityNotFoundException("DualAuthData not found for ID: " + dualAuthDataId);
        }
    }


    public void approveActivateRole(Integer dualAuthDataId) {

        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                // Deserialize the JSON data to a Role object
                Role role = objectMapper.readValue(dualAuthData.getNewData(), Role.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                // Check if the entity in DualAuthData is "Role"
                if ("Role".equals(dualAuthData.getEntity())) {
                    // Find the role by ID in the database
                    Optional<Role> roleOptional = roleRepository.findById(role.getRoleId());
                    if (roleOptional.isPresent()) {
                        Role existingRole = roleOptional.get();

                        // Set the role status to de-active and unlock it
                        existingRole.setActiveStatus("active");
                        existingRole.setIsLocked(false);
                        roleRepository.save(existingRole);

                        // Update DualAuthData status to approved
                        dualAuthData.setReviewedBy(adminId);
                        dualAuthData.setStatus("Approved");
                        dualAuthDataRepository.save(dualAuthData);

                        log.info("activation approved successfully for role ID: {}", existingRole.getRoleId());
                    } else {
                        log.error("Role not found for activation with ID: {}", role.getRoleId());
                    }
                } else {
                    log.error("Invalid entity for activation: {}", dualAuthData.getEntity());
                }
            } catch (Exception e) {
                log.error("Error approving activation", e);
                throw new RuntimeException("Failed to approve role activation", e);
            }
        } else {
            log.error("DualAuthData not found for ID: {}", dualAuthDataId);
            throw new EntityNotFoundException("DualAuthData not found for ID: " + dualAuthDataId);
        }
    }

    public String rejectRequest(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                // Parse the new data to extract the userId
                Role role = objectMapper.readValue(dualAuthData.getNewData(), Role.class);
                Integer userId = role.getRoleId(); // Assuming UserRequest has a method getId()

                Optional<Role> roleOptional = roleRepository.findById(userId);

                if (roleOptional.isPresent()) {
                    Role existingRole  = roleOptional.get();

                    // Check if the user is locked
                    if (Boolean.TRUE.equals(role.getIsLocked())) {
                        // Unlock the user
                        existingRole .setIsLocked(false);
                        roleRepository.save(existingRole);

                        // Update the dualAuthData record to reflect the rejection
                        Integer adminId = authenticationService.getLoggedInUserId();
                        dualAuthData.setReviewedBy(adminId);
                        dualAuthData.setStatus("Rejected");
                        dualAuthDataRepository.save(dualAuthData);

                        return "Request has been rejected, and the role is now unlocked.";
                    } else {
                        return "Role is not locked, no action needed.";
                    }
                } else {
                    return "Role not found.";
                }
            } catch (Exception e) {
                log.error("Error rejecting the request", e);
                return "Error rejecting the request.";
            }
        } else {
            return "DualAuthData not found.";
        }
    }

}
