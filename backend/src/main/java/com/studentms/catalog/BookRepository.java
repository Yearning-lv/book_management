package com.studentms.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
  boolean existsByIsbnAndIsDeleted(String isbn, Integer isDeleted);

  boolean existsByIsbnAndBookIdNotAndIsDeleted(String isbn, Integer bookId, Integer isDeleted);

  long countByIsDeleted(Integer isDeleted);
}
