package com.studentms.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Integer>, JpaSpecificationExecutor<BookCopy> {
  boolean existsByBarcodeAndIsDeleted(String barcode, Integer isDeleted);

  boolean existsByBarcodeAndCopyIdNotAndIsDeleted(String barcode, Integer copyId, Integer isDeleted);

  long countByIsDeleted(Integer isDeleted);

  long countByBookIdAndIsDeleted(Integer bookId, Integer isDeleted);

  long countByBookIdAndStatusInAndIsDeleted(Integer bookId, Collection<String> statuses, Integer isDeleted);

  long countByStatusAndIsDeleted(String status, Integer isDeleted);

  Optional<BookCopy> findFirstByBookIdAndStatusAndIsDeleted(Integer bookId, String status, Integer isDeleted);
}
