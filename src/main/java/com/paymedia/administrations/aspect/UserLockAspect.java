package com.paymedia.administrations.aspect;

import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.exception.UserLockedException;
import com.paymedia.administrations.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class UserLockAspect {

    private final UserRepository userRepository;

    public UserLockAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Pointcut("@annotation(com.paymedia.administrations.annotations.CheckUserLock) && args(id,..)")
    public void checkUserLockPointcut(Integer id) {}

    @Before("checkUserLockPointcut(id)")
    public void checkIfUserIsLocked(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (Boolean.TRUE.equals(user.getIsLocked())) {
                throw new UserLockedException("User is locked and cannot be updated or deleted.");
            }
        }else {
            throw new EntityNotFoundException("User with ID " + id + " not found.");
        }
    }
}
