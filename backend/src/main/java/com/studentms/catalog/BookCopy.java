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

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "book_copy")
public class BookCopy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "copy_id")
  private Integer copyId;

  @Column(name = "book_id", nullable = false)
  private Integer bookId;

  @Column(name = "barcode", nullable = false, unique = true, length = 40)
  private String barcode;

  @Column(name = "location_code", length = 40)
  private String locationCode;

  @Column(name = "condition_level", nullable = false, length = 20)
  private String conditionLevel;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Column(name = "last_inventory_date")
  private LocalDate lastInventoryDate;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;
}
