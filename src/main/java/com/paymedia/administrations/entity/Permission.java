package com.paymedia.administrations.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="permission_id")
    private Long permissionId;
    @Column(name="permission_name", unique = true)
    private String permissionName;

//    public Permission(Long permissionId) {
//        this.permissionId = permissionId;
//    }
//
//    // Default constructor (needed for JPA)
//    public Permission() {
//    }

    //
}

