CREATE DATABASE IF NOT EXISTS `library_management`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `library_management`;

DROP TABLE IF EXISTS `borrow_record`;
DROP TABLE IF EXISTS `reservation`;
DROP TABLE IF EXISTS `book_copy`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `publisher`;
DROP TABLE IF EXISTS `book_category`;
DROP TABLE IF EXISTS `reader_profile`;
DROP TABLE IF EXISTS `lib_user`;

CREATE TABLE `lib_user` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `role_code` VARCHAR(20) NOT NULL COMMENT '0-管理员 1-工作人员 2-读者',
  `account` VARCHAR(30) NOT NULL,
  `password` VARCHAR(64) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `gender` VARCHAR(10) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(80) DEFAULT NULL,
  `department` VARCHAR(80) DEFAULT NULL,
  `card_no` VARCHAR(30) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_lib_user_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一账户表';

CREATE TABLE `reader_profile` (
  `user_id` INT NOT NULL,
  `reader_no` VARCHAR(30) NOT NULL,
  `max_borrow_count` INT NOT NULL DEFAULT 8,
  `current_borrow_count` INT NOT NULL DEFAULT 0,
  `fine_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `card_status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  `grade` VARCHAR(30) DEFAULT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_reader_profile_reader_no` (`reader_no`),
  CONSTRAINT `fk_reader_profile_user` FOREIGN KEY (`user_id`) REFERENCES `lib_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='读者扩展档案';

CREATE TABLE `book_category` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `category_code` VARCHAR(30) NOT NULL,
  `category_name` VARCHAR(80) NOT NULL,
  `loan_days` INT NOT NULL DEFAULT 30,
  `location_code` VARCHAR(30) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_book_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类';

CREATE TABLE `publisher` (
  `publisher_id` INT NOT NULL AUTO_INCREMENT,
  `publisher_name` VARCHAR(100) NOT NULL,
  `contact_name` VARCHAR(50) DEFAULT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`publisher_id`),
  UNIQUE KEY `uk_publisher_name` (`publisher_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出版社信息';

CREATE TABLE `book` (
  `book_id` INT NOT NULL AUTO_INCREMENT,
  `isbn` VARCHAR(20) NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `author` VARCHAR(120) NOT NULL,
  `category_id` INT NOT NULL,
  `publisher_id` INT DEFAULT NULL,
  `publish_date` DATE DEFAULT NULL,
  `price` DECIMAL(10,2) DEFAULT NULL,
  `keywords` VARCHAR(200) DEFAULT NULL,
  `summary` VARCHAR(500) DEFAULT NULL,
  `shelf_status` VARCHAR(20) NOT NULL DEFAULT 'ON_SHELF',
  `total_copies` INT NOT NULL DEFAULT 0,
  `available_copies` INT NOT NULL DEFAULT 0,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uk_book_isbn` (`isbn`),
  KEY `idx_book_category_id` (`category_id`),
  KEY `idx_book_publisher_id` (`publisher_id`),
  CONSTRAINT `fk_book_category` FOREIGN KEY (`category_id`) REFERENCES `book_category` (`category_id`),
  CONSTRAINT `fk_book_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `publisher` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书目录';

CREATE TABLE `book_copy` (
  `copy_id` INT NOT NULL AUTO_INCREMENT,
  `book_id` INT NOT NULL,
  `barcode` VARCHAR(40) NOT NULL,
  `location_code` VARCHAR(40) DEFAULT NULL,
  `condition_level` VARCHAR(20) NOT NULL DEFAULT 'GOOD',
  `status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  `purchase_date` DATE DEFAULT NULL,
  `last_inventory_date` DATE DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`copy_id`),
  UNIQUE KEY `uk_book_copy_barcode` (`barcode`),
  KEY `idx_book_copy_book_id` (`book_id`),
  CONSTRAINT `fk_book_copy_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书实体副本';

CREATE TABLE `reservation` (
  `reservation_id` INT NOT NULL AUTO_INCREMENT,
  `book_id` INT NOT NULL,
  `reader_id` INT NOT NULL,
  `queue_no` INT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'WAITING',
  `reserve_date` DATE NOT NULL,
  `pickup_deadline` DATE DEFAULT NULL,
  `expire_date` DATE DEFAULT NULL,
  `note` VARCHAR(255) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`reservation_id`),
  KEY `idx_reservation_book_id` (`book_id`),
  KEY `idx_reservation_reader_id` (`reader_id`),
  CONSTRAINT `fk_reservation_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
  CONSTRAINT `fk_reservation_reader` FOREIGN KEY (`reader_id`) REFERENCES `lib_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约排队记录';

CREATE TABLE `borrow_record` (
  `record_id` INT NOT NULL AUTO_INCREMENT,
  `copy_id` INT NOT NULL,
  `book_id` INT NOT NULL,
  `reader_id` INT NOT NULL,
  `operator_id` INT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
  `borrow_date` DATE NOT NULL,
  `due_date` DATE NOT NULL,
  `return_date` DATE DEFAULT NULL,
  `renew_count` INT NOT NULL DEFAULT 0,
  `fine_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `reservation_id` INT DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`record_id`),
  KEY `idx_borrow_copy_id` (`copy_id`),
  KEY `idx_borrow_book_id` (`book_id`),
  KEY `idx_borrow_reader_id` (`reader_id`),
  KEY `idx_borrow_operator_id` (`operator_id`),
  CONSTRAINT `fk_borrow_copy` FOREIGN KEY (`copy_id`) REFERENCES `book_copy` (`copy_id`),
  CONSTRAINT `fk_borrow_book` FOREIGN KEY (`book_id`) REFERENCES `book` (`book_id`),
  CONSTRAINT `fk_borrow_reader` FOREIGN KEY (`reader_id`) REFERENCES `lib_user` (`user_id`),
  CONSTRAINT `fk_borrow_operator` FOREIGN KEY (`operator_id`) REFERENCES `lib_user` (`user_id`),
  CONSTRAINT `fk_borrow_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅流转记录';
