package com.paymedia.administrations.repository;

import com.paymedia.administrations.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByCreatedOnBetweenOrUpdatedOnBetween(LocalDateTime startDate1, LocalDateTime endDate1, LocalDateTime startDate2, LocalDateTime endDate2);

//    public List<Role> fetchRolesWithPermissions() {
//        return RoleRepository.findAll(); // Assuming roles are fetched with permissions eager-loaded or using a custom query
//    }
}
