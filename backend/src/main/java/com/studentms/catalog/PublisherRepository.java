package com.studentms.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
  List<Publisher> findByIsDeletedOrderByPublisherNameAsc(Integer isDeleted);
}
