package com.studentms.circulation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Integer>, JpaSpecificationExecutor<BorrowRecord> {
  List<BorrowRecord> findByStatusInAndIsDeleted(Collection<String> statuses, Integer isDeleted);

  boolean existsByReaderIdAndBookIdAndStatusInAndIsDeleted(Integer readerId, Integer bookId, Collection<String> statuses, Integer isDeleted);

  long countByStatusInAndIsDeleted(Collection<String> statuses, Integer isDeleted);
}
