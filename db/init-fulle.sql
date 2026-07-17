-- ============================================
-- 富勒股权穿透系统 - 初始化脚本
-- ============================================

-- 1. 公司/平台表
CREATE TABLE IF NOT EXISTS `fulle_company` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company_name` VARCHAR(200) NOT NULL COMMENT '公司全称',
    `short_name` VARCHAR(100) DEFAULT NULL COMMENT '简称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '上级公司ID（NULL=顶层）',
    `level` INT DEFAULT 0 COMMENT '层级深度（0=最顶层）',
    `total_shares` DECIMAL(20,4) DEFAULT NULL COMMENT '总注册资本',
    `data_source` VARCHAR(50) DEFAULT '爱企查' COMMENT '数据来源',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='富勒股权-公司平台表';

-- 2. 股东/合伙人持股表
CREATE TABLE IF NOT EXISTS `fulle_shareholder` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company_id` BIGINT NOT NULL COMMENT '所属公司ID',
    `holder_name` VARCHAR(100) NOT NULL COMMENT '股东/合伙人名称',
    `holder_type` VARCHAR(20) NOT NULL DEFAULT 'PERSON' COMMENT 'PERSON=自然人, COMPANY=公司/机构, EXTERNAL=外部机构',
    `linked_company_id` BIGINT DEFAULT NULL COMMENT '若holder为公司，关联fulle_company.id',
    `share_ratio` DECIMAL(10,6) NOT NULL COMMENT '持股比例(%)',
    `share_amount` DECIMAL(20,4) DEFAULT NULL COMMENT '认缴出资额',
    `share_date` DATE DEFAULT NULL COMMENT '认缴出资日期',
    `data_source` VARCHAR(50) DEFAULT '爱企查' COMMENT '数据来源',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_company_id` (`company_id`),
    INDEX `idx_holder_name` (`holder_name`),
    INDEX `idx_holder_type` (`holder_type`),
    INDEX `idx_linked_company_id` (`linked_company_id`),
    CONSTRAINT `fk_shareholder_company` FOREIGN KEY (`company_id`) REFERENCES `fulle_company`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_shareholder_linked` FOREIGN KEY (`linked_company_id`) REFERENCES `fulle_company`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='富勒股权-股东持股明细表';

-- ============================================
-- 预设数据（爱企查 2026-07-12）
-- ============================================

-- 顶层：上海富勒信息科技有限公司
INSERT IGNORE INTO `fulle_company` (`id`, `company_name`, `short_name`, `parent_id`, `level`, `total_shares`) VALUES
(1, '上海富勒信息科技有限公司', '富勒', NULL, 0, NULL);

-- Level 1：富勒的直接股东（公司/机构类）
INSERT IGNORE INTO `fulle_company` (`id`, `company_name`, `short_name`, `parent_id`, `level`, `total_shares`) VALUES
(2, '上海勒坤企业管理中心（有限合伙）', '勒坤', 1, 1, NULL),
(3, '上海勒健企业管理咨询合伙企业（有限合伙）', '勒健', 1, 1, NULL),
(4, '上海勒滕企业管理咨询合伙企业（有限合伙）', '勒滕', 1, 1, NULL),
(5, '上海勒晟祥科技有限公司', '勒晟祥', 1, 1, NULL),
(6, '上海勒聚企业管理咨询合伙企业（有限合伙）', '勒聚', 1, 1, NULL);

-- Level 2：勒坤的子级
INSERT IGNORE INTO `fulle_company` (`id`, `company_name`, `short_name`, `parent_id`, `level`, `total_shares`) VALUES
(7, '上海勒宜企业管理咨询合伙企业（有限合伙）', '勒宜', 2, 2, NULL),
(8, '上海勒疆企业管理咨询合伙企业（有限合伙）', '勒疆', 2, 2, NULL),
(9, '上海勒禾企业管理咨询合伙企业（有限合伙）', '勒禾', 2, 2, NULL),
(10, '上海勒懋企业管理咨询合伙企业（有限合伙）', '勒懋', 2, 2, NULL);

-- Level 2：勒健的子级
INSERT IGNORE INTO `fulle_company` (`id`, `company_name`, `short_name`, `parent_id`, `level`, `total_shares`) VALUES
(11, '上海勒伴企业管理咨询合伙企业（有限合伙）', '勒伴', 3, 2, NULL),
(12, '上海勒倍企业管理咨询合伙企业（有限合伙）', '勒倍', 3, 2, NULL),
(13, '上海勒画企业管理咨询合伙企业（有限合伙）', '勒画', 3, 2, NULL);

-- Level 2：勒滕的子级
INSERT IGNORE INTO `fulle_company` (`id`, `company_name`, `short_name`, `parent_id`, `level`, `total_shares`) VALUES
(14, '上海勒盛茂企业管理咨询合伙企业（有限合伙）', '勒盛茂', 4, 2, NULL);

-- ============================================
-- 股东数据：上海富勒信息科技有限公司（16条）
-- ============================================
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(1, '上海勒坤企业管理中心（有限合伙）', 'COMPANY', 2, 27.53943, 960.758, '2017-12-28'),
(1, '师尊俐', 'PERSON', NULL, 12.86299, 448.7465, '2017-12-28'),
(1, '上海勒健企业管理咨询合伙企业（有限合伙）', 'COMPANY', 3, 11.89483, 414.9705, '2024-07-05'),
(1, '顾晓', 'PERSON', NULL, 11.28226, 393.6, '2017-12-28'),
(1, '上海勒滕企业管理咨询合伙企业（有限合伙）', 'COMPANY', 4, 8.23459, 287.2773, '2017-12-28'),
(1, '上海勒晟祥科技有限公司', 'EXTERNAL', NULL, 4.94382, 172.4733, '2017-12-28'),
(1, '南京经纬创壹号投资合伙企业（有限合伙）', 'EXTERNAL', NULL, 4.74531, 165.5478, '2017-12-28'),
(1, '天津高成德瑞投资合伙企业（有限合伙）', 'EXTERNAL', NULL, 3.69632, 128.952, '2017-12-28'),
(1, '厦门经禾里一号投资合伙企业（有限合伙）', 'EXTERNAL', NULL, 3.08026, 107.46, '2017-12-28'),
(1, '珠海鼎磊投资中心（有限合伙）', 'EXTERNAL', NULL, 3.08026, 107.46, '2017-12-28'),
(1, '朱泉生', 'PERSON', NULL, 1.97622, 68.9436, '2017-12-28'),
(1, '蔡轶', 'PERSON', NULL, 1.93484, 67.5, '2017-12-27'),
(1, '南京经纬创叁号投资合伙企业（有限合伙）', 'EXTERNAL', NULL, 1.41522, 49.3721, '2017-12-28'),
(1, '苏州天扬恒钧创业投资合伙企业（有限合伙）', 'EXTERNAL', NULL, 1.2013, 41.9094, '2017-12-28'),
(1, '无锡南山宏佶佳汇创业投资合伙企业（有限合伙）', 'EXTERNAL', NULL, 1.1236, 39.1985, '2024-12-27'),
(1, '上海勒聚企业管理咨询合伙企业（有限合伙）', 'COMPANY', 6, 0.98877, 34.4948, '2024-07-05');

-- 股东数据：上海勒坤企业管理中心（23条）
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(2, '上海勒疆企业管理咨询合伙企业（有限合伙）', 'COMPANY', 8, 20.06, 270.81, '2037-04-04'),
(2, '上海勒禾企业管理咨询合伙企业（有限合伙）', 'COMPANY', 9, 16.71, 225.585, '2037-04-04'),
(2, '上海勒懋企业管理咨询合伙企业（有限合伙）', 'COMPANY', 10, 15.67, 211.545, '2037-04-04'),
(2, '上海勒宜企业管理咨询合伙企业（有限合伙）', 'COMPANY', 7, 14.6, 197.1, '2037-04-04'),
(2, '姜炜炜', 'PERSON', NULL, 2.88, 38.88, '2027-04-04'),
(2, '朱世福', 'PERSON', NULL, 2.88, 38.88, '2027-04-04'),
(2, '张竑伟', 'PERSON', NULL, 2.56, 34.56, '2037-04-04'),
(2, '李颖', 'PERSON', NULL, 2.53, 34.155, '2037-04-04'),
(2, '余东业', 'PERSON', NULL, 2.52, 34.02, '2037-04-04'),
(2, '刘海峰', 'PERSON', NULL, 2.16, 29.16, '2027-04-04'),
(2, '吕博', 'PERSON', NULL, 1.89, 25.515, '2037-04-04'),
(2, '徐晓良', 'PERSON', NULL, 1.59, 21.465, '2037-04-04'),
(2, '杜晓聪', 'PERSON', NULL, 1.59, 21.465, '2037-04-04'),
(2, '苏靖凯', 'PERSON', NULL, 1.59, 21.465, '2037-04-04'),
(2, '靳晓阳', 'PERSON', NULL, 1.59, 21.465, '2037-04-04'),
(2, '黄兵兵', 'PERSON', NULL, 1.59, 21.465, '2037-04-04'),
(2, '费思', 'PERSON', NULL, 1.51, 20.385, '2037-04-04'),
(2, '韩汶瑾', 'PERSON', NULL, 1.51, 20.385, '2037-04-04'),
(2, '徐进成', 'PERSON', NULL, 1.44, 19.44, '2037-04-04'),
(2, '李理', 'PERSON', NULL, 1.44, 19.44, '2027-04-04'),
(2, '顾莹佳', 'PERSON', NULL, 1.34, 18.09, '2037-04-04'),
(2, '张博', 'PERSON', NULL, 0.32, 4.32, '2037-04-04'),
(2, '师尊俐', 'PERSON', NULL, 0.03, 0.405, '2027-04-04');

-- 股东数据：上海勒健企业管理咨询合伙企业（4条）
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(3, '上海勒伴企业管理咨询合伙企业（有限合伙）', 'COMPANY', 11, 33, 121.8769, '2024-07-05'),
(3, '上海勒倍企业管理咨询合伙企业（有限合伙）', 'COMPANY', 12, 33, 121.8769, '2024-07-05'),
(3, '上海勒画企业管理咨询合伙企业（有限合伙）', 'COMPANY', 13, 33, 121.8769, '2024-07-05'),
(3, '师尊俐', 'PERSON', NULL, 0.99999, 3.6932, '2023-10-26');

-- 股东数据：上海勒滕企业管理咨询合伙企业（45条）
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(4, '师尊俐', 'PERSON', NULL, 52.41, 5670.762, '2043-11-22'),
(4, '上海勒盛茂企业管理咨询合伙企业（有限合伙）', 'COMPANY', 14, 18.48, 1999.536, '2023-11-30'),
(4, '余东业', 'PERSON', NULL, 1.85, 200.17, '2043-11-23'),
(4, '姜炜炜', 'PERSON', NULL, 1.85, 200.17, '2043-11-23'),
(4, '李颖', 'PERSON', NULL, 1.85, 200.17, '2043-11-23'),
(4, '杨溢', 'PERSON', NULL, 1.85, 200.17, '2043-11-23'),
(4, '梅金平', 'PERSON', NULL, 1.85, 200.17, '2043-11-22'),
(4, '韩汶瑾', 'PERSON', NULL, 1.85, 200.17, '2043-11-22'),
(4, '朱世福', 'PERSON', NULL, 1.66, 179.612, '2043-11-23'),
(4, '刘海峰', 'PERSON', NULL, 1.39, 150.398, '2043-11-22'),
(4, '徐晓良', 'PERSON', NULL, 1.39, 150.398, '2043-11-23'),
(4, '杜晓聪', 'PERSON', NULL, 1.2, 129.84, '2043-11-22'),
(4, '陈仕填', 'PERSON', NULL, 1.11, 120.102, '2043-11-22'),
(4, '朱翎', 'PERSON', NULL, 0.92, 99.544, '2043-11-23'),
(4, '李理', 'PERSON', NULL, 0.92, 99.544, '2043-11-22'),
(4, '翟莉莉', 'PERSON', NULL, 0.92, 99.544, '2043-11-22'),
(4, '顾剑敏', 'PERSON', NULL, 0.92, 99.544, '2043-11-22'),
(4, '顾莹佳', 'PERSON', NULL, 0.92, 99.544, '2043-11-22'),
(4, '刘容', 'PERSON', NULL, 0.56, 60.592, '2043-11-22'),
(4, '刘刚', 'PERSON', NULL, 0.52, 56.264, '2043-11-22'),
(4, '吕博', 'PERSON', NULL, 0.46, 49.772, '2043-11-23'),
(4, '翁时锋', 'PERSON', NULL, 0.46, 49.772, '2023-11-30'),
(4, '苏靖凯', 'PERSON', NULL, 0.46, 49.772, '2043-11-22'),
(4, '郝晓燕', 'PERSON', NULL, 0.46, 49.772, '2043-11-22'),
(4, '靳晓阳', 'PERSON', NULL, 0.46, 49.772, '2043-11-22'),
(4, '黄兵兵', 'PERSON', NULL, 0.46, 49.772, '2043-11-22'),
(4, '朱斌', 'PERSON', NULL, 0.37, 40.034, '2023-11-30'),
(4, '黄鸿宇', 'PERSON', NULL, 0.37, 40.034, '2043-11-22'),
(4, '张宝华', 'PERSON', NULL, 0.28, 30.296, '2043-11-23'),
(4, '盛雪峰', 'PERSON', NULL, 0.28, 30.296, '2043-11-22'),
(4, '张庆达', 'PERSON', NULL, 0.18, 19.476, '2043-11-23'),
(4, '徐进成', 'PERSON', NULL, 0.18, 19.476, '2043-11-22'),
(4, '曾广煊', 'PERSON', NULL, 0.18, 19.476, '2043-11-22'),
(4, '傅昕宇', 'PERSON', NULL, 0.1, 10.82, '2043-11-22'),
(4, '孔维彪', 'PERSON', NULL, 0.1, 10.82, '2043-11-22'),
(4, '张再进', 'PERSON', NULL, 0.1, 10.82, '2043-11-23'),
(4, '葛华', 'PERSON', NULL, 0.1, 10.82, '2043-11-22'),
(4, '葛增良', 'PERSON', NULL, 0.1, 10.82, '2043-11-22'),
(4, '费思', 'PERSON', NULL, 0.1, 10.82, '2043-11-22'),
(4, '邢贞信', 'PERSON', NULL, 0.1, 10.82, '2043-11-22'),
(4, '李勇', 'PERSON', NULL, 0.09, 9.738, '2023-11-30'),
(4, '罗南', 'PERSON', NULL, 0.09, 9.738, '2023-11-30'),
(4, '王喜阳', 'PERSON', NULL, 0.04, 4.328, '2043-11-22'),
(4, '胡容', 'PERSON', NULL, 0.04, 4.328, '2043-11-22'),
(4, '常乐', 'PERSON', NULL, 0.02, 2.164, '2043-11-22');

-- 股东数据：上海勒宜企业管理咨询合伙企业（34条）
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(7, '陈仕填', 'PERSON', NULL, 9.17997, 9.5435, '2023-10-15'),
(7, '张庆达', 'PERSON', NULL, 6.12005, 6.3624, '2023-10-15'),
(7, '马礼勇', 'PERSON', NULL, 4.26, 4.4287, '2023-10-15'),
(7, '周睿', 'PERSON', NULL, 4.12005, 4.2832, '2023-10-14'),
(7, '杨洋', 'PERSON', NULL, 3.95999, 4.1168, '2023-10-15'),
(7, '林沛烁', 'PERSON', NULL, 3.95999, 4.1168, '2023-10-14'),
(7, '罗明', 'PERSON', NULL, 3.85004, 4.0025, '2023-10-14'),
(7, '吴亮', 'PERSON', NULL, 3.45999, 3.597, '2023-10-14'),
(7, '刘文明', 'PERSON', NULL, 3.43998, 3.5762, '2023-10-14'),
(7, '李光照', 'PERSON', NULL, 3.37005, 3.5035, '2023-10-14'),
(7, '高健', 'PERSON', NULL, 3.12995, 3.2539, '2023-10-15'),
(7, '彭雨露', 'PERSON', NULL, 3.08003, 3.202, '2023-10-14'),
(7, '赵帆', 'PERSON', NULL, 3.07003, 3.1916, '2023-10-16'),
(7, '龙龙', 'PERSON', NULL, 3.01, 3.1292, '2023-10-14'),
(7, '陈岩', 'PERSON', NULL, 3.0, 3.1188, '2023-10-14'),
(7, '方美淇', 'PERSON', NULL, 2.68998, 2.7965, '2023-10-14'),
(7, '李强', 'PERSON', NULL, 2.68998, 2.7965, '2023-10-14'),
(7, '梁嘉俊', 'PERSON', NULL, 2.68998, 2.7965, '2023-10-14'),
(7, '洪曼雅', 'PERSON', NULL, 2.68998, 2.7965, '2023-10-14'),
(7, '刘翮威', 'PERSON', NULL, 2.31002, 2.4015, '2023-10-23'),
(7, '曾昭杰', 'PERSON', NULL, 2.31002, 2.4015, '2023-10-14'),
(7, '罗良涛', 'PERSON', NULL, 2.31002, 2.4015, '2023-10-14'),
(7, '薛明旭', 'PERSON', NULL, 2.31002, 2.4015, '2023-10-14'),
(7, '蒋文娟', 'PERSON', NULL, 2.26, 2.3495, '2023-10-16'),
(7, '崔广智', 'PERSON', NULL, 2.25, 2.3391, '2023-10-14'),
(7, '庄林艳', 'PERSON', NULL, 2.25, 2.3391, '2023-10-15'),
(7, '胡东', 'PERSON', NULL, 2.14996, 2.2351, '2023-10-15'),
(7, '毛敏华', 'PERSON', NULL, 1.91997, 1.996, '2023-10-14'),
(7, '陈思', 'PERSON', NULL, 1.91997, 1.996, '2023-10-23'),
(7, '王恩', 'PERSON', NULL, 1.87995, 1.9544, '2023-10-14'),
(7, '岳斯文', 'PERSON', NULL, 1.51, 1.5698, '2023-10-15'),
(7, '要作钰', 'PERSON', NULL, 1.5, 1.5594, '2023-10-14'),
(7, '徐金鹏', 'PERSON', NULL, 1.25, 1.2995, '2023-10-15'),
(7, '师尊俐', 'PERSON', NULL, 0.10004, 0.104, '2024-09-19');

-- 股东数据：上海勒伴企业管理咨询合伙企业（46条）
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(11, '师尊俐', 'PERSON', NULL, 25.56038, 31.1522, '2024-06-28'),
(11, '罗南', 'PERSON', NULL, 3.33, 4.0585, '2023-10-14'),
(11, '宋大清', 'PERSON', NULL, 2.66999, 3.2541, '2023-10-14'),
(11, '张鹏', 'PERSON', NULL, 2.61001, 3.181, '2024-06-27'),
(11, '翁时锋', 'PERSON', NULL, 2.61001, 3.181, '2024-06-27'),
(11, '张建伟', 'PERSON', NULL, 2.57998, 3.1444, '2023-10-15'),
(11, '杨波', 'PERSON', NULL, 2.55003, 3.1079, '2023-10-15'),
(11, '张元斌', 'PERSON', NULL, 2.27, 2.7666, '2023-10-14'),
(11, '朱志帆', 'PERSON', NULL, 2.23997, 2.73, '2023-10-15'),
(11, '韦学文', 'PERSON', NULL, 2.23997, 2.73, '2023-10-14'),
(11, '卢斯', 'PERSON', NULL, 2.22996, 2.7178, '2023-10-15'),
(11, '米思玮', 'PERSON', NULL, 2.19, 2.6691, '2023-10-15'),
(11, '郑明华', 'PERSON', NULL, 1.99997, 2.4375, '2023-10-14'),
(11, '吕虹', 'PERSON', NULL, 1.95, 2.3766, '2024-06-28'),
(11, '罗江', 'PERSON', NULL, 1.95, 2.3766, '2023-10-15'),
(11, '吴家海', 'PERSON', NULL, 1.75005, 2.1329, '2024-06-27'),
(11, '林坊', 'PERSON', NULL, 1.74003, 2.1207, '2024-06-27'),
(11, '谭晓文', 'PERSON', NULL, 1.73995, 2.1206, '2023-10-18'),
(11, '关家新', 'PERSON', NULL, 1.66996, 2.0353, '2023-10-15'),
(11, '叶剑忠', 'PERSON', NULL, 1.66996, 2.0353, '2023-10-15'),
(11, '姜玲', 'PERSON', NULL, 1.66996, 2.0353, '2023-10-15'),
(11, '赵明琼', 'PERSON', NULL, 1.66996, 2.0353, '2023-10-14'),
(11, '韦信华', 'PERSON', NULL, 1.66996, 2.0353, '2023-10-16'),
(11, '黄积鑫', 'PERSON', NULL, 1.66996, 2.0353, '2023-10-15'),
(11, '赵馨怡', 'PERSON', NULL, 1.49003, 1.816, '2023-10-15'),
(11, '余龙海', 'PERSON', NULL, 1.48002, 1.8038, '2024-06-27'),
(11, '吴林春', 'PERSON', NULL, 1.48002, 1.8038, '2024-06-27'),
(11, '鄢亚辉', 'PERSON', NULL, 1.48002, 1.8038, '2024-06-27'),
(11, '陈海锋', 'PERSON', NULL, 1.44999, 1.7672, '2023-10-15'),
(11, '张亮亮', 'PERSON', NULL, 1.41996, 1.7306, '2023-10-14'),
(11, '吴泽凯', 'PERSON', NULL, 1.33003, 1.621, '2024-06-28'),
(11, '吴禄', 'PERSON', NULL, 1.33003, 1.621, '2024-06-28'),
(11, '张余', 'PERSON', NULL, 1.3, 1.5844, '2023-10-14'),
(11, '谢作将', 'PERSON', NULL, 1.3, 1.5844, '2023-10-14'),
(11, '陈明杰', 'PERSON', NULL, 1.16995, 1.4259, '2023-10-15'),
(11, '冯永权', 'PERSON', NULL, 1.15001, 1.4016, '2024-06-27'),
(11, '刘贻湖', 'PERSON', NULL, 1.15001, 1.4016, '2024-06-27'),
(11, '周享誉', 'PERSON', NULL, 1.15001, 1.4016, '2024-06-27'),
(11, '辛培根', 'PERSON', NULL, 1.15001, 1.4016, '2024-06-27'),
(11, '罗爽', 'PERSON', NULL, 1.08995, 1.3284, '2023-10-23'),
(11, '吴泉东', 'PERSON', NULL, 1.00003, 1.2188, '2024-06-27'),
(11, '刘金旺', 'PERSON', NULL, 0.97, 1.1822, '2023-10-14'),
(11, '曹振兴', 'PERSON', NULL, 0.84996, 1.0359, '2023-10-14'),
(11, '胡波', 'PERSON', NULL, 0.84996, 1.0359, '2023-10-15'),
(11, '陈志燚', 'PERSON', NULL, 0.84996, 1.0359, '2023-10-15'),
(11, '李斌', 'PERSON', NULL, 0.33001, 0.4022, '2024-06-27');

-- 股东数据：上海勒盛茂企业管理咨询合伙企业（50条）
INSERT IGNORE INTO `fulle_shareholder` (`company_id`, `holder_name`, `holder_type`, `linked_company_id`, `share_ratio`, `share_amount`, `share_date`) VALUES
(14, '师尊俐', 'PERSON', NULL, 6.3, 126, '2033-12-22'),
(14, '周恩富', 'PERSON', NULL, 5.0, 100, '2033-12-12'),
(14, '杨宇子', 'PERSON', NULL, 5.0, 100, '2033-12-12'),
(14, '杨洋', 'PERSON', NULL, 5.0, 100, '2033-12-12'),
(14, '白隆德', 'PERSON', NULL, 5.0, 100, '2033-12-12'),
(14, '邵宝衡', 'PERSON', NULL, 5.0, 100, '2033-12-12'),
(14, '陈贤德', 'PERSON', NULL, 5.0, 100, '2033-12-12'),
(14, '管军保', 'PERSON', NULL, 4.75, 95, '2033-12-12'),
(14, '林沛烁', 'PERSON', NULL, 3.5, 70, '2033-12-12'),
(14, '周睿', 'PERSON', NULL, 2.5, 50, '2033-12-12'),
(14, '宋大清', 'PERSON', NULL, 2.5, 50, '2033-12-12'),
(14, '常勇', 'PERSON', NULL, 2.5, 50, '2033-12-12'),
(14, '路琦', 'PERSON', NULL, 2.5, 50, '2033-12-12'),
(14, '高诗林', 'PERSON', NULL, 2.5, 50, '2033-12-22'),
(14, '赵海峰', 'PERSON', NULL, 2.25, 45, '2033-12-22'),
(14, '陈海涛', 'PERSON', NULL, 2.25, 45, '2033-12-22'),
(14, '吴希', 'PERSON', NULL, 2.0, 40, '2033-12-12'),
(14, '张文明', 'PERSON', NULL, 2.0, 40, '2033-12-22'),
(14, '邴守旭', 'PERSON', NULL, 2.0, 40, '2033-12-22'),
(14, '王太平', 'PERSON', NULL, 1.75, 35, '2033-12-22'),
(14, '姜云凤', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '李艳秋', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '潘革', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '王鹏', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '迟志强', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '邬娜娜', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '金凤娇', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '马礼勇', 'PERSON', NULL, 1.5, 30, '2033-12-22'),
(14, '刘敬敬', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '吴亮', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '周勇', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '周鑫', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '方菊花', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '李扬武', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '洪曼雅', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '王志平', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '罗明', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '谭建春', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '高兵', 'PERSON', NULL, 1.0, 20, '2023-12-22'),
(14, '黄云', 'PERSON', NULL, 1.0, 20, '2033-12-22'),
(14, '张建伟', 'PERSON', NULL, 0.9, 18, '2033-12-22'),
(14, '尹强', 'PERSON', NULL, 0.75, 15, '2033-12-22'),
(14, '张晓月', 'PERSON', NULL, 0.75, 15, '2033-12-22'),
(14, '李秀春', 'PERSON', NULL, 0.75, 15, '2033-12-22'),
(14, '涂彬', 'PERSON', NULL, 0.75, 15, '2033-12-22'),
(14, '方美淇', 'PERSON', NULL, 0.6, 12, '2033-12-22'),
(14, '杨胜', 'PERSON', NULL, 0.6, 12, '2033-12-22'),
(14, '赵南', 'PERSON', NULL, 0.6, 12, '2033-12-22'),
(14, '夏天', 'PERSON', NULL, 0.5, 10, '2033-12-22'),
(14, '龙龙', 'PERSON', NULL, 0.5, 10, '2033-12-22');

-- ============================================
-- 菜单注册
-- ============================================
INSERT IGNORE INTO `sys_menu` (`menu_name`, `menu_code`, `url`, `icon`, `parent_id`, `sort_order`) VALUES
('富勒股权', 'fulle_share', '/fulle/', '🏢', 11, 4);

-- 超级管理员角色 拥有新菜单（menu_id 自动生成，可直接通过 menu_code 关联）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE `menu_code` = 'fulle_share';