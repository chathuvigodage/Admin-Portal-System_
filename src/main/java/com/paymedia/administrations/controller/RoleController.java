//package com.paymedia.administrations.controller;
//
//import com.paymedia.administrations.model.entity.Role;
//import com.paymedia.administrations.service.RoleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/roles")
//public class RoleController {
//
//    @Autowired
//    private RoleService roleService;
//
//    @PostMapping("/add")
//    public ResponseEntity<Role> addRole(@RequestBody Role role) {
//        return ResponseEntity.ok(roleService.saveRole(role));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Role>> getAllRoles() {
//        return ResponseEntity.ok(roleService.getAllRoles());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
//        return ResponseEntity.ok(roleService.getRoleById(id));
//    }
//}
