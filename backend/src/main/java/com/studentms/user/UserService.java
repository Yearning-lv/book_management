package com.studentms.user;

import com.studentms.common.RequestUtils;
import com.studentms.security.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
  private final LibraryUserRepository libraryUserRepository;
  private final ReaderProfileRepository readerProfileRepository;

  public UserService(LibraryUserRepository libraryUserRepository, ReaderProfileRepository readerProfileRepository) {
    this.libraryUserRepository = libraryUserRepository;
    this.readerProfileRepository = readerProfileRepository;
  }

  public Page<Map<String, Object>> getUserList(String keyword, String roleCode, int page, int pageSize) {
    Specification<LibraryUser> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("isDeleted"), 0));
      if (keyword != null && !keyword.isBlank()) {
        String likeValue = "%" + keyword.trim() + "%";
        predicates.add(cb.or(
            cb.like(root.get("account"), likeValue),
            cb.like(root.get("name"), likeValue),
            cb.like(root.get("phone"), likeValue)
        ));
      }
      if (roleCode != null && !roleCode.isBlank()) {
        predicates.add(cb.equal(root.get("roleCode"), roleCode.trim()));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };

    return libraryUserRepository.findAll(
        specification,
        PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "userId"))
    ).map(this::toUserMap);
  }

  public Map<String, Object> getUserInfo(Integer userId) {
    LibraryUser user = libraryUserRepository.findById(requireId(userId, "userId不能为空"))
        .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    return toUserMap(user);
  }

  @Transactional
  public Map<String, Object> saveUser(Map<String, Object> payload) {
    if (payload == null) {
      throw new IllegalArgumentException("提交数据不能为空");
    }
    Integer userId = RequestUtils.intValue(payload, "userId");
    String account = requireText(payload, "account", "账号不能为空");
    String name = requireText(payload, "name", "姓名不能为空");
    String roleCode = normalizeRole(payload);

    LibraryUser user = userId == null
        ? new LibraryUser()
        : libraryUserRepository.findById(requireId(userId, "userId不能为空")).orElseThrow(() -> new IllegalArgumentException("用户不存在"));

    if (userId == null) {
      if (libraryUserRepository.existsByAccountAndIsDeleted(account, 0)) {
        throw new IllegalArgumentException("账号已存在");
      }
      user.setStatus("ACTIVE");
      user.setIsDeleted(0);
      user.setPassword(defaultPassword(payload));
    } else if (libraryUserRepository.existsByAccountAndUserIdNotAndIsDeleted(account, requireId(userId, "userId不能为空"), 0)) {
      throw new IllegalArgumentException("账号已存在");
    } else if (RequestUtils.stringValue(payload, "password") != null) {
      user.setPassword(RequestUtils.stringValue(payload, "password"));
    }

    user.setAccount(account);
    user.setName(name);
    user.setRoleCode(roleCode);
    user.setGender(RequestUtils.stringValue(payload, "gender"));
    user.setPhone(RequestUtils.stringValue(payload, "phone"));
    user.setEmail(RequestUtils.stringValue(payload, "email"));
    user.setDepartment(RequestUtils.stringValue(payload, "department"));
    user.setCardNo(RequestUtils.stringValue(payload, "cardNo"));
    user.setStatus(Optional.ofNullable(RequestUtils.stringValue(payload, "status")).orElse("ACTIVE"));

    LibraryUser savedUser = libraryUserRepository.save(user);
    if (Role.READER.getCode().equals(roleCode)) {
      ensureReaderProfile(savedUser, payload);
    }
    return getUserInfo(requireId(savedUser.getUserId(), "保存后的用户ID为空"));
  }

  @Transactional
  public void deleteUser(Integer userId) {
    LibraryUser user = libraryUserRepository.findById(requireId(userId, "userId不能为空"))
        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    user.setIsDeleted(1);
    libraryUserRepository.save(user);
  }

  @Transactional
  public Map<String, Object> payFine(Integer userId, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("缴费金额必须大于0");
    }
    int resolvedUserId = requireId(userId, "userId不能为空");
    ReaderProfile profile = readerProfileRepository.findById(resolvedUserId)
        .orElseThrow(() -> new IllegalArgumentException("读者档案不存在"));
    BigDecimal balance = Optional.ofNullable(profile.getFineBalance()).orElse(BigDecimal.ZERO);
    if (amount.compareTo(balance) > 0) {
      throw new IllegalArgumentException("缴费金额不能超过欠费余额");
    }
    profile.setFineBalance(balance.subtract(amount));
    readerProfileRepository.save(profile);
    return getUserInfo(resolvedUserId);
  }

  private void ensureReaderProfile(LibraryUser user, Map<String, Object> payload) {
    int userId = requireId(user.getUserId(), "读者用户ID为空");
    ReaderProfile profile = readerProfileRepository.findById(userId).orElseGet(ReaderProfile::new);
    profile.setUserId(userId);
    profile.setReaderNo(Optional.ofNullable(RequestUtils.stringValue(payload, "readerNo"))
        .orElse("R" + String.format("%05d", userId)));
    profile.setMaxBorrowCount(Optional.ofNullable(RequestUtils.intValue(payload, "maxBorrowCount")).orElse(8));
    profile.setCurrentBorrowCount(Optional.ofNullable(profile.getCurrentBorrowCount()).orElse(0));
    profile.setFineBalance(Optional.ofNullable(profile.getFineBalance()).orElse(BigDecimal.ZERO));
    profile.setCardStatus(Optional.ofNullable(RequestUtils.stringValue(payload, "cardStatus")).orElse("NORMAL"));
    profile.setGrade(RequestUtils.stringValue(payload, "grade"));
    profile.setNote(RequestUtils.stringValue(payload, "note"));
    readerProfileRepository.save(profile);
  }

  private Map<String, Object> toUserMap(LibraryUser user) {
    Map<String, Object> map = new HashMap<>();
    map.put("userId", user.getUserId());
    map.put("account", user.getAccount());
    map.put("name", user.getName());
    map.put("roleCode", user.getRoleCode());
    Role role = Role.fromValue(user.getRoleCode());
    map.put("roleName", role == null ? user.getRoleCode() : role.getLabel());
    map.put("gender", user.getGender());
    map.put("phone", user.getPhone());
    map.put("email", user.getEmail());
    map.put("department", user.getDepartment());
    map.put("cardNo", user.getCardNo());
    map.put("status", user.getStatus());

    Integer userId = user.getUserId();
    if (userId != null) {
      readerProfileRepository.findById(userId).ifPresent(profile -> {
        map.put("readerNo", profile.getReaderNo());
        map.put("maxBorrowCount", profile.getMaxBorrowCount());
        map.put("currentBorrowCount", profile.getCurrentBorrowCount());
        map.put("fineBalance", profile.getFineBalance());
        map.put("cardStatus", profile.getCardStatus());
        map.put("grade", profile.getGrade());
        map.put("note", profile.getNote());
      });
    }
    return map;
  }

  private String normalizeRole(Map<String, Object> payload) {
    String roleCode = requireText(payload, "roleCode", "角色不能为空");
    Role role = Role.fromValue(roleCode);
    if (role == null) {
      throw new IllegalArgumentException("角色不合法");
    }
    return role.getCode();
  }

  private String defaultPassword(Map<String, Object> payload) {
    String password = RequestUtils.stringValue(payload, "password");
    return password == null ? "123456" : password;
  }

  private String requireText(Map<String, Object> payload, String key, String message) {
    String value = RequestUtils.stringValue(payload, key);
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private int requireId(Integer value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
