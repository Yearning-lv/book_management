# 图书管理系统

基于 `JavaFX + Spring Boot 3 + MySQL` 的前后端分离课程实验项目，面向高校图书馆场景，覆盖账户管理、图书目录、馆藏副本、借阅流转、预约排队与统计分析。

## 目录结构

```text
  backend/   Spring Boot 3 后端
  frontend/  JavaFX 客户端
  sql/       建表脚本与演示数据
  docs/      需求、数据库、接口、部署文档
```

## 角色说明

- 管理员：管理工作人员与读者账户，查看系统统计。
- 工作人员：维护图书目录、馆藏副本，处理借阅与归还。
- 读者：检索图书、查看个人借阅、预约排队、续借。

## 快速启动

1. 使用 MySQL 执行 [`sql/library_management_schema.sql`](./sql/library_management_schema.sql)。
2. 再执行 [`sql/library_management_seed.sql`](./sql/library_management_seed.sql) 导入演示数据。
3. 修改 [`backend/src/main/resources/application.yml`](./backend/src/main/resources/application.yml) 中数据库账号密码。
4. 在 `backend/` 下执行：`mvnw.cmd clean package`
5. 在 `frontend/` 下执行：`mvnw.cmd clean package`
6. 先启动后端，再启动前端。

## 演示账号

- 管理员：`admin / 123456`
- 工作人员：`staff01 / 123456`
- 工作人员：`staff02 / 123456`
- 读者：`reader01 / 123456`
- 读者：`reader02 / 123456`
- 读者：`reader03 / 123456`
- 读者：`reader04 / 123456`
- 读者：`reader05 / 123456`
- 读者：`reader06 / 123456`
- 读者：`reader07 / 123456`
- 读者：`reader08 / 123456`

## 演示辅助

- 启动系统：[`start-library.cmd`](./start-library.cmd)
- 重置演示数据：[`reset-demo-data.cmd`](./reset-demo-data.cmd)
- 中文演示流程：[`docs/演示案例与答辩流程.md`](./docs/演示案例与答辩流程.md)

## 核心亮点

- 三角色权限分离，符合课程“用户与权限管理”要求。
- 至少四个核心功能模块：账户、图书目录、馆藏副本、借阅中心。
- 借阅复杂流程完整：预约排队、待取、借出、续借、逾期、归还、罚金结算。
- 内置多读者、多工作人员、多状态演示数据，适合课堂答辩与逐模块验收。
- 统计可视化完整：系统概览、分类分布、借阅趋势、热门图书。
