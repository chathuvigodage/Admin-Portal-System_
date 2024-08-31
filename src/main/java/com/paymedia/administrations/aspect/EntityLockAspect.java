package com.paymedia.administrations.aspect;

import com.paymedia.administrations.annotations.CheckEntityLock;
import com.paymedia.administrations.entity.Role;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.exception.RoleLockedException;
import com.paymedia.administrations.exception.UserLockedException;
import com.paymedia.administrations.repository.RoleRepository;
import com.paymedia.administrations.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class EntityLockAspect {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public EntityLockAspect(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Pointcut("@annotation(com.paymedia.administrations.annotations.CheckEntityLock) && args(id,..)")
    public void checkEntityLockPointcut(Integer id) {}

    @Before("checkEntityLockPointcut(id) && @annotation(checkEntityLock)")
    public void checkIfEntityIsLocked(JoinPoint joinPoint, CheckEntityLock checkEntityLock, Integer id) {
        Class<?> entityType = checkEntityLock.entityType();

        if (entityType.equals(User.class)) {
            checkUserLock(id);
        } else if (entityType.equals(Role.class)) {
            checkRoleLock(id);
        } else {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    private void checkUserLock(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (Boolean.TRUE.equals(user.getIsLocked())) {
                throw new UserLockedException("User is locked and cannot be updated or deleted.");
            }
        } else {
            throw new EntityNotFoundException("User with ID " + userId + " not found.");
        }
    }

    private void checkRoleLock(Integer roleId) {
        Optional<Role> roleOptional = roleRepository.findById(roleId);
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            if (Boolean.TRUE.equals(role.getIsLocked())) {
                throw new RoleLockedException("Role is locked and cannot be updated or deleted.");
            }
        } else {
            throw new EntityNotFoundException("Role with ID " + roleId + " not found.");
        }
    }
}

