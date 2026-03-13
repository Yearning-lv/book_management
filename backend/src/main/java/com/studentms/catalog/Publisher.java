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
@Table(name = "publisher")
public class Publisher {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "publisher_id")
  private Integer publisherId;

  @Column(name = "publisher_name", nullable = false, unique = true, length = 100)
  private String publisherName;

  @Column(name = "contact_name", length = 50)
  private String contactName;

  @Column(name = "contact_phone", length = 20)
  private String contactPhone;

  @Column(name = "is_deleted", nullable = false)
  private Integer isDeleted;
}
