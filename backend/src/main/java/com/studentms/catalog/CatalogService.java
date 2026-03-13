package com.studentms.catalog;

import com.studentms.common.RequestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CatalogService {
  private static final List<String> AVAILABLE_COPY_STATES = List.of("AVAILABLE");

  private final BookRepository bookRepository;
  private final BookCopyRepository bookCopyRepository;
  private final BookCategoryRepository bookCategoryRepository;
  private final PublisherRepository publisherRepository;

  public CatalogService(BookRepository bookRepository,
                        BookCopyRepository bookCopyRepository,
                        BookCategoryRepository bookCategoryRepository,
                        PublisherRepository publisherRepository) {
    this.bookRepository = bookRepository;
    this.bookCopyRepository = bookCopyRepository;
    this.bookCategoryRepository = bookCategoryRepository;
    this.publisherRepository = publisherRepository;
  }

  public Page<Map<String, Object>> getBookList(String keyword, Integer categoryId, String shelfStatus, int page, int pageSize) {
    Specification<Book> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("isDeleted"), 0));
      if (keyword != null && !keyword.isBlank()) {
        String likeValue = "%" + keyword.trim() + "%";
        predicates.add(cb.or(
            cb.like(root.get("isbn"), likeValue),
            cb.like(root.get("title"), likeValue),
            cb.like(root.get("author"), likeValue),
            cb.like(root.get("keywords"), likeValue)
        ));
      }
      if (categoryId != null) {
        predicates.add(cb.equal(root.get("categoryId"), categoryId));
      }
      if (shelfStatus != null && !shelfStatus.isBlank()) {
        predicates.add(cb.equal(root.get("shelfStatus"), shelfStatus));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
    return bookRepository.findAll(
        specification,
        PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "bookId"))
    ).map(this::toBookMap);
  }

  public Map<String, Object> getBookInfo(Integer bookId) {
    Book book = requireBook(bookId);
    return toBookMap(book);
  }

  @Transactional
  public Map<String, Object> saveBook(Map<String, Object> payload) {
    if (payload == null) {
      throw new IllegalArgumentException("图书数据不能为空");
    }
    Integer bookId = RequestUtils.intValue(payload, "bookId");
    String isbn = requireText(payload, "isbn", "ISBN不能为空");
    String title = requireText(payload, "title", "书名不能为空");
    String author = requireText(payload, "author", "作者不能为空");
    Integer categoryId = RequestUtils.intValue(payload, "categoryId");
    if (categoryId == null) {
      throw new IllegalArgumentException("分类不能为空");
    }
    int resolvedCategoryId = categoryId;
    bookCategoryRepository.findById(resolvedCategoryId).orElseThrow(() -> new IllegalArgumentException("分类不存在"));

    Book book = bookId == null ? new Book() : requireBook(bookId);
    if (bookId == null) {
      if (bookRepository.existsByIsbnAndIsDeleted(isbn, 0)) {
        throw new IllegalArgumentException("ISBN已存在");
      }
      book.setIsDeleted(0);
      book.setTotalCopies(0);
      book.setAvailableCopies(0);
    } else if (bookRepository.existsByIsbnAndBookIdNotAndIsDeleted(isbn, bookId, 0)) {
      throw new IllegalArgumentException("ISBN已存在");
    }

    book.setIsbn(isbn);
    book.setTitle(title);
    book.setAuthor(author);
    book.setCategoryId(resolvedCategoryId);
    book.setPublisherId(RequestUtils.intValue(payload, "publisherId"));
    book.setPublishDate(RequestUtils.dateValue(payload, "publishDate"));
    book.setPrice(RequestUtils.decimalValue(payload, "price"));
    book.setKeywords(RequestUtils.stringValue(payload, "keywords"));
    book.setSummary(RequestUtils.stringValue(payload, "summary"));
    book.setShelfStatus(Optional.ofNullable(RequestUtils.stringValue(payload, "shelfStatus")).orElse("ON_SHELF"));

    return toBookMap(bookRepository.save(book));
  }

  @Transactional
  public void deleteBook(Integer bookId) {
    Book book = requireBook(bookId);
    int resolvedBookId = requireId(bookId, "bookId不能为空");
    long copyCount = bookCopyRepository.countByBookIdAndIsDeleted(resolvedBookId, 0);
    if (copyCount > 0) {
      throw new IllegalArgumentException("该图书仍有关联馆藏副本，不能删除");
    }
    book.setIsDeleted(1);
    bookRepository.save(book);
  }

  public Page<Map<String, Object>> getCopyList(String keyword, String status, int page, int pageSize) {
    Specification<BookCopy> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("isDeleted"), 0));
      if (keyword != null && !keyword.isBlank()) {
        String likeValue = "%" + keyword.trim() + "%";
        predicates.add(cb.or(
            cb.like(root.get("barcode"), likeValue),
            cb.like(root.get("locationCode"), likeValue)
        ));
      }
      if (status != null && !status.isBlank()) {
        predicates.add(cb.equal(root.get("status"), status));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
    return bookCopyRepository.findAll(
        specification,
        PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "copyId"))
    ).map(this::toCopyMap);
  }

  public Map<String, Object> getCopyInfo(Integer copyId) {
    return toCopyMap(requireCopy(copyId));
  }

  @Transactional
  public Map<String, Object> saveCopy(Map<String, Object> payload) {
    if (payload == null) {
      throw new IllegalArgumentException("馆藏数据不能为空");
    }
    Integer copyId = RequestUtils.intValue(payload, "copyId");
    Integer bookId = RequestUtils.intValue(payload, "bookId");
    String barcode = requireText(payload, "barcode", "条码不能为空");
    if (bookId == null) {
      throw new IllegalArgumentException("图书不能为空");
    }
    int resolvedBookId = requireId(bookId, "bookId不能为空");
    requireBook(resolvedBookId);

    BookCopy copy = copyId == null ? new BookCopy() : requireCopy(copyId);
    if (copyId == null) {
      if (bookCopyRepository.existsByBarcodeAndIsDeleted(barcode, 0)) {
        throw new IllegalArgumentException("条码已存在");
      }
      copy.setIsDeleted(0);
    } else if (bookCopyRepository.existsByBarcodeAndCopyIdNotAndIsDeleted(barcode, copyId, 0)) {
      throw new IllegalArgumentException("条码已存在");
    }

    copy.setBookId(resolvedBookId);
    copy.setBarcode(barcode);
    copy.setLocationCode(RequestUtils.stringValue(payload, "locationCode"));
    copy.setConditionLevel(Optional.ofNullable(RequestUtils.stringValue(payload, "conditionLevel")).orElse("GOOD"));
    copy.setStatus(Optional.ofNullable(RequestUtils.stringValue(payload, "status")).orElse("AVAILABLE"));
    copy.setPurchaseDate(RequestUtils.dateValue(payload, "purchaseDate"));
    copy.setLastInventoryDate(RequestUtils.dateValue(payload, "lastInventoryDate"));

    BookCopy savedCopy = bookCopyRepository.save(copy);
    refreshBookInventory(resolvedBookId);
    return toCopyMap(savedCopy);
  }

  @Transactional
  public void deleteCopy(Integer copyId) {
    BookCopy copy = requireCopy(copyId);
    if ("BORROWED".equalsIgnoreCase(copy.getStatus()) || "RESERVED".equalsIgnoreCase(copy.getStatus())) {
      throw new IllegalArgumentException("借出或预约中的副本不能删除");
    }
    copy.setIsDeleted(1);
    bookCopyRepository.save(copy);
    refreshBookInventory(requireId(copy.getBookId(), "馆藏副本缺少图书ID"));
  }

  public List<Map<String, Object>> getCategoryOptions() {
    return bookCategoryRepository.findByIsDeletedOrderByCategoryCodeAsc(0).stream().map(category -> {
      Map<String, Object> item = new HashMap<>();
      item.put("categoryId", category.getCategoryId());
      item.put("categoryCode", category.getCategoryCode());
      item.put("categoryName", category.getCategoryName());
      item.put("loanDays", category.getLoanDays());
      item.put("locationCode", category.getLocationCode());
      return item;
    }).toList();
  }

  public List<Map<String, Object>> getPublisherOptions() {
    return publisherRepository.findByIsDeletedOrderByPublisherNameAsc(0).stream().map(publisher -> {
      Map<String, Object> item = new HashMap<>();
      item.put("publisherId", publisher.getPublisherId());
      item.put("publisherName", publisher.getPublisherName());
      return item;
    }).toList();
  }

  public void refreshBookInventory(Integer bookId) {
    int resolvedBookId = requireId(bookId, "bookId不能为空");
    Book book = requireBook(resolvedBookId);
    book.setTotalCopies((int) bookCopyRepository.countByBookIdAndIsDeleted(resolvedBookId, 0));
    book.setAvailableCopies((int) bookCopyRepository.countByBookIdAndStatusInAndIsDeleted(resolvedBookId, AVAILABLE_COPY_STATES, 0));
    bookRepository.save(book);
  }

  private Map<String, Object> toBookMap(Book book) {
    Map<String, Object> map = new HashMap<>();
    map.put("bookId", book.getBookId());
    map.put("isbn", book.getIsbn());
    map.put("title", book.getTitle());
    map.put("author", book.getAuthor());
    map.put("categoryId", book.getCategoryId());
    map.put("publisherId", book.getPublisherId());
    map.put("publishDate", book.getPublishDate());
    map.put("price", book.getPrice());
    map.put("keywords", book.getKeywords());
    map.put("summary", book.getSummary());
    map.put("shelfStatus", book.getShelfStatus());
    map.put("totalCopies", book.getTotalCopies());
    map.put("availableCopies", book.getAvailableCopies());
    Integer categoryId = book.getCategoryId();
    if (categoryId != null) {
      bookCategoryRepository.findById(categoryId).ifPresent(category -> {
        map.put("categoryCode", category.getCategoryCode());
        map.put("categoryName", category.getCategoryName());
        map.put("loanDays", category.getLoanDays());
      });
    }
    Integer publisherId = book.getPublisherId();
    if (publisherId != null) {
      publisherRepository.findById(publisherId).ifPresent(publisher -> map.put("publisherName", publisher.getPublisherName()));
    }
    return map;
  }

  @SuppressWarnings("null")
  private Map<String, Object> toCopyMap(BookCopy copy) {
    Map<String, Object> map = new HashMap<>();
    map.put("copyId", copy.getCopyId());
    map.put("barcode", copy.getBarcode());
    map.put("locationCode", copy.getLocationCode());
    map.put("conditionLevel", copy.getConditionLevel());
    map.put("status", copy.getStatus());
    map.put("purchaseDate", copy.getPurchaseDate());
    map.put("lastInventoryDate", copy.getLastInventoryDate());
    int resolvedBookId = requireId(copy.getBookId(), "馆藏副本缺少图书ID");
    map.put("bookId", resolvedBookId);
    bookRepository.findById(resolvedBookId).ifPresent(book -> {
      map.put("isbn", book.getIsbn());
      map.put("title", book.getTitle());
      map.put("author", book.getAuthor());
      map.put("bookStatus", book.getShelfStatus());
    });
    return map;
  }

  @SuppressWarnings("null")
  private Book requireBook(Integer bookId) {
    int resolvedBookId = requireId(bookId, "bookId不能为空");
    return bookRepository.findById(resolvedBookId)
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("图书不存在"));
  }

  @SuppressWarnings("null")
  private BookCopy requireCopy(Integer copyId) {
    int resolvedCopyId = requireId(copyId, "copyId不能为空");
    return bookCopyRepository.findById(resolvedCopyId)
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("馆藏副本不存在"));
  }

  private int requireId(Integer value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private String requireText(Map<String, Object> payload, String key, String message) {
    String value = RequestUtils.stringValue(payload, key);
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
