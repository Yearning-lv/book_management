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

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "book")
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "book_id")
  private Integer bookId;

  @Column(name = "isbn", nullable = false, unique = true, length = 20)
  private String isbn;

  @Column(name = "title", nullable = false, length = 120)
  private String title;

  @Column(name = "author", nullable = false, length = 120)
  private String author;

  @Column(name = "category_id", nullable = false)
  private Integer categoryId;

  @Column(name = "publisher_id")
  private Integer publisherId;

  @Column(name = "publish_date")
  private LocalDate publishDate;

  @Column(name = "price")
  private BigDecimal price;

  @Column(name = "keywords", length = 200)
  private String keywords;

  @Column(name = "summary", length = 500)
  private String summary;

  @Column(name = "shelf_status", nullable = false, length = 20)
  private String shelfStatus;

  @Column(name = "total_copies", nullable = false)
  private Integer totalCopies;

  @Column(name = "available_copies", nullable = false)
  private Integer availableCopies;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;
}
