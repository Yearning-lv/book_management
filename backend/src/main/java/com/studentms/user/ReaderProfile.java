package com.studentms.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reader_profile")
public class ReaderProfile {
  @Id
  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "reader_no", nullable = false, unique = true, length = 30)
  private String readerNo;

  @Column(name = "max_borrow_count", nullable = false)
  private Integer maxBorrowCount;

  @Column(name = "current_borrow_count", nullable = false)
  private Integer currentBorrowCount;

  @Column(name = "fine_balance", nullable = false)
  private BigDecimal fineBalance;

  @Column(name = "card_status", nullable = false, length = 20)
  private String cardStatus;

  @Column(name = "grade", length = 30)
  private String grade;

  @Column(name = "note", length = 255)
  private String note;
}
