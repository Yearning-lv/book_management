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

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reservation")
public class Reservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "reservation_id")
  private Integer reservationId;

  @Column(name = "book_id", nullable = false)
  private Integer bookId;

  @Column(name = "reader_id", nullable = false)
  private Integer readerId;

  @Column(name = "queue_no", nullable = false)
  private Integer queueNo;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "reserve_date", nullable = false)
  private LocalDate reserveDate;

  @Column(name = "pickup_deadline")
  private LocalDate pickupDeadline;

  @Column(name = "expire_date")
  private LocalDate expireDate;

  @Column(name = "note", length = 255)
  private String note;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;
}
