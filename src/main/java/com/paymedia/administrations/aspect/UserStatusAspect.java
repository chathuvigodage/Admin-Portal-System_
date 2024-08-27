//package com.paymedia.administrations.aspect;
//
//import com.paymedia.administrations.entity.User;
//import com.paymedia.administrations.exception.UserStatusException;
//import com.paymedia.administrations.repository.UserRepository;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Aspect
//@Component
//public class UserStatusAspect {
//
//    private final UserRepository userRepository;
//
//    public UserStatusAspect(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Before("@annotation(com.paymedia.administrations.annotations.CheckUserStatus) && args(userId,..)")
//    public void checkUserStatus(JoinPoint joinPoint, Integer userId) {
//        Optional<User> userOptional = userRepository.findById(userId);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            if ("active".equalsIgnoreCase(user.getActiveStatus())) {
//                throw new UserStatusException("This user is already active.");
//            } else if ("de-active".equalsIgnoreCase(user.getActiveStatus())) {
//                throw new UserStatusException("This user is already deactivated.");
//            }
//        } else {
//            throw new UserStatusException("User not found.");
//        }
//    }
//}
package com.paymedia.administrations.aspect;

import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.exception.UserStatusException;
import com.paymedia.administrations.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class UserStatusAspect {

    private final UserRepository userRepository;

    public UserStatusAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Before("@annotation(com.paymedia.administrations.annotations.CheckUserStatus) && args(userId,..)")
    public void checkUserStatus(JoinPoint joinPoint, Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String methodName = joinPoint.getSignature().getName();

            // Check the method name to determine the operation being performed
            if (methodName.equals("activateUser")) {
                if ("active".equalsIgnoreCase(user.getActiveStatus())) {
                    throw new UserStatusException("This user is already active.");
                }
            } else if (methodName.equals("deactivateUser")) {
                if ("de-active".equalsIgnoreCase(user.getActiveStatus())) {
                    throw new UserStatusException("This user is already deactivated.");
                }
            }
        } else {
            throw new EntityNotFoundException("User not found.");
        }
    }
}
