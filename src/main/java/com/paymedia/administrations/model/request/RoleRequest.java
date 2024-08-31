package com.paymedia.administrations.model.request;

import com.paymedia.administrations.entity.Permission;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter

public class RoleRequest {

    private Integer roleId;

    private String rolename;

    private Set<Long> permissions;
}
