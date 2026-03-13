package com.studentms.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "book_category")
public class BookCategory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_id")
  private Integer categoryId;

  @Column(name = "category_code", nullable = false, unique = true, length = 30)
  private String categoryCode;

  @Column(name = "category_name", nullable = false, length = 80)
  private String categoryName;

  @Column(name = "loan_days", nullable = false)
  private Integer loanDays;

  @Column(name = "location_code", length = 30)
  private String locationCode;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;
}
