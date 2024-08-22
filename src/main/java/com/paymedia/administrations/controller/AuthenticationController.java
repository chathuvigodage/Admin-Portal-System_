package com.paymedia.administrations.controller;

import com.paymedia.administrations.model.AuthenticationResponse;
import com.paymedia.administrations.model.response.CommonResponse;
import com.paymedia.administrations.model.request.LoginRequest;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authService;

//    public AuthenticationController(AuthenticationService authService) {
//        this.authService = authService;
//    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> register(
            @RequestBody UserRequest request
    ){
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.ok(new CommonResponse<>(true, "User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<AuthenticationResponse>> login(
            @RequestBody LoginRequest request
    ){
        log.info("===================================================================> reached the login method");
        AuthenticationResponse response = authService.authenticate(request);

        log.info("===========================================================> after the service cal");
        return ResponseEntity.ok(new CommonResponse<>(true, "Login successful", response));
    }
}
