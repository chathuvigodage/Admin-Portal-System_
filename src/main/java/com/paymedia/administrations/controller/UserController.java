package com.paymedia.administrations.controller;

import com.paymedia.administrations.entity.DualAuthData;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.model.response.CommonResponse;
import com.paymedia.administrations.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<User>>> getUsers(
            @RequestParam(value = "search", required = false) String searchTerm,
            Pageable pageable) {
        Page<User> users = userService.searchUsers(searchTerm, pageable);
        return ResponseEntity.ok(new CommonResponse<>(true, "Users fetched successfully", users));
    }

//    @PostMapping("/create")
//    public ResponseEntity<CommonResponse<String>> createUser(@RequestBody User user) {
//        // Get the current authentication object
//
//
//        // Call the service to create the user
//        userService.createUser(user);
//
//        return ResponseEntity.ok(new CommonResponse<>(true, "User creation request submitted successfully"));
//    }

//    @PostMapping("/create")
//    public ResponseEntity<CommonResponse<UserRequest>> createUser(@RequestBody User user, @RequestParam Long creatorId) {
//        UserDto createdUser = dualAuthSystemService.createPendingUser(user, creatorId);
//        return ResponseEntity.ok(new ApiResponse<>(true, "User created and pending approval", createdUser));
//    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<DualAuthData>> createUser(@RequestBody UserRequest userRequest) {
        DualAuthData createdUser = userService.createUser(userRequest);
        return ResponseEntity.ok(new CommonResponse<>(true, "User creation request submitted successfully", createdUser));
    }


    @PostMapping("/approve/{id}")
    public ResponseEntity<CommonResponse<String>> approveUser(@PathVariable Integer id) {
        userService.approveUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "User approved successfully", null));
    }

    @PostMapping("/approveDeletion/{id}")
    public ResponseEntity<CommonResponse<String>> approveDeleteUser(@PathVariable Integer id) {
        userService.approveDeleteUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "User approved Deletion successfully", null));
    }

    @PostMapping("/approveActivation/{id}")
    public ResponseEntity<CommonResponse<String>> approveActivateUser(@PathVariable Integer id) {
        userService.approveActivateUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "User approved Activation successfully", null));
    }

    @PostMapping("/approveDeactivation/{id}")
    public ResponseEntity<CommonResponse<String>> approveDeactivateUser(@PathVariable Integer id) {
        userService.approveDeactivateUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "User approved Deactivation successfully", null));
    }



    @PostMapping("/reject/{id}")
    public ResponseEntity<CommonResponse<String>> rejectUser(@PathVariable Integer id) {
        userService.rejectUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "User rejected successfully", null));
    }

    @PostMapping("/rejectRequest/{id}")
    public ResponseEntity<CommonResponse<String>> rejectRequest(@PathVariable Integer id) {
        String response = userService.rejectRequest(id);
        return ResponseEntity.ok(new CommonResponse<>(true, response, null));
    }

//    @PutMapping("/update/{id}")
//    public ResponseEntity<CommonResponse<String>> updateUserRequest(
//            @PathVariable Integer id,
//            @RequestBody UserRequest newUserRequest) {
//
//        String result = userService.updateUserRequest(id, newUserRequest);
//        return ResponseEntity.ok(new CommonResponse<>(true, result, null));
//    }

    @PostMapping("/update/{id}")
    public ResponseEntity<CommonResponse<String>> requestUserUpdate(@PathVariable Integer id, @RequestBody UserRequest updatedUserRequest) {
        String response = userService.requestUserUpdate(id, updatedUserRequest);
        return ResponseEntity.ok(new CommonResponse<>(true, response, null));
    }

    @PostMapping("/approveUpdation/{id}")
    public ResponseEntity<CommonResponse<String>> approveUpdateUser(@PathVariable Integer id) {
        userService.approveUpdateUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true,"User update approved successfully",null));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CommonResponse<String>> requestUserDeletion(
            @PathVariable Integer id) {

        String result = userService.requestUserDeletion(id);
        return ResponseEntity.ok(new CommonResponse<>(true, result, null));
    }

    @PostMapping("/active/{id}")
    public ResponseEntity<CommonResponse<String>> activateUser(@PathVariable Integer id) {
        userService.activateUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Activation request submitted successfully", null));
    }

    @PostMapping("/de-active/{id}")
    public ResponseEntity<CommonResponse<String>> deactivateUser(@PathVariable Integer id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Deactivation request submitted successfully", null));
    }


}
