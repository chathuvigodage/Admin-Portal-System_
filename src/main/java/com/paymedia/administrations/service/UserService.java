package com.paymedia.administrations.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymedia.administrations.annotations.CheckEntityLock;
import com.paymedia.administrations.annotations.CheckUserLock;
import com.paymedia.administrations.annotations.CheckUserStatus;
import com.paymedia.administrations.entity.DualAuthData;
import com.paymedia.administrations.entity.Role;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.model.UserSearchCriteria;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.repository.DualAuthDataRepository;
import com.paymedia.administrations.repository.RoleRepository;
import com.paymedia.administrations.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DualAuthDataRepository dualAuthDataRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PasswordEncoder passwordEncoder;



    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchByUsernameOrRoleName(searchTerm, pageable);
    }


    public DualAuthData createUser(UserRequest userRequest) {
        log.info("Starting to create user...");
        try {
            Integer adminId = authenticationService.getLoggedInUserId();
            log.info("*********************Logged-in user ID: {}", adminId);
            if (adminId == null) {
                throw new RuntimeException("Logged-in user ID is null");
            }

            String newData = objectMapper.writeValueAsString(userRequest);
            log.info("UserRequest JSON: {}", newData);


            DualAuthData dualAuthData = DualAuthData.builder()
                    .entity("User")
                    .newData(newData)
                    .createdBy(adminId)
                    .status("Pending")
                    .action("Create")
                    .build();


            DualAuthData savedDualAuthData = dualAuthDataRepository.save(dualAuthData);
            log.info("DualAuthData saved successfully with ID: {}", savedDualAuthData.getId());
            return savedDualAuthData;
        } catch (Exception e) {
            log.error("Error creating DualAuthData", e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public void approveUser(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                UserRequest userRequest = objectMapper.readValue(dualAuthData.getNewData(), UserRequest.class);
                Optional<Role> roleOptional = roleRepository.findById(userRequest.getRoleId());
                Integer adminId = authenticationService.getLoggedInUserId();
                if(roleOptional.isEmpty()) {
                    log.info("register -> role not found");
                }

                Role role = roleOptional.get();


                User user = User.builder()
                        .firstName(userRequest.getFirstName())
                        .lastName(userRequest.getLastName())
                        .username(userRequest.getUsername())
                        .password(passwordEncoder.encode(userRequest.getPassword()))
                        .role(role) // Assign role based on role ID
                        .build();

                userRepository.save(user);

                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);


            } catch (Exception e) {
                log.error("Error approving user", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }
    }



    public void approveDeleteUser(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                UserRequest userRequest = objectMapper.readValue(dualAuthData.getNewData(), UserRequest.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                // Ensure ID is not null for deletion
                if (userRequest.getId() == null) {
                    log.error("User ID is null; cannot proceed with deletion.");
                    return;
                }

                log.info("Attempting to delete user with ID: {}", userRequest.getId());

                if (dualAuthData.getEntity().equals("User")) {
                    Optional<User> userToDelete = userRepository.findById(userRequest.getId());

                    if (userToDelete.isPresent()) {
                        userRepository.delete(userToDelete.get());
                        log.info("User deleted successfully: {}", userToDelete.get().getUsername());
                    } else {
                        log.error("User not found for deletion with ID: {}", userRequest.getId());
                        return;
                    }
                } else {
                    log.error("Invalid entity for deletion: {}", dualAuthData.getEntity());
                    return;
                }

                // Update DualAuthData status to approved
                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);

                log.info("Deletion approved successfully for entity: {}", dualAuthData.getEntity());

            } catch (Exception e) {
                log.error("Error approving deletion", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }
    }


    public void approveActivateUser(Integer dualAuthDataId) {

        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                UserRequest userRequest = objectMapper.readValue(dualAuthData.getNewData(), UserRequest.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                if (dualAuthData.getEntity().equals("User")) {
                    Optional<User> userOptional = userRepository.findById(userRequest.getId());
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        user.setActiveStatus("active");
                        userRepository.save(user);
                    } else {
                        log.error("User not found for activation with ID: {}", userRequest.getId());
                        return;
                    }
                } else {
                    log.error("Invalid entity for activation: {}", dualAuthData.getEntity());
                    return;
                }

                // Update DualAuthData status to approved
                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);

                log.info("Activation approved successfully for entity: {}", dualAuthData.getEntity());

            } catch (Exception e) {
                log.error("Error approving activation", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }

    }

    public void approveDeactivateUser(Integer dualAuthDataId) {

        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                UserRequest userRequest = objectMapper.readValue(dualAuthData.getNewData(), UserRequest.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                if (dualAuthData.getEntity().equals("User")) {
                    Optional<User> userOptional = userRepository.findById(userRequest.getId());
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        user.setActiveStatus("de-active");
                        userRepository.save(user);
                    } else {
                        log.error("User not found for deactivation with ID: {}", userRequest.getId());
                        return;
                    }
                } else {
                    log.error("Invalid entity for deactivation: {}", dualAuthData.getEntity());
                    return;
                }

                // Update DualAuthData status to approved
                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);

                log.info("Deactivation approved successfully for entity: {}", dualAuthData.getEntity());

            } catch (Exception e) {
                log.error("Error approving deactivation", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }

    }

    public void approveUpdateUser(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                // Parse the new data from the DualAuthData entry
                UserRequest updatedUserRequest = objectMapper.readValue(dualAuthData.getNewData(), UserRequest.class);
                Integer adminId = authenticationService.getLoggedInUserId();

                if (dualAuthData.getEntity().equals("User")) {
                    Optional<User> userToUpdate = userRepository.findById(updatedUserRequest.getId());
                    Optional<Role> roleOptional = roleRepository.findById(updatedUserRequest.getRoleId());

                    if(roleOptional.isEmpty()) {
                        log.info("register -> role not found");
                    }

                    Role role = roleOptional.get();

                    if (userToUpdate.isPresent()) {
                        User user = userToUpdate.get();
                        user.setFirstName(updatedUserRequest.getFirstName());
                        user.setLastName(updatedUserRequest.getLastName());
                        user.setUsername(updatedUserRequest.getUsername());
                        user.setPassword(passwordEncoder.encode(updatedUserRequest.getPassword()));
                        user.setRole(role);

                        user.setIsLocked(false);
                        userRepository.save(user);
                        log.info("User updated successfully: {}", user.getUsername());
                    } else {
                        log.error("User not found for updating with ID: {}", updatedUserRequest.getId());
                        return;
                    }
                } else {
                    log.error("Invalid entity for updating: {}", dualAuthData.getEntity());
                    return;
                }

                // Update DualAuthData status to approved
                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Approved");
                dualAuthDataRepository.save(dualAuthData);

                log.info("Update approved successfully for entity: {}", dualAuthData.getEntity());

            } catch (Exception e) {
                log.error("Error approving update", e);
            }
        } else {
            log.error("DualAuthData not found for id: {}", dualAuthDataId);
        }
    }


    public void rejectUser(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);


        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();


            try {
                Integer adminId = authenticationService.getLoggedInUserId();

                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Rejected");
                dualAuthDataRepository.save(dualAuthData);
            } catch (Exception e) {
                log.error("Error approving user", e);
            }
        }
    }

    public String rejectRequest(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {
                // Parse the new data to extract the userId
                UserRequest updatedUserRequest = objectMapper.readValue(dualAuthData.getNewData(), UserRequest.class);
                Integer userId = updatedUserRequest.getId(); // Assuming UserRequest has a method getId()

                Optional<User> userOptional = userRepository.findById(userId);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // Check if the user is locked
                    if (Boolean.TRUE.equals(user.getIsLocked())) {
                        // Unlock the user
                        user.setIsLocked(false);
                        userRepository.save(user);

                        // Update the dualAuthData record to reflect the rejection
                        Integer adminId = authenticationService.getLoggedInUserId();
                        dualAuthData.setReviewedBy(adminId);
                        dualAuthData.setStatus("Rejected");
                        dualAuthDataRepository.save(dualAuthData);

                        return "Request has been rejected, and the user is now unlocked.";
                    } else {
                        return "User is not locked, no action needed.";
                    }
                } else {
                    return "User not found.";
                }
            } catch (Exception e) {
                log.error("Error rejecting the request", e);
                return "Error rejecting the request.";
            }
        } else {
            return "DualAuthData not found.";
        }
    }


    @CheckEntityLock(entityType = User.class)
    public String requestUserUpdate(Integer id, UserRequest updatedUserRequest) {
        Optional<User> userToUpdate = userRepository.findById(id);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (userToUpdate.isPresent() && adminId != null) {
            User user = userToUpdate.get();

            // Check if the user is locked, and if so, return a message
            if (Boolean.TRUE.equals(user.getIsLocked())) {
                return "User is already requested for update and is locked.";
            }

            try {
                // Lock the user for updating
                user.setIsLocked(true);
                userRepository.save(user);

                // Convert the current user data to JSON for oldData
                String oldDataJson = objectMapper.writeValueAsString(user);

                // Convert the updated user data to JSON for newData
                String newDataJson = objectMapper.writeValueAsString(updatedUserRequest);

                // Create DualAuthData for updating
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("User")
                        .oldData(oldDataJson)
                        .newData(newDataJson)
                        .createdBy(adminId)
                        .action("Update")
                        .status("Pending")
                        .build();

                dualAuthDataRepository.save(dualAuthData);
                return "User update request submitted successfully";
            } catch (Exception e) {
                log.error("Error requesting user update", e);
                return "Error submitting user update request";
            }
        } else {
            log.error("User not found or admin ID is null");
            return "User not found or admin ID is null";
        }
    }


    @CheckEntityLock(entityType = User.class)
    public String requestUserDeletion(Integer id) {
        Optional<User> userToDelete = userRepository.findById(id);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (userToDelete.isPresent() && adminId != null) {
            User user = userToDelete.get();

            // Lock the user
            user.setIsLocked(true);
            userRepository.save(user);

            try {
                String userDataJson = objectMapper.writeValueAsString(user);

                // Create DualAuthData for deletion
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("User")
                        .newData(userDataJson)
                        .createdBy(adminId)
                        .action("Delete")
                        .status("Pending")
                        .build();

                dualAuthDataRepository.save(dualAuthData);
                return "User deletion requested successfully";
            } catch (Exception e) {
                log.error("Error requesting user deletion", e);
                return "Error deleting user request";
            }
        } else {
            log.error("User not found or admin ID is null");
            return "User not found";
        }
    }

   @CheckUserStatus
    public String activateUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (userOptional.isPresent()) {
            try {
                String userDataJson = objectMapper.writeValueAsString(userOptional.get());
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("User")
                        .newData(userDataJson)
                        .createdBy(adminId)
                        .action("Activation")
                        .status("Pending")
                        .build();
                dualAuthDataRepository.save(dualAuthData);
                return "User activation requested successfully";
            } catch (JsonProcessingException e) {
                log.error("Error serializing user data to JSON", e);
//                throw new RuntimeException("Failed to process user data", e);
                  return("error") ;
            }
        } else {
//            throw new EntityNotFoundException("User not found");
              return ("User not found");
        }
    }

    @CheckUserStatus
    public void deactivateUser(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (userOptional.isPresent()) {
            try {
                String userDataJson = objectMapper.writeValueAsString(userOptional.get());
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("User")
                        .newData(userDataJson)
                        .createdBy(adminId)
                        .action("Activation")
                        .status("Pending")
                        .build();
                dualAuthDataRepository.save(dualAuthData);
            } catch (JsonProcessingException e) {
                log.error("Error serializing user data to JSON", e);
                throw new RuntimeException("Failed to process user data", e);
            }
        } else {
            throw new EntityNotFoundException("User not found");
        }
    }





}