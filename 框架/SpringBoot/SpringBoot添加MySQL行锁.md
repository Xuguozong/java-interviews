#### 背景:

> 一个账单包含若干人员账单明细数据，需要在一个方法内分别生成一个账单编号和若干人员编号且编号都是全局唯一的。

#### 实现思路：

- **读取人员编号时添加数据库行锁(SELECT ... FOR UPDATE)**
- 按人员条数生成人员编号
- 更新人员编号的补偿(nextValue+人数)信息
- **手动提交事务**

#### 具体实现：

##### 数据库结构

```
-- ----------------------------
-- Table structure for platform_bizcode
-- ----------------------------
DROP TABLE IF EXISTS `platform_bizcode`;
CREATE TABLE `platform_bizcode`  (
  `biz_code_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '编码类型',
  `code_prefix` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '前缀',
  `code_length` int NULL DEFAULT NULL COMMENT '长度',
  `step` int NULL DEFAULT NULL COMMENT '步长',
  `next_value` int NULL DEFAULT NULL COMMENT '下一个值',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`biz_code_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '订单编号配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of platform_bizcode
-- ----------------------------
INSERT INTO `platform_bizcode` VALUES ('batch_no', 'BT', 8, 1, 536, '2020-11-18 22:46:56', '2020-11-18 22:46:57', '导入批次号');
INSERT INTO `platform_bizcode` VALUES ('bill_no', 'ZDBH', 5, 1, 113, '2021-10-11 16:43:43', '2021-10-11 16:43:45', '账单编号');
INSERT INTO `platform_bizcode` VALUES ('bill_staff_no', 'RYBH', 5, 1, 23, '2021-11-25 15:14:21', '2021-11-25 15:14:24', '账单人员编号');
INSERT INTO `platform_bizcode` VALUES ('job_no', 'GW', 5, 1, 186, '2021-01-18 17:25:14', '2021-01-18 17:25:17', '岗位编号');
INSERT INTO `platform_bizcode` VALUES ('order_cost_no', 'JF', 5, 1, 181, '2020-11-15 09:28:52', '2020-11-15 09:28:52', '临工任务费用编号');
INSERT INTO `platform_bizcode` VALUES ('order_no', 'DD', 5, 1, 257, '2020-11-15 09:27:58', '2020-11-15 09:27:58', '临工订单编号');
INSERT INTO `platform_bizcode` VALUES ('service_no', 'FWX', 5, 1, 22557, '2021-04-14 17:48:39', '2021-04-14 17:48:39', '服务项编号');
INSERT INTO `platform_bizcode` VALUES ('staff_contract_no', 'HTBH', 5, 1, 2319, '2021-03-09 15:42:23', '2021-03-09 15:42:27', '入职合同编号');
INSERT INTO `platform_bizcode` VALUES ('supplier_no', 'GYS', 5, 1, 58, '2020-11-15 09:27:58', '2020-11-15 09:27:58', '供应链编号');
INSERT INTO `platform_bizcode` VALUES ('task_no', 'RW', 5, 1, 261, '2020-11-15 09:27:58', '2020-11-15 09:27:58', '临工订单任务编号');
```

##### 代码实现

> BillService#importBills:

```java
private final DataSourceTransactionManager dataSourceTransactionManager;

private final TransactionDefinition transactionDefinition;

TransactionStatus transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
PlatformBizcode bizcode = bizcodeService.bizcode("bill_staff_no");
// ... 业务处理
// 更新步长
bizcodeService.updateNextValue(nextValue.get(), "bill_staff_no");
// 手动提交事务
dataSourceTransactionManager.commit(transaction);
```

> ```
> platformBizcodeMapper.selectByPrimaryKeyForUpdate(code)
> ```

```java
@Select("SELECT * FROM platform_bizcode WHERE biz_code_type=#{code} FOR UPDATE")
    PlatformBizcode selectByPrimaryKeyForUpdate(String code);
```

