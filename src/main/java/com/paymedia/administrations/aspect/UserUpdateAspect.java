package com.paymedia.administrations.aspect;

import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.exception.UserLockedException;
import com.paymedia.administrations.repository.UserRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class UserUpdateAspect {

    private final UserRepository userRepository;

    public UserUpdateAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Before("execution(* com.paymedia.administrations.service.UserService.requestUserUpdate(..)) && args(id,..)")
    public void checkIfUserIsLocked(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (Boolean.TRUE.equals(user.getIsLocked())) {
                throw new UserLockedException("User is locked and cannot be updated.");
            }
        }
    }
}