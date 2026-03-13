package com.studentms.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReaderProfileRepository extends JpaRepository<ReaderProfile, Integer> {
  Optional<ReaderProfile> findByReaderNo(String readerNo);
}
