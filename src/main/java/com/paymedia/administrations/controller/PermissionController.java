package com.paymedia.administrations.controller;

import com.paymedia.administrations.entity.Permission;
import com.paymedia.administrations.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController

public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/roles")
    public Page<Permission> getPermissionsByRole(
            @RequestParam("userId") Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("name")));
        return permissionService.getPermissionsByRoleId(userId, pageable);
    }
}

