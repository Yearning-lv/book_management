package com.studentms.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Integer> {
  List<BookCategory> findByIsDeletedOrderByCategoryCodeAsc(Integer isDeleted);
}
