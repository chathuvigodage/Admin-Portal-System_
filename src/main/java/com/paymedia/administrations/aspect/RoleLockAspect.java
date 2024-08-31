package com.paymedia.administrations.aspect;

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
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class RoleLockAspect {

    private final RoleRepository roleRepository;

    public RoleLockAspect(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Pointcut("@annotation(com.paymedia.administrations.annotations.CheckRoleLock) && args(id,..)")
    public void checkRoleLockPointcut(Integer id) {}

    @Before("checkRoleLockPointcut(id)")
    public void checkIfRoleIsLocked(Integer id) {
        Optional<Role> roleOptional = roleRepository.findById(id);
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();

            if (Boolean.TRUE.equals(role.getIsLocked())) {
                throw new RoleLockedException("Role is locked and cannot be updated or deleted.");
            }
        }else {
            throw new EntityNotFoundException("Role with ID " + id + " not found.");
        }
    }
}

