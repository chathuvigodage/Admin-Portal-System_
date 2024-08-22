package com.paymedia.administrations.service;

import com.paymedia.administrations.entity.Permission;
import com.paymedia.administrations.entity.Role;
import com.paymedia.administrations.entity.User;
import com.paymedia.administrations.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
public class PermissionService {

    @Autowired
    private UserRepository userRepository;

    public Page<Permission> getPermissionsByRoleId(Integer userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = user.getRole();
        return new PageImpl<>(new ArrayList<>(role.getPermissions()), pageable, role.getPermissions().size());
    }
}
