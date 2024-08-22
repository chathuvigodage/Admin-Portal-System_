package com.paymedia.administrations.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

//    public void createUser(UserRequest userRequest) {
//    log.info("================================================>####");
//        try {
//            Integer adminId = authenticationService.getLoggedInUserId();
//            log.info("*********************Logged-in user ID: {}", adminId);
//            if (adminId == null) {
//                throw new RuntimeException("Logged-in user ID is null");
//            }
//            // Convert UserRequest to JSON
//            log.info("========================================>before new data");
//            String newData = objectMapper.writeValueAsString(userRequest);
//            log.info("====================>UserRequest JSON: {}", newData);
//
//            // Create DualAuthData
//            DualAuthData dualAuthData = DualAuthData.builder()
//                    .entity("User")
//                    .newData(newData)
//                    .createdBy(adminId)
//                    .status("Pending")
//                    .build();
//
//            dualAuthDataRepository.save(dualAuthData);
//            log.info("=====================>DualAuthData saved successfully with ID: {}", dualAuthData.getId());
//
//        } catch (Exception e) {
//            log.error("Error creating DualAuthData", e);
//        }
//
//    }


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

//                // Ensure ID is not null for deletion
//                if (userRequest.getId() == null) {
//                    log.error("User ID is null; cannot proceed with deletion.");
//                    return;
//                }
//
//                log.info("Attempting to delete user with ID: {}", userRequest.getId());

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

//                // Ensure ID is not null for deletion
//                if (userRequest.getId() == null) {
//                    log.error("User ID is null; cannot proceed with deletion.");
//                    return;
//                }
//
//                log.info("Attempting to delete user with ID: {}", userRequest.getId());

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




    public void rejectUser(Integer dualAuthDataId) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(dualAuthDataId);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();


            try {
                Integer adminId = authenticationService.getLoggedInUserId();

                dualAuthData.setReviewedBy(adminId);
                dualAuthData.setStatus("Rejected");
//                dualAuthData.setReviewedAt(LocalDateTime.now());
                dualAuthDataRepository.save(dualAuthData);
            } catch (Exception e) {
                log.error("Error approving user", e);
            }
        }
    }

    public String updateUserRequest(Integer id, UserRequest newUserRequest) {
        Optional<DualAuthData> optionalDualAuthData = dualAuthDataRepository.findById(id);

        if (optionalDualAuthData.isPresent()) {
            DualAuthData dualAuthData = optionalDualAuthData.get();

            try {

                String updatedNewData = objectMapper.writeValueAsString(newUserRequest);

                String currentNewData = dualAuthData.getNewData();
                dualAuthData.setOldData(currentNewData);

                dualAuthData.setNewData(updatedNewData);

                dualAuthDataRepository.save(dualAuthData);

                return "User request updated successfully";

            } catch (Exception e) {
                log.error("Error updating DualAuthData", e);
                return "Error updating user request";
            }
        } else {
            log.error("DualAuthData not found for id: {}", id);
            return "DualAuthData not found";
        }
    }

//    public String deleteDualAuthDataById(Integer id) {
//        try {
//            if (dualAuthDataRepository.existsById(id)) {
//                dualAuthDataRepository.deleteById(id);
//                return "DualAuthData record deleted successfully";
//            } else {
//                return "DualAuthData record not found";
//            }
//        } catch (Exception e) {
//            log.error("Error deleting DualAuthData", e);
//            return "Error deleting DualAuthData record";
//        }
//    }

    public String requestUserDeletion(Integer id) {
        Optional<User> userToDelete = userRepository.findById(id);
        Integer adminId = authenticationService.getLoggedInUserId();

        if (userToDelete.isPresent() && adminId != null) {
            try {
                String userDataJson = objectMapper.writeValueAsString(userToDelete.get());

                // Create DualAuthData for deletion
                DualAuthData dualAuthData = DualAuthData.builder()
                        .entity("User")
                        .newData(userDataJson)
                        .createdBy(adminId)
                        .action("Delete")
                        .status("Pending")
                        .build();

                dualAuthDataRepository.save(dualAuthData);
                return "User deleted successfully";
            } catch (Exception e) {
                log.error("Error requesting user deletion", e);
                return "Error deleting user request";
            }
        } else {
            log.error("User not found or admin ID is null");
            return "DualAuthData not found";
        }
    }

    public void activateUser(Integer userId) {
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