package com.studentms.circulation;

import com.studentms.catalog.Book;
import com.studentms.catalog.BookCategory;
import com.studentms.catalog.BookCategoryRepository;
import com.studentms.catalog.BookCopy;
import com.studentms.catalog.BookCopyRepository;
import com.studentms.catalog.BookRepository;
import com.studentms.catalog.CatalogService;
import com.studentms.common.RequestUtils;
import com.studentms.security.AuthSession;
import com.studentms.security.Role;
import com.studentms.user.LibraryUser;
import com.studentms.user.LibraryUserRepository;
import com.studentms.user.ReaderProfile;
import com.studentms.user.ReaderProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CirculationService {
  private static final List<String> ACTIVE_BORROW_STATUSES = List.of("BORROWED", "OVERDUE");
  private static final List<String> ACTIVE_RESERVATION_STATUSES = List.of("WAITING", "READY");
  private static final BigDecimal DAILY_FINE = new BigDecimal("0.50");

  private final BorrowRecordRepository borrowRecordRepository;
  private final ReservationRepository reservationRepository;
  private final BookRepository bookRepository;
  private final BookCopyRepository bookCopyRepository;
  private final BookCategoryRepository bookCategoryRepository;
  private final LibraryUserRepository libraryUserRepository;
  private final ReaderProfileRepository readerProfileRepository;
  private final CatalogService catalogService;

  public CirculationService(BorrowRecordRepository borrowRecordRepository,
                            ReservationRepository reservationRepository,
                            BookRepository bookRepository,
                            BookCopyRepository bookCopyRepository,
                            BookCategoryRepository bookCategoryRepository,
                            LibraryUserRepository libraryUserRepository,
                            ReaderProfileRepository readerProfileRepository,
                            CatalogService catalogService) {
    this.borrowRecordRepository = borrowRecordRepository;
    this.reservationRepository = reservationRepository;
    this.bookRepository = bookRepository;
    this.bookCopyRepository = bookCopyRepository;
    this.bookCategoryRepository = bookCategoryRepository;
    this.libraryUserRepository = libraryUserRepository;
    this.readerProfileRepository = readerProfileRepository;
    this.catalogService = catalogService;
  }

  public Page<Map<String, Object>> getBorrowList(String keyword, String status, int page, int pageSize, AuthSession session) {
    refreshOverdueRecords();
    Specification<BorrowRecord> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("isDeleted"), 0));
      if (status != null && !status.isBlank()) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (session != null && session.getRole() == Role.READER) {
        predicates.add(cb.equal(root.get("readerId"), session.getUserId()));
      }
      if (keyword != null && !keyword.isBlank()) {
        List<Integer> matchedBookIds = bookRepository.findAll().stream()
            .filter(book -> book.getIsDeleted() != null && book.getIsDeleted() == 0)
            .filter(book -> contains(book.getTitle(), keyword) || contains(book.getAuthor(), keyword) || contains(book.getIsbn(), keyword))
            .map(Book::getBookId)
            .toList();
        List<Integer> matchedReaderIds = libraryUserRepository.findAll().stream()
            .filter(user -> user.getIsDeleted() != null && user.getIsDeleted() == 0)
            .filter(user -> contains(user.getName(), keyword) || contains(user.getAccount(), keyword))
            .map(LibraryUser::getUserId)
            .toList();
        Predicate barcodeMatch = cb.disjunction();
        List<Integer> matchedCopyIds = bookCopyRepository.findAll().stream()
            .filter(copy -> copy.getIsDeleted() != null && copy.getIsDeleted() == 0)
            .filter(copy -> contains(copy.getBarcode(), keyword))
            .map(BookCopy::getCopyId)
            .toList();
        if (!matchedCopyIds.isEmpty()) {
          barcodeMatch = root.get("copyId").in(matchedCopyIds);
        }
        Predicate bookMatch = matchedBookIds.isEmpty() ? cb.disjunction() : root.get("bookId").in(matchedBookIds);
        Predicate readerMatch = matchedReaderIds.isEmpty() ? cb.disjunction() : root.get("readerId").in(matchedReaderIds);
        predicates.add(cb.or(bookMatch, readerMatch, barcodeMatch));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
    return borrowRecordRepository.findAll(
        specification,
        PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "recordId"))
    ).map(this::toBorrowMap);
  }

  public Page<Map<String, Object>> getReservationList(String status, int page, int pageSize, AuthSession session) {
    Specification<Reservation> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("isDeleted"), 0));
      if (status != null && !status.isBlank()) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      if (session != null && session.getRole() == Role.READER) {
        predicates.add(cb.equal(root.get("readerId"), session.getUserId()));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
    return reservationRepository.findAll(
        specification,
        PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "reservationId"))
    ).map(this::toReservationMap);
  }

  @Transactional
  public Map<String, Object> borrowBook(Map<String, Object> payload, AuthSession session) {
    Integer copyId = RequestUtils.intValue(payload, "copyId");
    Integer readerId = RequestUtils.intValue(payload, "readerId");
    LocalDate borrowDate = Optional.ofNullable(RequestUtils.dateValue(payload, "borrowDate")).orElse(LocalDate.now());
    if (copyId == null || readerId == null) {
      throw new IllegalArgumentException("copyId和readerId不能为空");
    }
    BookCopy copy = requireCopy(copyId);
    Book book = requireBook(copy.getBookId());
    ReaderProfile profile = requireReaderProfile(readerId);
    LibraryUser reader = requireUser(readerId);

    if (!"ACTIVE".equalsIgnoreCase(reader.getStatus()) || !"NORMAL".equalsIgnoreCase(profile.getCardStatus())) {
      throw new IllegalArgumentException("读者账号或借阅证状态异常");
    }
    if (Optional.ofNullable(profile.getFineBalance()).orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
      throw new IllegalArgumentException("读者存在未结清罚金，不能借书");
    }
    if (Optional.ofNullable(profile.getCurrentBorrowCount()).orElse(0) >= Optional.ofNullable(profile.getMaxBorrowCount()).orElse(0)) {
      throw new IllegalArgumentException("读者已达到借阅上限");
    }
    if (!List.of("AVAILABLE", "RESERVED").contains(copy.getStatus())) {
      throw new IllegalArgumentException("该馆藏当前不可借出");
    }

    Integer reservationId = null;
    if ("RESERVED".equalsIgnoreCase(copy.getStatus())) {
      Reservation reservation = reservationRepository
          .findFirstByBookIdAndReaderIdAndStatusInAndIsDeletedOrderByQueueNoAsc(book.getBookId(), readerId, List.of("READY"), 0)
          .orElseThrow(() -> new IllegalArgumentException("该副本已为其他预约读者保留"));
      reservation.setStatus("FULFILLED");
      reservation.setExpireDate(borrowDate);
      reservationRepository.save(reservation);
      reservationId = reservation.getReservationId();
    } else if (reservationRepository.existsByReaderIdAndBookIdAndStatusInAndIsDeleted(readerId, book.getBookId(), List.of("WAITING", "READY"), 0)) {
      Reservation reservation = reservationRepository
          .findFirstByBookIdAndReaderIdAndStatusInAndIsDeletedOrderByQueueNoAsc(book.getBookId(), readerId, List.of("WAITING", "READY"), 0)
          .orElse(null);
      if (reservation != null) {
        reservation.setStatus("FULFILLED");
        reservation.setExpireDate(borrowDate);
        reservationRepository.save(reservation);
        reservationId = reservation.getReservationId();
      }
    }

    BookCategory category = bookCategoryRepository.findById(requireId(book.getCategoryId(), "图书分类ID缺失"))
        .orElseThrow(() -> new IllegalArgumentException("图书分类不存在"));
    int resolvedBookId = requireId(book.getBookId(), "图书ID缺失");
    int operatorId = session == null ? readerId : requireId(session.getUserId(), "操作员ID缺失");
    BorrowRecord record = new BorrowRecord();
    record.setCopyId(copyId);
    record.setBookId(resolvedBookId);
    record.setReaderId(readerId);
    record.setOperatorId(operatorId);
    record.setStatus("BORROWED");
    record.setBorrowDate(borrowDate);
    record.setDueDate(borrowDate.plusDays(Optional.ofNullable(category.getLoanDays()).orElse(30)));
    record.setReturnDate(null);
    record.setRenewCount(0);
    record.setFineAmount(BigDecimal.ZERO);
    record.setReservationId(reservationId);
    record.setRemark(RequestUtils.stringValue(payload, "remark"));
    record.setIsDeleted(0);
    BorrowRecord savedRecord = borrowRecordRepository.save(record);

    copy.setStatus("BORROWED");
    bookCopyRepository.save(copy);
    profile.setCurrentBorrowCount(Optional.ofNullable(profile.getCurrentBorrowCount()).orElse(0) + 1);
    readerProfileRepository.save(profile);
    catalogService.refreshBookInventory(resolvedBookId);
    return toBorrowMap(savedRecord);
  }

  @Transactional
  public Map<String, Object> renewBorrow(Map<String, Object> payload, AuthSession session) {
    Integer recordId = RequestUtils.intValue(payload, "recordId");
    if (recordId == null) {
      throw new IllegalArgumentException("recordId不能为空");
    }
    BorrowRecord record = requireBorrowRecord(recordId);
    if (!"BORROWED".equalsIgnoreCase(record.getStatus())) {
      throw new IllegalArgumentException("当前记录不可续借");
    }
    if (session != null && session.getRole() == Role.READER && !session.getUserId().equals(record.getReaderId())) {
      throw new IllegalArgumentException("只能续借自己的借阅记录");
    }
    if (Optional.ofNullable(record.getRenewCount()).orElse(0) >= 2) {
      throw new IllegalArgumentException("每本书最多续借2次");
    }
    if (reservationRepository.existsByBookIdAndStatusInAndIsDeleted(record.getBookId(), List.of("READY"), 0)) {
      throw new IllegalArgumentException("该书已进入待取流程，不能续借");
    }
    List<Reservation> waitings = reservationRepository.findByBookIdAndStatusAndIsDeletedOrderByQueueNoAsc(record.getBookId(), "WAITING", 0);
    if (!waitings.isEmpty()) {
      throw new IllegalArgumentException("该书已有预约排队，不能续借");
    }

    Book book = requireBook(record.getBookId());
    BookCategory category = bookCategoryRepository.findById(requireId(book.getCategoryId(), "图书分类ID缺失"))
        .orElseThrow(() -> new IllegalArgumentException("图书分类不存在"));
    long extensionDays = Math.max(7, Optional.ofNullable(category.getLoanDays()).orElse(30) / 2);
    record.setDueDate(record.getDueDate().plusDays(extensionDays));
    record.setRenewCount(Optional.ofNullable(record.getRenewCount()).orElse(0) + 1);
    return toBorrowMap(borrowRecordRepository.save(record));
  }

  @Transactional
  public Map<String, Object> returnBook(Map<String, Object> payload) {
    Integer recordId = RequestUtils.intValue(payload, "recordId");
    LocalDate returnDate = Optional.ofNullable(RequestUtils.dateValue(payload, "returnDate")).orElse(LocalDate.now());
    if (recordId == null) {
      throw new IllegalArgumentException("recordId不能为空");
    }
    BorrowRecord record = requireBorrowRecord(recordId);
    if (!ACTIVE_BORROW_STATUSES.contains(record.getStatus())) {
      throw new IllegalArgumentException("当前记录不可归还");
    }

    BookCopy copy = requireCopy(record.getCopyId());
    ReaderProfile profile = requireReaderProfile(record.getReaderId());
    long overdueDays = Math.max(0, ChronoUnit.DAYS.between(record.getDueDate(), returnDate));
    BigDecimal fine = overdueDays == 0 ? BigDecimal.ZERO : DAILY_FINE.multiply(BigDecimal.valueOf(overdueDays));

    record.setStatus("RETURNED");
    record.setReturnDate(returnDate);
    record.setFineAmount(fine);
    borrowRecordRepository.save(record);

    profile.setCurrentBorrowCount(Math.max(0, Optional.ofNullable(profile.getCurrentBorrowCount()).orElse(0) - 1));
    if (fine.compareTo(BigDecimal.ZERO) > 0) {
      profile.setFineBalance(Optional.ofNullable(profile.getFineBalance()).orElse(BigDecimal.ZERO).add(fine));
    }
    readerProfileRepository.save(profile);

    Reservation nextReservation = reservationRepository.findByBookIdAndStatusAndIsDeletedOrderByQueueNoAsc(record.getBookId(), "WAITING", 0)
        .stream()
        .findFirst()
        .orElse(null);
    if (nextReservation != null) {
      nextReservation.setStatus("READY");
      nextReservation.setPickupDeadline(returnDate.plusDays(2));
      reservationRepository.save(nextReservation);
      copy.setStatus("RESERVED");
    } else {
      copy.setStatus("AVAILABLE");
    }
    bookCopyRepository.save(copy);
    catalogService.refreshBookInventory(record.getBookId());
    return toBorrowMap(record);
  }

  @Transactional
  public Map<String, Object> reserveBook(Map<String, Object> payload, AuthSession session) {
    Integer bookId = RequestUtils.intValue(payload, "bookId");
    if (bookId == null) {
      throw new IllegalArgumentException("bookId不能为空");
    }
    Integer readerId = resolveReaderId(payload, session);
    Book book = requireBook(bookId);
    ReaderProfile profile = requireReaderProfile(readerId);
    if (!"ON_SHELF".equalsIgnoreCase(book.getShelfStatus())) {
      throw new IllegalArgumentException("该图书当前不可预约");
    }
    if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
      throw new IllegalArgumentException("当前仍有可借副本，无需预约");
    }
    if (!"NORMAL".equalsIgnoreCase(profile.getCardStatus())) {
      throw new IllegalArgumentException("当前借阅证状态不可预约");
    }
    if (borrowRecordRepository.existsByReaderIdAndBookIdAndStatusInAndIsDeleted(readerId, bookId, ACTIVE_BORROW_STATUSES, 0)) {
      throw new IllegalArgumentException("您已借阅该图书，无需重复预约");
    }
    if (reservationRepository.existsByReaderIdAndBookIdAndStatusInAndIsDeleted(readerId, bookId, ACTIVE_RESERVATION_STATUSES, 0)) {
      throw new IllegalArgumentException("您已存在有效预约记录");
    }
    Reservation reservation = new Reservation();
    reservation.setBookId(bookId);
    reservation.setReaderId(readerId);
    int queueNo = Optional.ofNullable(reservationRepository.findMaxQueueNo(bookId)).orElse(0) + 1;
    reservation.setQueueNo(queueNo);
    reservation.setStatus("WAITING");
    reservation.setReserveDate(LocalDate.now());
    reservation.setPickupDeadline(null);
    reservation.setExpireDate(null);
    reservation.setNote(RequestUtils.stringValue(payload, "note"));
    reservation.setIsDeleted(0);
    return toReservationMap(reservationRepository.save(reservation));
  }

  @Transactional
  public Map<String, Object> cancelReservation(Map<String, Object> payload, AuthSession session) {
    Integer reservationId = RequestUtils.intValue(payload, "reservationId");
    if (reservationId == null) {
      throw new IllegalArgumentException("reservationId不能为空");
    }
    Reservation reservation = requireReservation(reservationId);
    if (session != null && session.getRole() == Role.READER && !session.getUserId().equals(reservation.getReaderId())) {
      throw new IllegalArgumentException("只能取消自己的预约");
    }
    if (!ACTIVE_RESERVATION_STATUSES.contains(reservation.getStatus())) {
      throw new IllegalArgumentException("当前预约状态不可取消");
    }
    boolean wasReady = "READY".equalsIgnoreCase(reservation.getStatus());
    reservation.setStatus("CANCELED");
    reservation.setExpireDate(LocalDate.now());
    Reservation savedReservation = reservationRepository.save(reservation);
    if (wasReady) {
      BookCopy reservedCopy = bookCopyRepository.findFirstByBookIdAndStatusAndIsDeleted(savedReservation.getBookId(), "RESERVED", 0)
          .orElse(null);
      if (reservedCopy != null) {
        Reservation nextWaiting = reservationRepository.findByBookIdAndStatusAndIsDeletedOrderByQueueNoAsc(savedReservation.getBookId(), "WAITING", 0)
            .stream()
            .findFirst()
            .orElse(null);
        if (nextWaiting != null) {
          nextWaiting.setStatus("READY");
          nextWaiting.setPickupDeadline(LocalDate.now().plusDays(2));
          reservationRepository.save(nextWaiting);
        } else {
          reservedCopy.setStatus("AVAILABLE");
          bookCopyRepository.save(reservedCopy);
          catalogService.refreshBookInventory(savedReservation.getBookId());
        }
      }
    }
    return toReservationMap(savedReservation);
  }

  @Transactional
  public void refreshOverdueRecords() {
    List<BorrowRecord> records = borrowRecordRepository.findByStatusInAndIsDeleted(List.of("BORROWED"), 0);
    LocalDate today = LocalDate.now();
    for (BorrowRecord record : records) {
      if (record.getDueDate() != null && record.getDueDate().isBefore(today)) {
        record.setStatus("OVERDUE");
        borrowRecordRepository.save(record);
      }
    }
  }

  private Integer resolveReaderId(Map<String, Object> payload, AuthSession session) {
    if (session != null && session.getRole() == Role.READER) {
      return session.getUserId();
    }
    Integer readerId = RequestUtils.intValue(payload, "readerId");
    if (readerId == null) {
      throw new IllegalArgumentException("readerId不能为空");
    }
    return readerId;
  }

  private Map<String, Object> toBorrowMap(BorrowRecord record) {
    Map<String, Object> map = new HashMap<>();
    map.put("recordId", record.getRecordId());
    map.put("copyId", record.getCopyId());
    map.put("bookId", record.getBookId());
    map.put("readerId", record.getReaderId());
    map.put("operatorId", record.getOperatorId());
    map.put("status", record.getStatus());
    map.put("borrowDate", record.getBorrowDate());
    map.put("dueDate", record.getDueDate());
    map.put("returnDate", record.getReturnDate());
    map.put("renewCount", record.getRenewCount());
    map.put("fineAmount", record.getFineAmount());
    map.put("remark", record.getRemark());
    Integer bookId = record.getBookId();
    if (bookId != null) {
      bookRepository.findById(bookId).ifPresent(book -> {
        map.put("isbn", book.getIsbn());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
      });
    }
    Integer copyId = record.getCopyId();
    if (copyId != null) {
      bookCopyRepository.findById(copyId).ifPresent(copy -> {
        map.put("barcode", copy.getBarcode());
        map.put("locationCode", copy.getLocationCode());
      });
    }
    Integer readerId = record.getReaderId();
    if (readerId != null) {
      libraryUserRepository.findById(readerId).ifPresent(reader -> {
        map.put("readerName", reader.getName());
        map.put("readerAccount", reader.getAccount());
      });
    }
    return map;
  }

  private Map<String, Object> toReservationMap(Reservation reservation) {
    Map<String, Object> map = new HashMap<>();
    map.put("reservationId", reservation.getReservationId());
    map.put("bookId", reservation.getBookId());
    map.put("readerId", reservation.getReaderId());
    map.put("queueNo", reservation.getQueueNo());
    map.put("status", reservation.getStatus());
    map.put("reserveDate", reservation.getReserveDate());
    map.put("pickupDeadline", reservation.getPickupDeadline());
    map.put("expireDate", reservation.getExpireDate());
    map.put("note", reservation.getNote());
    Integer reservationBookId = reservation.getBookId();
    if (reservationBookId != null) {
      bookRepository.findById(reservationBookId).ifPresent(book -> {
        map.put("isbn", book.getIsbn());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
      });
    }
    Integer readerId = reservation.getReaderId();
    if (readerId != null) {
      libraryUserRepository.findById(readerId).ifPresent(reader -> {
        map.put("readerName", reader.getName());
        map.put("readerAccount", reader.getAccount());
      });
    }
    return map;
  }

  private LibraryUser requireUser(Integer userId) {
    return libraryUserRepository.findById(requireId(userId, "userId不能为空"))
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
  }

  private ReaderProfile requireReaderProfile(Integer userId) {
    return readerProfileRepository.findById(requireId(userId, "userId不能为空"))
        .orElseThrow(() -> new IllegalArgumentException("读者档案不存在"));
  }

  private Book requireBook(Integer bookId) {
    return bookRepository.findById(requireId(bookId, "bookId不能为空"))
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("图书不存在"));
  }

  private BookCopy requireCopy(Integer copyId) {
    return bookCopyRepository.findById(requireId(copyId, "copyId不能为空"))
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("馆藏副本不存在"));
  }

  private BorrowRecord requireBorrowRecord(Integer recordId) {
    return borrowRecordRepository.findById(requireId(recordId, "recordId不能为空"))
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("借阅记录不存在"));
  }

  private Reservation requireReservation(Integer reservationId) {
    return reservationRepository.findById(requireId(reservationId, "reservationId不能为空"))
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("预约记录不存在"));
  }

  private int requireId(Integer value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private boolean contains(String value, String keyword) {
    return value != null && keyword != null && value.toLowerCase().contains(keyword.trim().toLowerCase());
  }
}
