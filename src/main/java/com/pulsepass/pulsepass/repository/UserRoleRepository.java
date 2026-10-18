package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.UserRole;
import com.pulsepass.pulsepass.domain.UserRoleId;
import com.pulsepass.pulsepass.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("SELECT r.id.role FROM UserRole r WHERE r.id.userId = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}
