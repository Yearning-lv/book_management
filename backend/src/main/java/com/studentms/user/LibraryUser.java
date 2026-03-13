package com.studentms.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lib_user")
public class LibraryUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "role_code", nullable = false, length = 20)
  private String roleCode;

  @Column(name = "account", nullable = false, length = 30, unique = true)
  private String account;

  @Column(name = "password", nullable = false, length = 64)
  private String password;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "gender", length = 10)
  private String gender;

  @Column(name = "phone", length = 20)
  private String phone;

  @Column(name = "email", length = 80)
  private String email;

  @Column(name = "department", length = 80)
  private String department;

  @Column(name = "card_no", length = 30)
  private String cardNo;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;
}
