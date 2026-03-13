package com.studentms.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LibraryUserRepository extends JpaRepository<LibraryUser, Integer>, JpaSpecificationExecutor<LibraryUser> {
  Optional<LibraryUser> findByAccountAndIsDeleted(String account, Integer isDeleted);

  boolean existsByAccountAndIsDeleted(String account, Integer isDeleted);

  boolean existsByAccountAndUserIdNotAndIsDeleted(String account, Integer userId, Integer isDeleted);

  long countByRoleCodeAndIsDeleted(String roleCode, Integer isDeleted);
}
