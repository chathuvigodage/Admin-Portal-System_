package com.paymedia.administrations.service;

import com.paymedia.administrations.entity.Role;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.model.AuthenticationResponse;
import com.paymedia.administrations.model.request.LoginRequest;
import com.paymedia.administrations.model.request.UserRequest;
import com.paymedia.administrations.repository.RoleRepository;
import com.paymedia.administrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserRequest userRequest) {
//        Role role = roleRepository.findById(userRequest.getRoleId())
//                .orElseThrow(() -> new RuntimeException("Role not found"));

        Optional<Role> roleOptional = roleRepository.findById(userRequest.getRoleId());

        if(roleOptional.isEmpty()) {
            log.info("register -> role not found");
        }

        Role role = roleOptional.get();

        log.info("role exists");

        User user = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(role)
                .build();

        user = userRepository.save(user);
        String token = jwtService.generateToken(user);

        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        log.info("==================================================> 1line");
        log.info(loginRequest.getUsername());
        log.info(loginRequest.getPassword());
//       Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getUsername(),
//                        loginRequest.getPassword()
//                )
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("============================================>line2");
//        User user = repository.findByUsername(loginRequest.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        log.info("============================================> line3");
        if (userOptional.isEmpty()) {
            return new AuthenticationResponse(null);
        }
        log.info("============================================> line4");
        User user = userOptional.get();
        log.info("============================================> line5");

        String token = jwtService.generateToken(user);
        log.info("============================================> line6");

        return new AuthenticationResponse(token);


    }

    public Integer getLoggedInUserId () {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("=======================================>////");

        if (authentication != null && authentication.isAuthenticated()) {

            log.info("===================================>00000");
            String username = authentication.getName();
            log.info("===================================>99999");


            Optional<User> userOptional = userRepository.findByUsername(username);
            log.info("===================================>88888");
            if (userOptional.isPresent()) {
                log.info("===================================>77777");
                // Return the user's ID
                return userOptional.get().getId();

            }
        }

        return null;
    }
}
