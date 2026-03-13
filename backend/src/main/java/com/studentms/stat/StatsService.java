package com.studentms.stat;

import com.studentms.catalog.Book;
import com.studentms.catalog.BookCategory;
import com.studentms.catalog.BookCategoryRepository;
import com.studentms.catalog.BookCopyRepository;
import com.studentms.catalog.BookRepository;
import com.studentms.circulation.BorrowRecord;
import com.studentms.circulation.BorrowRecordRepository;
import com.studentms.circulation.CirculationService;
import com.studentms.circulation.ReservationRepository;
import com.studentms.security.Role;
import com.studentms.user.LibraryUserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
  private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

  private final BookRepository bookRepository;
  private final BookCopyRepository bookCopyRepository;
  private final LibraryUserRepository libraryUserRepository;
  private final BorrowRecordRepository borrowRecordRepository;
  private final ReservationRepository reservationRepository;
  private final BookCategoryRepository bookCategoryRepository;
  private final CirculationService circulationService;

  public StatsService(BookRepository bookRepository,
                      BookCopyRepository bookCopyRepository,
                      LibraryUserRepository libraryUserRepository,
                      BorrowRecordRepository borrowRecordRepository,
                      ReservationRepository reservationRepository,
                      BookCategoryRepository bookCategoryRepository,
                      CirculationService circulationService) {
    this.bookRepository = bookRepository;
    this.bookCopyRepository = bookCopyRepository;
    this.libraryUserRepository = libraryUserRepository;
    this.borrowRecordRepository = borrowRecordRepository;
    this.reservationRepository = reservationRepository;
    this.bookCategoryRepository = bookCategoryRepository;
    this.circulationService = circulationService;
  }

  public Map<String, Object> overview() {
    circulationService.refreshOverdueRecords();
    Map<String, Object> data = new HashMap<>();
    data.put("bookCount", bookRepository.countByIsDeleted(0));
    data.put("copyCount", bookCopyRepository.countByIsDeleted(0));
    data.put("readerCount", libraryUserRepository.countByRoleCodeAndIsDeleted(Role.READER.getCode(), 0));
    data.put("activeBorrowCount", borrowRecordRepository.countByStatusInAndIsDeleted(List.of("BORROWED", "OVERDUE"), 0));
    data.put("overdueCount", borrowRecordRepository.countByStatusInAndIsDeleted(List.of("OVERDUE"), 0));
    data.put("reservationCount", reservationRepository.countByStatusInAndIsDeleted(List.of("WAITING", "READY"), 0));
    data.put("availableCopyCount", bookCopyRepository.countByStatusAndIsDeleted("AVAILABLE", 0));
    return data;
  }

  public Map<String, Object> categoryDistribution() {
    List<Map<String, Object>> items = new ArrayList<>();
    List<Book> books = bookRepository.findAll().stream()
        .filter(book -> book.getIsDeleted() != null && book.getIsDeleted() == 0)
        .toList();
    for (BookCategory category : bookCategoryRepository.findByIsDeletedOrderByCategoryCodeAsc(0)) {
      long titleCount = books.stream().filter(book -> category.getCategoryId().equals(book.getCategoryId())).count();
      int availableCount = books.stream()
          .filter(book -> category.getCategoryId().equals(book.getCategoryId()))
          .map(Book::getAvailableCopies)
          .filter(value -> value != null)
          .mapToInt(Integer::intValue)
          .sum();
      Map<String, Object> item = new HashMap<>();
      item.put("categoryName", category.getCategoryName());
      item.put("titleCount", titleCount);
      item.put("availableCopies", availableCount);
      items.add(item);
    }
    Map<String, Object> data = new HashMap<>();
    data.put("items", items);
    return data;
  }

  public Map<String, Object> borrowTrend() {
    LocalDate current = LocalDate.now().withDayOfMonth(1);
    Map<String, Long> monthCounter = new LinkedHashMap<>();
    for (int i = 5; i >= 0; i--) {
      monthCounter.put(current.minusMonths(i).format(MONTH_FORMATTER), 0L);
    }
    for (BorrowRecord record : borrowRecordRepository.findAll()) {
      if (record.getIsDeleted() == null || record.getIsDeleted() != 0 || record.getBorrowDate() == null) {
        continue;
      }
      String month = record.getBorrowDate().withDayOfMonth(1).format(MONTH_FORMATTER);
      if (monthCounter.containsKey(month)) {
        monthCounter.put(month, monthCounter.get(month) + 1);
      }
    }
    List<Map<String, Object>> items = new ArrayList<>();
    monthCounter.forEach((month, count) -> {
      Map<String, Object> item = new HashMap<>();
      item.put("month", month);
      item.put("count", count);
      items.add(item);
    });
    Map<String, Object> data = new HashMap<>();
    data.put("items", items);
    return data;
  }

  public Map<String, Object> topBooks() {
    Map<Integer, Long> counter = new HashMap<>();
    for (BorrowRecord record : borrowRecordRepository.findAll()) {
      if (record.getIsDeleted() == null || record.getIsDeleted() != 0) {
        continue;
      }
      Integer bookId = record.getBookId();
      if (bookId == null) {
        continue;
      }
      counter.put(bookId, counter.getOrDefault(bookId, 0L) + 1L);
    }
    List<Map<String, Object>> items = counter.entrySet().stream()
        .sorted(Map.Entry.<Integer, Long>comparingByValue(Comparator.reverseOrder()))
        .limit(5)
        .map(entry -> {
          Integer bookId = entry.getKey();
          Book book = bookId == null ? null : bookRepository.findById(bookId).orElse(null);
          Map<String, Object> item = new HashMap<>();
          item.put("bookId", bookId);
          item.put("title", book == null ? "未知图书" : book.getTitle());
          item.put("author", book == null ? "" : book.getAuthor());
          item.put("count", entry.getValue());
          return item;
        })
        .toList();
    Map<String, Object> data = new HashMap<>();
    data.put("items", items);
    return data;
  }
}
