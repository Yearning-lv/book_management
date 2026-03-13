package com.studentms.circulation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "borrow_record")
public class BorrowRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "record_id")
  private Integer recordId;

  @Column(name = "copy_id", nullable = false)
  private Integer copyId;

  @Column(name = "book_id", nullable = false)
  private Integer bookId;

  @Column(name = "reader_id", nullable = false)
  private Integer readerId;

  @Column(name = "operator_id", nullable = false)
  private Integer operatorId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "borrow_date", nullable = false)
  private LocalDate borrowDate;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(name = "return_date")
  private LocalDate returnDate;

  @Column(name = "renew_count", nullable = false)
  private Integer renewCount;

  @Column(name = "fine_amount", nullable = false)
  private BigDecimal fineAmount;

  @Column(name = "reservation_id")
  private Integer reservationId;

  @Column(name = "remark", length = 255)
  private String remark;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;
}
