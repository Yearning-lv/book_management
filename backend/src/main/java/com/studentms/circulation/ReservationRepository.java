package com.studentms.circulation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer>, JpaSpecificationExecutor<Reservation> {
  boolean existsByReaderIdAndBookIdAndStatusInAndIsDeleted(Integer readerId, Integer bookId, Collection<String> statuses, Integer isDeleted);

  boolean existsByBookIdAndStatusInAndIsDeleted(Integer bookId, Collection<String> statuses, Integer isDeleted);

  long countByStatusInAndIsDeleted(Collection<String> statuses, Integer isDeleted);

  List<Reservation> findByBookIdAndStatusAndIsDeletedOrderByQueueNoAsc(Integer bookId, String status, Integer isDeleted);

  Optional<Reservation> findFirstByBookIdAndReaderIdAndStatusInAndIsDeletedOrderByQueueNoAsc(
      Integer bookId, Integer readerId, Collection<String> statuses, Integer isDeleted
  );

  @Query("select coalesce(max(r.queueNo), 0) from Reservation r where r.bookId = :bookId and r.isDeleted = 0")
  Integer findMaxQueueNo(@Param("bookId") Integer bookId);
}
