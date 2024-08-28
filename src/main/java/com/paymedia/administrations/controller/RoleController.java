package com.paymedia.administrations.controller;

import com.paymedia.administrations.entity.DualAuthData;
import com.paymedia.administrations.model.request.RoleRequest;
import com.paymedia.administrations.model.response.CommonResponse;
import com.paymedia.administrations.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/create")
    public ResponseEntity<CommonResponse<DualAuthData>> createRole(@RequestBody RoleRequest roleRequest) {
        DualAuthData createdRole = roleService.createRole(roleRequest);
        return ResponseEntity.ok(new CommonResponse<>(true, "Role creation request submitted successfully", createdRole));
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<CommonResponse<String>> approveRole(@PathVariable Integer id) {
        roleService.approveRole(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "User approved successfully", null));
    }

}
