USE `library_management`;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `borrow_record`;
DELETE FROM `reservation`;
DELETE FROM `book_copy`;
DELETE FROM `book`;
DELETE FROM `publisher`;
DELETE FROM `book_category`;
DELETE FROM `reader_profile`;
DELETE FROM `lib_user`;

ALTER TABLE `lib_user` AUTO_INCREMENT = 1;
ALTER TABLE `book_category` AUTO_INCREMENT = 1;
ALTER TABLE `publisher` AUTO_INCREMENT = 1;
ALTER TABLE `book` AUTO_INCREMENT = 1;
ALTER TABLE `book_copy` AUTO_INCREMENT = 1;
ALTER TABLE `reservation` AUTO_INCREMENT = 1;
ALTER TABLE `borrow_record` AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

SET @today = CURRENT_DATE();

INSERT INTO `lib_user`
(`user_id`, `role_code`, `account`, `password`, `name`, `gender`, `phone`, `email`, `department`, `card_no`, `status`, `is_deleted`)
VALUES
(1, '0', 'admin', '123456', '系统管理员', '男', '13800000001', 'admin@library.local', '图书馆信息中心', 'CARD-ADMIN-01', 'ACTIVE', 0),
(2, '1', 'staff01', '123456', '流通管理员', '女', '13800000002', 'staff01@library.local', '流通服务部', 'CARD-STAFF-01', 'ACTIVE', 0),
(3, '1', 'staff02', '123456', '馆藏专员', '男', '13800000012', 'staff02@library.local', '馆藏编目部', 'CARD-STAFF-02', 'ACTIVE', 0),
(4, '2', 'reader01', '123456', '张晨', '男', '13800000003', 'reader01@library.local', '软件学院', 'CARD-READER-01', 'ACTIVE', 0),
(5, '2', 'reader02', '123456', '李悦', '女', '13800000004', 'reader02@library.local', '数据科学学院', 'CARD-READER-02', 'ACTIVE', 0),
(6, '2', 'reader03', '123456', '王可', '男', '13800000005', 'reader03@library.local', '计算机学院', 'CARD-READER-03', 'ACTIVE', 0),
(7, '2', 'reader04', '123456', '陈思颖', '女', '13800000006', 'reader04@library.local', '人工智能学院', 'CARD-READER-04', 'ACTIVE', 0),
(8, '2', 'reader05', '123456', '周岚', '女', '13800000007', 'reader05@library.local', '经济管理学院', 'CARD-READER-05', 'ACTIVE', 0),
(9, '2', 'reader06', '123456', '赵宁', '男', '13800000008', 'reader06@library.local', '网络空间安全学院', 'CARD-READER-06', 'ACTIVE', 0),
(10, '2', 'reader07', '123456', '孙哲', '男', '13800000009', 'reader07@library.local', '新媒体学院', 'CARD-READER-07', 'ACTIVE', 0),
(11, '2', 'reader08', '123456', '刘婷', '女', '13800000010', 'reader08@library.local', '计算机学院', 'CARD-READER-08', 'ACTIVE', 0);

INSERT INTO `reader_profile`
(`user_id`, `reader_no`, `max_borrow_count`, `current_borrow_count`, `fine_balance`, `card_status`, `grade`, `note`)
VALUES
(4, 'R00001', 8, 0, 0.00, 'NORMAL', '2023级软件工程', '适合演示普通借书与续借'),
(5, 'R00002', 6, 0, 6.50, 'NORMAL', '2022级数据科学', '存在未结清罚金，适合演示罚金结算'),
(6, 'R00003', 2, 0, 0.00, 'NORMAL', '2023级计算机科学', '当前已达到借阅上限，适合演示校验提示'),
(7, 'R00004', 8, 0, 0.00, 'NORMAL', '2024级人工智能', '已有 READY 预约记录，可直接演示预约取书'),
(8, 'R00005', 8, 0, 0.00, 'NORMAL', '2022级工商管理', '排队序号 1，可演示等待预约转为待取'),
(9, 'R00006', 8, 0, 0.00, 'NORMAL', '2023级网络安全', '排队序号 2，可演示预约队列顺延'),
(10, 'R00007', 8, 0, 0.00, 'NORMAL', '2021级数字媒体', '有在借记录，也可演示预约其他图书'),
(11, 'R00008', 8, 0, 0.00, 'PAUSED', '2024级计算机科学', '借阅证已暂停，适合演示证件状态异常');

INSERT INTO `book_category`
(`category_id`, `category_code`, `category_name`, `loan_days`, `location_code`, `is_deleted`)
VALUES
(1, 'CS', '计算机基础', 30, 'A-01', 0),
(2, 'SE', '软件工程', 30, 'A-02', 0),
(3, 'DS', '数据科学', 20, 'B-01', 0),
(4, 'ART', '设计艺术', 25, 'C-01', 0),
(5, 'MGT', '管理与创新', 20, 'D-01', 0);

INSERT INTO `publisher`
(`publisher_id`, `publisher_name`, `contact_name`, `contact_phone`, `is_deleted`)
VALUES
(1, '机械工业出版社', '王编辑', '010-12340001', 0),
(2, '人民邮电出版社', '赵编辑', '010-12340002', 0),
(3, '电子工业出版社', '陈编辑', '010-12340003', 0),
(4, '清华大学出版社', '刘编辑', '010-12340004', 0),
(5, '北京大学出版社', '孙编辑', '010-12340005', 0);

INSERT INTO `book`
(`book_id`, `isbn`, `title`, `author`, `category_id`, `publisher_id`, `publish_date`, `price`, `keywords`, `summary`, `shelf_status`, `total_copies`, `available_copies`, `is_deleted`)
VALUES
(1, '9787111123456', 'Java核心技术 卷I', 'Cay S. Horstmann', 1, 1, '2024-03-01', 139.00, 'Java,基础,后端', '面向 Java 语言核心语法、集合、并发与基础 API 的经典教材。', 'ON_SHELF', 0, 0, 0),
(2, '9787302600012', '数据结构与算法分析', 'Mark Allen Weiss', 1, 4, '2023-09-01', 89.00, '算法,数据结构', '覆盖链表、树、图、排序与复杂度分析，适合课程实验与面试准备。', 'ON_SHELF', 0, 0, 0),
(3, '9787115534562', '计算机网络：自顶向下方法', 'Kurose', 1, 2, '2022-07-01', 118.00, '网络,协议', '以应用场景驱动讲解网络分层、TCP/IP、拥塞控制与安全机制。', 'ON_SHELF', 0, 0, 0),
(4, '9787121422331', '设计心理学', 'Donald Norman', 4, 3, '2021-06-15', 68.00, '设计,用户体验', '从用户认知视角解释产品设计、交互反馈与可用性原则。', 'ON_SHELF', 0, 0, 0),
(5, '9787111654321', '数据可视化实战', 'Cole Nussbaumer', 3, 1, '2024-01-20', 79.00, '可视化,图表', '从业务表达出发介绍图表叙事、指标设计与可视化规范。', 'ON_SHELF', 0, 0, 0),
(6, '9787115612345', 'Spring Boot 微服务开发实战', '李航', 2, 2, '2024-05-18', 99.00, 'Spring Boot,微服务', '聚焦项目实战，覆盖 REST 接口、配置管理、部署与运维基础。', 'ON_SHELF', 0, 0, 0),
(7, '9787121456789', '人工智能导论', '周志华', 3, 5, '2023-08-12', 88.00, '人工智能,机器学习', '适合作为人工智能基础课程的入门图书，涵盖搜索、学习与推理。', 'ON_SHELF', 0, 0, 0),
(8, '9787301334001', '创新管理实践', '陈劲', 5, 5, '2022-09-08', 66.00, '创新,管理', '适合展示上下架管理与分类统计的管理类图书。', 'OFF_SHELF', 0, 0, 0),
(9, '9787302600784', '数据库系统概论', '王珊', 1, 4, '2024-02-10', 76.00, '数据库,SQL', '覆盖数据库系统基础概念、SQL 与事务，是课程实验常用教材。', 'ON_SHELF', 0, 0, 0);

INSERT INTO `book_copy`
(`copy_id`, `book_id`, `barcode`, `location_code`, `condition_level`, `status`, `purchase_date`, `last_inventory_date`, `is_deleted`)
VALUES
(1, 1, 'BC20260001', 'A-01-01', 'GOOD', 'BORROWED', DATE_SUB(@today, INTERVAL 180 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(2, 1, 'BC20260002', 'A-01-01', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 180 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(3, 2, 'BC20260003', 'A-01-02', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 175 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(4, 2, 'BC20260004', 'A-01-02', 'GOOD', 'RESERVED', DATE_SUB(@today, INTERVAL 175 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(5, 3, 'BC20260005', 'A-01-03', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 170 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(6, 3, 'BC20260006', 'A-01-03', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 170 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(7, 4, 'BC20260007', 'C-01-01', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 165 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(8, 5, 'BC20260008', 'B-01-02', 'NORMAL', 'REPAIR', DATE_SUB(@today, INTERVAL 160 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(9, 6, 'BC20260009', 'A-02-01', 'GOOD', 'BORROWED', DATE_SUB(@today, INTERVAL 155 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(10, 6, 'BC20260010', 'A-02-01', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 155 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(11, 3, 'BC20260011', 'A-01-03', 'WORN', 'LOST', DATE_SUB(@today, INTERVAL 150 DAY), DATE_SUB(@today, INTERVAL 30 DAY), 0),
(12, 5, 'BC20260012', 'B-01-02', 'GOOD', 'BORROWED', DATE_SUB(@today, INTERVAL 150 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(13, 7, 'BC20260013', 'B-02-01', 'GOOD', 'BORROWED', DATE_SUB(@today, INTERVAL 145 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(14, 7, 'BC20260014', 'B-02-01', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 145 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(15, 9, 'BC20260015', 'A-01-04', 'GOOD', 'BORROWED', DATE_SUB(@today, INTERVAL 140 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0),
(16, 9, 'BC20260016', 'A-01-04', 'NORMAL', 'LOST', DATE_SUB(@today, INTERVAL 140 DAY), DATE_SUB(@today, INTERVAL 20 DAY), 0),
(17, 8, 'BC20260017', 'D-01-01', 'GOOD', 'AVAILABLE', DATE_SUB(@today, INTERVAL 135 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 0);

INSERT INTO `reservation`
(`reservation_id`, `book_id`, `reader_id`, `queue_no`, `status`, `reserve_date`, `pickup_deadline`, `expire_date`, `note`, `is_deleted`)
VALUES
(1, 2, 7, 1, 'READY', DATE_SUB(@today, INTERVAL 2 DAY), DATE_ADD(@today, INTERVAL 2 DAY), NULL, '工作人员归还后已进入待取，可直接演示借出完成预约', 0),
(2, 5, 8, 1, 'WAITING', DATE_SUB(@today, INTERVAL 1 DAY), NULL, NULL, '等待当前读者归还后优先借阅', 0),
(3, 5, 9, 2, 'WAITING', @today, NULL, NULL, '排队序号 2，用于展示预约队列顺延', 0),
(4, 9, 10, 1, 'WAITING', DATE_SUB(@today, INTERVAL 3 DAY), NULL, NULL, '数据库教材暂无可借副本，进入等待队列', 0),
(5, 1, 4, 1, 'FULFILLED', DATE_SUB(@today, INTERVAL 45 DAY), DATE_SUB(@today, INTERVAL 40 DAY), DATE_SUB(@today, INTERVAL 39 DAY), '历史已完成预约记录', 0),
(6, 3, 8, 1, 'CANCELED', DATE_SUB(@today, INTERVAL 18 DAY), NULL, DATE_SUB(@today, INTERVAL 17 DAY), '读者主动取消的预约记录', 0);

INSERT INTO `borrow_record`
(`record_id`, `copy_id`, `book_id`, `reader_id`, `operator_id`, `status`, `borrow_date`, `due_date`, `return_date`, `renew_count`, `fine_amount`, `reservation_id`, `remark`, `is_deleted`)
VALUES
(1, 1, 1, 4, 2, 'BORROWED', DATE_SUB(@today, INTERVAL 11 DAY), DATE_ADD(@today, INTERVAL 19 DAY), NULL, 0, 0.00, NULL, 'reader01 当前在借，可演示续借', 0),
(2, 9, 6, 5, 2, 'OVERDUE', DATE_SUB(@today, INTERVAL 42 DAY), DATE_SUB(@today, INTERVAL 12 DAY), NULL, 1, 0.00, NULL, 'reader02 已逾期，适合演示归还和罚金规则', 0),
(3, 13, 7, 6, 3, 'BORROWED', DATE_SUB(@today, INTERVAL 7 DAY), DATE_ADD(@today, INTERVAL 13 DAY), NULL, 0, 0.00, NULL, 'reader03 当前在借 1', 0),
(4, 15, 9, 6, 2, 'BORROWED', DATE_SUB(@today, INTERVAL 9 DAY), DATE_ADD(@today, INTERVAL 21 DAY), NULL, 0, 0.00, NULL, 'reader03 当前在借 2，用于达到借阅上限', 0),
(5, 5, 3, 4, 2, 'RETURNED', DATE_SUB(@today, INTERVAL 62 DAY), DATE_SUB(@today, INTERVAL 32 DAY), DATE_SUB(@today, INTERVAL 33 DAY), 0, 0.00, NULL, '历史正常归还记录', 0),
(6, 3, 2, 8, 2, 'RETURNED', DATE_SUB(@today, INTERVAL 90 DAY), DATE_SUB(@today, INTERVAL 60 DAY), DATE_SUB(@today, INTERVAL 61 DAY), 0, 0.00, NULL, '历史借阅记录，形成借阅趋势', 0),
(7, 2, 1, 9, 3, 'RETURNED', DATE_SUB(@today, INTERVAL 118 DAY), DATE_SUB(@today, INTERVAL 88 DAY), DATE_SUB(@today, INTERVAL 89 DAY), 0, 0.00, NULL, 'Java 图书历史借阅记录', 0),
(8, 7, 4, 10, 2, 'RETURNED', DATE_SUB(@today, INTERVAL 155 DAY), DATE_SUB(@today, INTERVAL 130 DAY), DATE_SUB(@today, INTERVAL 126 DAY), 0, 2.00, NULL, '历史逾期归还记录', 0),
(9, 10, 6, 4, 2, 'RETURNED', DATE_SUB(@today, INTERVAL 142 DAY), DATE_SUB(@today, INTERVAL 112 DAY), DATE_SUB(@today, INTERVAL 113 DAY), 0, 0.00, NULL, 'Spring Boot 图书历史借阅记录', 0),
(10, 10, 6, 8, 2, 'RETURNED', DATE_SUB(@today, INTERVAL 84 DAY), DATE_SUB(@today, INTERVAL 54 DAY), DATE_SUB(@today, INTERVAL 56 DAY), 0, 0.00, NULL, '热门图书统计样例', 0),
(11, 3, 2, 9, 3, 'RETURNED', DATE_SUB(@today, INTERVAL 113 DAY), DATE_SUB(@today, INTERVAL 83 DAY), DATE_SUB(@today, INTERVAL 84 DAY), 0, 0.00, NULL, '算法教材历史借阅记录', 0),
(12, 14, 7, 10, 3, 'RETURNED', DATE_SUB(@today, INTERVAL 25 DAY), DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 6 DAY), 0, 0.00, NULL, '人工智能图书近期借阅记录', 0),
(13, 12, 5, 10, 2, 'BORROWED', DATE_SUB(@today, INTERVAL 10 DAY), DATE_ADD(@today, INTERVAL 10 DAY), NULL, 0, 0.00, NULL, 'reader07 当前占用副本，可触发等待预约转 READY', 0);

UPDATE `reader_profile` rp
SET `current_borrow_count` = (
  SELECT COUNT(1)
  FROM `borrow_record` br
  WHERE br.`reader_id` = rp.`user_id`
    AND br.`is_deleted` = 0
    AND br.`status` IN ('BORROWED', 'OVERDUE')
);

UPDATE `book` b
SET `total_copies` = (
    SELECT COUNT(1)
    FROM `book_copy` bc
    WHERE bc.`book_id` = b.`book_id`
      AND bc.`is_deleted` = 0
  ),
  `available_copies` = (
    SELECT COUNT(1)
    FROM `book_copy` bc
    WHERE bc.`book_id` = b.`book_id`
      AND bc.`is_deleted` = 0
      AND bc.`status` = 'AVAILABLE'
  );
