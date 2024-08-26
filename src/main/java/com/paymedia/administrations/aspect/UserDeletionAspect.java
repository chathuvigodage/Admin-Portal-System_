//package com.paymedia.administrations.aspect;
//
//import com.paymedia.administrations.entity.User;
//import com.paymedia.administrations.exception.UserLockedException;
//import com.paymedia.administrations.repository.UserRepository;
//import jakarta.persistence.EntityNotFoundException;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//@Aspect
//@Component
//public class UserDeletionAspect {
//
//    private final UserRepository userRepository;
//
//    @Autowired
//    public UserDeletionAspect(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Before("execution(* com.paymedia.administrations.service.UserService.requestUserDeletion(..)) && args(userId)")
//    public void checkUserLock(JoinPoint joinPoint, Integer userId) {
//        Optional<User> user = userRepository.findById(userId);
//
//        if (user.isPresent() && Boolean.TRUE.equals(user.get().getIsLocked())) {
//            throw new UserLockedException("User is already locked and pending deletion.");
//        }
//    }
//}
