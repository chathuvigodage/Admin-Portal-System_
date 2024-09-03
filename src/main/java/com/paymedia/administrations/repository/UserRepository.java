package com.paymedia.administrations.repository;

import com.paymedia.administrations.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    List<User> findByCreatedOnBetweenOrUpdatedOnBetween(LocalDateTime startDate1, LocalDateTime endDate1, LocalDateTime startDate2, LocalDateTime endDate2);


    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.role.rolename LIKE %:searchTerm%")
    Page<User> searchByUsernameOrRoleName(@Param("searchTerm") String searchTerm, Pageable pageable);

}

