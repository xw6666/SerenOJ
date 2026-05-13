-- ============================================================
-- SerenOJ Database Schema (精简自 HOJ)
-- 保留: 题目 / 判题 / 比赛 / 训练 / 用户
-- 砍掉: 讨论 / 群组 / 通知 / 远程OJ / 打印 / 评分
-- ============================================================

CREATE DATABASE IF NOT EXISTS `serenoj` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `serenoj`;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、用户域 (5 张)
-- ============================================================

DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `uuid`        varchar(32)  NOT NULL,
  `username`    varchar(100) NOT NULL           COMMENT '用户名',
  `password`    varchar(100) NOT NULL           COMMENT '密码(BCrypt)',
  `nickname`    varchar(100) DEFAULT NULL       COMMENT '昵称',
  `school`      varchar(100) DEFAULT NULL       COMMENT '学校',
  `course`      varchar(100) DEFAULT NULL       COMMENT '专业',
  `number`      varchar(20)  DEFAULT NULL       COMMENT '学号',
  `realname`    varchar(100) DEFAULT NULL       COMMENT '真实姓名',
  `gender`      varchar(20)  NOT NULL DEFAULT 'secrecy' COMMENT '性别',
  `github`      varchar(255) DEFAULT NULL       COMMENT 'GitHub地址',
  `blog`        varchar(255) DEFAULT NULL       COMMENT '博客地址',
  `email`       varchar(320) DEFAULT NULL       COMMENT '邮箱',
  `avatar`      varchar(255) DEFAULT NULL       COMMENT '头像地址',
  `signature`   mediumtext                      COMMENT '个性签名',
  `title_name`  varchar(255) DEFAULT NULL       COMMENT '头衔称号',
  `title_color` varchar(255) DEFAULT NULL       COMMENT '头衔颜色',
  `status`      int(11)      NOT NULL DEFAULT 0 COMMENT '0正常 1封禁',
  `gmt_create`  datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `UK_USERNAME` (`username`),
  UNIQUE KEY `UK_EMAIL` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id`           bigint(20) unsigned zerofill NOT NULL,
  `role`         varchar(50)  NOT NULL       COMMENT '角色标识',
  `description`  varchar(100) DEFAULT NULL   COMMENT '描述',
  `status`       tinyint(4)   NOT NULL DEFAULT 0 COMMENT '0可用 1不可用',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `auth`;
CREATE TABLE `auth` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name`         varchar(100) DEFAULT NULL COMMENT '权限名称',
  `permission`   varchar(100) DEFAULT NULL COMMENT '权限字符串',
  `status`       tinyint(4)   NOT NULL DEFAULT 0 COMMENT '0可用 1不可用',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `role_auth`;
CREATE TABLE `role_auth` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `auth_id`      bigint(20) unsigned NOT NULL,
  `role_id`      bigint(20) unsigned NOT NULL,
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_auth_id` (`auth_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_role_auth_auth` FOREIGN KEY (`auth_id`) REFERENCES `auth` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_auth_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `uid`          varchar(32)         NOT NULL,
  `role_id`      bigint(20) unsigned NOT NULL,
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_user_role_uid`  FOREIGN KEY (`uid`)     REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role`      (`id`)   ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 二、编程语言 (1 张)
-- ============================================================

DROP TABLE IF EXISTS `language`;
CREATE TABLE `language` (
  `id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `content_type`    varchar(255) DEFAULT NULL COMMENT 'CodeMirror语法高亮类型',
  `description`     varchar(255) DEFAULT NULL COMMENT '语言描述(版本号)',
  `name`            varchar(255) DEFAULT NULL COMMENT '语言名称',
  `compile_command` mediumtext            COMMENT '编译指令(DB存储用, 实际以language.yml为准)',
  `template`        longtext              COMMENT 'A+B示例模板',
  `code_template`   longtext              COMMENT '默认代码模板',
  `is_spj`          tinyint(1)   DEFAULT 0 COMMENT '是否可作为SPJ/交互语言',
  `oj`              varchar(255) DEFAULT 'ME' COMMENT '所属OJ(固定ME)',
  `seq`             int(11)      DEFAULT 0 COMMENT '排序号',
  `gmt_create`      datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 三、题目域 (9 张)
-- ============================================================

DROP TABLE IF EXISTS `problem`;
CREATE TABLE `problem` (
  `id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `problem_id`        varchar(255) NOT NULL       COMMENT '题号(如 P1000)',
  `title`             varchar(255) NOT NULL       COMMENT '标题',
  `author`            varchar(255) DEFAULT '未知' COMMENT '作者(纯字符串,无FK)',
  `type`              int(11)      NOT NULL DEFAULT 0 COMMENT '0=ACM 1=OI',
  `time_limit`        int(11)      DEFAULT 1000   COMMENT '时间限制(ms)',
  `memory_limit`      int(11)      DEFAULT 65535  COMMENT '内存限制(KB)',
  `stack_limit`       int(11)      DEFAULT 128    COMMENT '栈限制(MB)',
  `description`       longtext                    COMMENT '题目描述(Markdown/HTML)',
  `input`             longtext                    COMMENT '输入说明',
  `output`            longtext                    COMMENT '输出说明',
  `examples`          longtext                    COMMENT '题面样例(JSON)',
  `source`            text                        COMMENT '题目来源',
  `difficulty`        int(11)      DEFAULT 0      COMMENT '难度:0简单 1中等 2困难',
  `hint`              longtext                    COMMENT '提示/备注',
  `auth`              int(11)      DEFAULT 1      COMMENT '1公开 2私有 3比赛',
  `io_score`          int(11)      DEFAULT 100    COMMENT 'OI题目总分',
  `code_share`        tinyint(1)   DEFAULT 1      COMMENT '提交代码可否被他人查看',
  `judge_mode`        varchar(255) DEFAULT 'default' COMMENT '判题模式:default/spj/interactive',
  `judge_case_mode`   varchar(255) DEFAULT 'default' COMMENT '样例评测模式:default/subtask_lowest/subtask_average',
  `user_extra_file`   mediumtext   DEFAULT NULL    COMMENT '用户需额外提供的文件(JSON)',
  `judge_extra_file`  mediumtext   DEFAULT NULL    COMMENT '判题需额外提供的文件(JSON)',
  `spj_code`          longtext                    COMMENT 'SPJ/交互程序源码',
  `spj_language`      varchar(255) DEFAULT NULL    COMMENT 'SPJ/交互程序的语言',
  `is_remove_end_blank` tinyint(1) DEFAULT 1      COMMENT '是否去除文末空格',
  `open_case_result`  tinyint(1)   DEFAULT 1      COMMENT '是否展示测试点详情',
  `is_upload_case`    tinyint(1)   DEFAULT 1      COMMENT '测试数据是否上传为文件',
  `case_version`      varchar(40)  DEFAULT '0'    COMMENT '测试数据版本号',
  `modified_user`     varchar(255) DEFAULT NULL    COMMENT '最后修改者',
  `is_file_io`        tinyint(1)   DEFAULT 0      COMMENT '是否文件IO模式',
  `io_read_file_name` varchar(255) DEFAULT NULL    COMMENT '输入文件名',
  `io_write_file_name` varchar(255) DEFAULT NULL   COMMENT '输出文件名',
  `gmt_create`        datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_problem_id` (`problem_id`),
  KEY `idx_author` (`author`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `problem_case`;
CREATE TABLE `problem_case` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pid`          bigint(20) unsigned NOT NULL       COMMENT '题目ID',
  `input`        longtext                           COMMENT '测试输入',
  `output`       longtext                           COMMENT '期望输出',
  `score`        int(11)      DEFAULT NULL          COMMENT 'OI模式该测试点分值',
  `status`       int(11)      DEFAULT 0             COMMENT '0正常 1禁用',
  `group_num`    int(11)      DEFAULT 1             COMMENT 'subtask分组编号',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  CONSTRAINT `fk_problem_case_pid` FOREIGN KEY (`pid`) REFERENCES `problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `tag_classification`;
CREATE TABLE `tag_classification` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `name`         varchar(255)    NOT NULL       COMMENT '分类名称(如"算法""数据结构")',
  `oj`           varchar(255)    NOT NULL DEFAULT 'ME' COMMENT '所属OJ',
  `rank`         int(10) unsigned zerofill DEFAULT NULL COMMENT '排序优先级(越小越前)',
  `gmt_create`   datetime        DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name`         varchar(255)  NOT NULL          COMMENT '标签名(如"动态规划")',
  `color`        varchar(10)   DEFAULT NULL      COMMENT '标签颜色(#RRGGBB)',
  `oj`           varchar(255)  DEFAULT 'ME'      COMMENT '所属OJ',
  `tcid`         bigint(20) unsigned DEFAULT NULL COMMENT '分类ID',
  `gmt_create`   datetime      DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_oj` (`name`, `oj`),
  KEY `idx_tcid` (`tcid`),
  CONSTRAINT `fk_tag_tcid` FOREIGN KEY (`tcid`) REFERENCES `tag_classification` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `problem_tag`;
CREATE TABLE `problem_tag` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pid`          bigint(20) unsigned NOT NULL,
  `tid`          bigint(20) unsigned NOT NULL,
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_tid` (`tid`),
  CONSTRAINT `fk_problem_tag_pid` FOREIGN KEY (`pid`) REFERENCES `problem` (`id`)  ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_problem_tag_tid` FOREIGN KEY (`tid`) REFERENCES `tag`     (`id`)  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `problem_language`;
CREATE TABLE `problem_language` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pid`          bigint(20) unsigned NOT NULL,
  `lid`          bigint(20) unsigned NOT NULL,
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_lid` (`lid`),
  CONSTRAINT `fk_problem_lang_pid` FOREIGN KEY (`pid`) REFERENCES `problem`  (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_problem_lang_lid` FOREIGN KEY (`lid`) REFERENCES `language` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `code_template`;
CREATE TABLE `code_template` (
  `id`           int(11) NOT NULL AUTO_INCREMENT,
  `pid`          bigint(20) unsigned NOT NULL,
  `lid`          bigint(20) unsigned NOT NULL,
  `code`         longtext NOT NULL,
  `status`       tinyint(1) DEFAULT 0 COMMENT '0正常 1禁用',
  `gmt_create`   datetime  DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_lid` (`lid`),
  CONSTRAINT `fk_code_template_pid` FOREIGN KEY (`pid`) REFERENCES `problem`  (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_code_template_lid` FOREIGN KEY (`lid`) REFERENCES `language` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `problem_count`;
CREATE TABLE `problem_count` (
  `pid`          bigint(20) unsigned NOT NULL COMMENT '题目ID(也是主键)',
  `total`        int(11) DEFAULT 0 COMMENT '总提交数',
  `ac`           int(11) DEFAULT 0 COMMENT 'Accepted',
  `wa`           int(11) DEFAULT 0 COMMENT 'Wrong Answer',
  `tle`          int(11) DEFAULT 0 COMMENT 'Time Limit Exceeded',
  `mle`          int(11) DEFAULT 0 COMMENT 'Memory Limit Exceeded',
  `re`           int(11) DEFAULT 0 COMMENT 'Runtime Error',
  `pe`           int(11) DEFAULT 0 COMMENT 'Presentation Error',
  `ce`           int(11) DEFAULT 0 COMMENT 'Compile Error',
  `se`           int(11) DEFAULT 0 COMMENT 'System Error',
  `pa`           int(11) DEFAULT 0 COMMENT 'Partial Accepted',
  `version`      bigint(20) DEFAULT 0 COMMENT '乐观锁版本号',
  `gmt_create`   datetime   DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`pid`),
  CONSTRAINT `fk_problem_count_pid` FOREIGN KEY (`pid`) REFERENCES `problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 四、判题域 (2 张)
-- ============================================================

DROP TABLE IF EXISTS `judge`;
CREATE TABLE `judge` (
  `submit_id`       bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pid`             bigint(20) unsigned NOT NULL   COMMENT '题目ID',
  `display_pid`     varchar(255) NOT NULL          COMMENT '题目展示ID(如P1000)',
  `uid`             varchar(32)  NOT NULL          COMMENT '提交者UUID',
  `username`        varchar(255) DEFAULT NULL      COMMENT '提交者用户名',
  `submit_time`     datetime     NOT NULL          COMMENT '提交时间',
  `status`          int(11)      DEFAULT NULL      COMMENT '结果码(见Constants)',
  `share`           tinyint(1)   DEFAULT 0         COMMENT '0仅自己可见 1公开',
  `error_message`   mediumtext                     COMMENT '编译器错误信息',
  `time`            int(11)      DEFAULT NULL      COMMENT '运行时间(ms)',
  `memory`          int(11)      DEFAULT NULL      COMMENT '运行内存(KB)',
  `score`           int(11)      DEFAULT NULL      COMMENT 'OI得分',
  `length`          int(11)      DEFAULT NULL      COMMENT '代码长度(字节)',
  `code`            longtext     NOT NULL          COMMENT '源代码',
  `language`        varchar(255) DEFAULT NULL      COMMENT '编程语言',
  `cid`             bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '比赛ID(0=非比赛)',
  `cpid`            bigint(20) unsigned DEFAULT 0  COMMENT '比赛中题目排序ID',
  `judger`          varchar(20)  DEFAULT NULL      COMMENT '判题机标识',
  `ip`              varchar(64)  DEFAULT NULL      COMMENT '提交者IP',
  `oi_rank_score`   int(11)      DEFAULT 0         COMMENT 'OI排行榜得分',
  `gmt_create`      datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`submit_id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_uid` (`uid`),
  KEY `idx_username` (`username`),
  CONSTRAINT `fk_judge_pid` FOREIGN KEY (`pid`) REFERENCES `problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_judge_uid` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `judge_case`;
CREATE TABLE `judge_case` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `submit_id`    bigint(20) unsigned NOT NULL      COMMENT '提交ID',
  `uid`          varchar(32)  NOT NULL             COMMENT '用户UUID',
  `pid`          bigint(20) unsigned NOT NULL      COMMENT '题目ID',
  `case_id`      bigint(20)   DEFAULT NULL         COMMENT '测试点ID',
  `status`       int(11)      DEFAULT NULL         COMMENT '结果码',
  `time`         int(11)      DEFAULT NULL         COMMENT '该测点耗时(ms)',
  `memory`       int(11)      DEFAULT NULL         COMMENT '该测点内存(KB)',
  `score`        int(11)      DEFAULT NULL         COMMENT 'OI该测点得分',
  `group_num`    int(11)      DEFAULT NULL         COMMENT 'subtask分组号',
  `seq`          int(11)      DEFAULT NULL         COMMENT '测试点序号',
  `mode`         varchar(255) DEFAULT 'default'    COMMENT '评测模式',
  `input_data`   longtext                          COMMENT '测点输入(赛后可见)',
  `output_data`  longtext                          COMMENT '期望输出(赛后可见)',
  `user_output`  longtext                          COMMENT '用户实际输出(赛后可见)',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_submit_id` (`submit_id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_pid` (`pid`),
  CONSTRAINT `fk_judge_case_submit` FOREIGN KEY (`submit_id`) REFERENCES `judge`      (`submit_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_judge_case_uid`    FOREIGN KEY (`uid`)      REFERENCES `user_info`   (`uuid`)      ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_judge_case_pid`    FOREIGN KEY (`pid`)      REFERENCES `problem`     (`id`)        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 五、比赛域 (4 张)
-- ============================================================

DROP TABLE IF EXISTS `contest`;
CREATE TABLE `contest` (
  `id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `uid`             varchar(32)  NOT NULL          COMMENT '创建者UUID',
  `author`          varchar(255) DEFAULT NULL      COMMENT '创建者用户名',
  `title`           varchar(255) DEFAULT NULL      COMMENT '比赛标题',
  `type`            int(11)      NOT NULL DEFAULT 0 COMMENT '0=ACM 1=OI',
  `description`     longtext                       COMMENT '比赛说明',
  `source`          int(11)      DEFAULT 0         COMMENT '0原创 其他为克隆源ID',
  `auth`            int(11)      NOT NULL          COMMENT '0公开 1私有(需密码访问) 2保护(需密码提交)',
  `pwd`             varchar(255) DEFAULT NULL      COMMENT '比赛密码',
  `start_time`      datetime     DEFAULT NULL      COMMENT '开始时间',
  `end_time`        datetime     DEFAULT NULL      COMMENT '结束时间',
  `duration`        bigint(20)   DEFAULT NULL      COMMENT '比赛时长(秒)',
  `seal_rank`       tinyint(1)   DEFAULT 0         COMMENT '是否封榜',
  `seal_rank_time`  datetime     DEFAULT NULL      COMMENT '封榜时间',
  `auto_real_rank`  tinyint(1)   DEFAULT 1         COMMENT '结束后自动解封',
  `status`          int(11)      DEFAULT NULL      COMMENT '-1未开始 0进行中 1已结束',
  `visible`         tinyint(1)   DEFAULT 1         COMMENT '是否可见',
  `rank_show_name`  varchar(20)  DEFAULT 'username' COMMENT '排名显示:username/nickname/realname',
  `oi_rank_score_type` varchar(255) DEFAULT 'Recent' COMMENT 'OI计分方式:Recent/Highest',
  `allow_end_submit` tinyint(1)  DEFAULT 0         COMMENT '是否允许赛后提交',
  `gmt_create`      datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  CONSTRAINT `fk_contest_uid` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `contest_problem`;
CREATE TABLE `contest_problem` (
  `id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `display_id`    varchar(255) NOT NULL          COMMENT '比赛内显示ID(如A/B/C)',
  `cid`           bigint(20) unsigned NOT NULL   COMMENT '比赛ID',
  `pid`           bigint(20) unsigned NOT NULL   COMMENT '题目ID',
  `display_title` varchar(255) NOT NULL          COMMENT '比赛内标题',
  `color`         varchar(255) DEFAULT NULL      COMMENT '气球颜色',
  `gmt_create`    datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_display` (`display_id`, `cid`, `pid`),
  KEY `idx_cid` (`cid`),
  KEY `idx_pid` (`pid`),
  CONSTRAINT `fk_contest_prob_cid` FOREIGN KEY (`cid`) REFERENCES `contest` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contest_prob_pid` FOREIGN KEY (`pid`) REFERENCES `problem` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `contest_record`;
CREATE TABLE `contest_record` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `cid`          bigint(20) unsigned DEFAULT NULL COMMENT '比赛ID',
  `uid`          varchar(255) NOT NULL          COMMENT '用户UUID',
  `pid`          bigint(20) unsigned DEFAULT NULL COMMENT '题目ID',
  `cpid`         bigint(20) unsigned DEFAULT NULL COMMENT '比赛题目ID',
  `username`     varchar(255) DEFAULT NULL     COMMENT '用户名',
  `realname`     varchar(255) DEFAULT NULL     COMMENT '真实姓名',
  `display_id`   varchar(255) DEFAULT NULL     COMMENT '比赛展示ID',
  `submit_id`    bigint(20) unsigned NOT NULL  COMMENT '提交ID(可重判)',
  `status`       int(11) DEFAULT NULL          COMMENT '0未AC无罚时 1=AC -1=未AC有罚时',
  `submit_time`  datetime NOT NULL             COMMENT '提交时间',
  `time`         bigint(20) unsigned DEFAULT NULL COMMENT '距比赛开始的秒数',
  `score`        int(11) DEFAULT NULL          COMMENT 'OI得分',
  `use_time`     int(11) DEFAULT NULL          COMMENT '运行耗时(ms)',
  `first_blood`  tinyint(1) DEFAULT 0          COMMENT '(废弃)',
  `checked`      tinyint(1) DEFAULT 0          COMMENT 'AC是否已审核',
  `gmt_create`   datetime  DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cid` (`cid`),
  KEY `idx_uid` (`uid`),
  KEY `idx_pid` (`pid`),
  KEY `idx_cpid` (`cpid`),
  KEY `idx_submit_id` (`submit_id`),
  KEY `idx_time` (`time`),
  CONSTRAINT `fk_contest_rec_cid`   FOREIGN KEY (`cid`)       REFERENCES `contest`         (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contest_rec_uid`   FOREIGN KEY (`uid`)       REFERENCES `user_info`       (`uuid`)      ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contest_rec_pid`   FOREIGN KEY (`pid`)       REFERENCES `problem`         (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contest_rec_cpid`  FOREIGN KEY (`cpid`)      REFERENCES `contest_problem` (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contest_rec_submit` FOREIGN KEY (`submit_id`) REFERENCES `judge`           (`submit_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `contest_register`;
CREATE TABLE `contest_register` (
  `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `cid`          bigint(20) unsigned NOT NULL   COMMENT '比赛ID',
  `uid`          varchar(32)  NOT NULL          COMMENT '用户UUID',
  `status`       int(11)      DEFAULT 0         COMMENT '0正常 1失效',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cid_uid` (`cid`, `uid`),
  KEY `idx_uid` (`uid`),
  CONSTRAINT `fk_contest_reg_cid` FOREIGN KEY (`cid`) REFERENCES `contest`   (`id`)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_contest_reg_uid` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `user_acproblem`;
CREATE TABLE `user_acproblem` (
  `id`           bigint(20) NOT NULL AUTO_INCREMENT,
  `uid`          varchar(32) NOT NULL          COMMENT '用户UUID',
  `pid`          bigint(20) unsigned NOT NULL  COMMENT 'AC的题目ID',
  `submit_id`    bigint(20) unsigned NOT NULL  COMMENT '达成AC的提交ID',
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_pid` (`pid`),
  KEY `idx_submit_id` (`submit_id`),
  CONSTRAINT `fk_user_acprob_pid`    FOREIGN KEY (`pid`)       REFERENCES `problem` (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_acprob_submit` FOREIGN KEY (`submit_id`) REFERENCES `judge`   (`submit_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 六、训练域 (6 张)
-- ============================================================

DROP TABLE IF EXISTS `training_category`;
CREATE TABLE `training_category` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `name`         varchar(255) DEFAULT NULL COMMENT '分类名',
  `color`        varchar(255) DEFAULT NULL COMMENT '分类颜色',
  `gmt_create`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `training`;
CREATE TABLE `training` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `title`        varchar(255) DEFAULT NULL     COMMENT '题单名称',
  `description`  longtext                      COMMENT '题单简介',
  `author`       varchar(255) NOT NULL         COMMENT '创建者用户名',
  `auth`         varchar(255) NOT NULL         COMMENT '权限:Public/Private',
  `private_pwd`  varchar(255) DEFAULT NULL     COMMENT '私有题单密码',
  `rank`         int         DEFAULT 0         COMMENT '排序号(升序)',
  `status`       tinyint(1)  DEFAULT 1         COMMENT '0禁用 1正常',
  `gmt_create`   datetime    DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `mapping_training_category`;
CREATE TABLE `mapping_training_category` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `tid`          bigint unsigned NOT NULL COMMENT '训练题单ID',
  `cid`          bigint unsigned NOT NULL COMMENT '训练分类ID',
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tid` (`tid`),
  KEY `idx_cid` (`cid`),
  CONSTRAINT `fk_mtc_tid` FOREIGN KEY (`tid`) REFERENCES `training`          (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_mtc_cid` FOREIGN KEY (`cid`) REFERENCES `training_category` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `training_problem`;
CREATE TABLE `training_problem` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `tid`          bigint unsigned NOT NULL      COMMENT '训练题单ID',
  `pid`          bigint unsigned NOT NULL      COMMENT '题目ID',
  `rank`         int DEFAULT 0                 COMMENT '排序号',
  `display_id`   varchar(255) NOT NULL         COMMENT '展示ID',
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tid` (`tid`),
  KEY `idx_pid` (`pid`),
  KEY `idx_display_id` (`display_id`),
  CONSTRAINT `fk_tp_tid` FOREIGN KEY (`tid`) REFERENCES `training` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tp_pid` FOREIGN KEY (`pid`) REFERENCES `problem`  (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `training_record`;
CREATE TABLE `training_record` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `tid`          bigint unsigned NOT NULL      COMMENT '训练题单ID',
  `tpid`         bigint unsigned NOT NULL      COMMENT '训练题目ID',
  `pid`          bigint unsigned NOT NULL      COMMENT '题目ID',
  `uid`          varchar(255) NOT NULL         COMMENT '用户UUID',
  `submit_id`    bigint unsigned NOT NULL      COMMENT '提交ID',
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tid` (`tid`),
  KEY `idx_tpid` (`tpid`),
  KEY `idx_pid` (`pid`),
  KEY `idx_uid` (`uid`),
  KEY `idx_submit_id` (`submit_id`),
  CONSTRAINT `fk_tr_tid`    FOREIGN KEY (`tid`)       REFERENCES `training`          (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tr_tpid`   FOREIGN KEY (`tpid`)      REFERENCES `training_problem`  (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tr_pid`    FOREIGN KEY (`pid`)       REFERENCES `problem`          (`id`)        ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tr_uid`    FOREIGN KEY (`uid`)       REFERENCES `user_info`        (`uuid`)      ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tr_submit` FOREIGN KEY (`submit_id`) REFERENCES `judge`            (`submit_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `training_register`;
CREATE TABLE `training_register` (
  `id`           bigint unsigned NOT NULL AUTO_INCREMENT,
  `tid`          bigint unsigned NOT NULL      COMMENT '训练题单ID',
  `uid`          varchar(255) NOT NULL         COMMENT '用户UUID',
  `status`       tinyint(1) DEFAULT 1          COMMENT '0失效 1正常',
  `gmt_create`   datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tid` (`tid`),
  KEY `idx_uid` (`uid`),
  CONSTRAINT `fk_treg_tid` FOREIGN KEY (`tid`) REFERENCES `training`  (`id`)   ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_treg_uid` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 七、文件 (1 张)
-- ============================================================

DROP TABLE IF EXISTS `file`;
CREATE TABLE `file` (
  `id`          bigint(32) unsigned NOT NULL AUTO_INCREMENT,
  `uid`         varchar(32)  DEFAULT NULL       COMMENT '上传者UUID',
  `name`        varchar(255) NOT NULL           COMMENT '文件名',
  `suffix`      varchar(255) NOT NULL           COMMENT '后缀',
  `folder_path` varchar(255) NOT NULL           COMMENT '文件夹路径',
  `file_path`   varchar(255) DEFAULT NULL       COMMENT '文件绝对路径',
  `type`        varchar(255) DEFAULT NULL       COMMENT '文件类型(如avatar)',
  `delete`      tinyint(1)   DEFAULT 0          COMMENT '0正常 1已删除',
  `gmt_create`  datetime     DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  CONSTRAINT `fk_file_uid` FOREIGN KEY (`uid`) REFERENCES `user_info` (`uuid`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 八、触发器 & 事件 (比赛状态自动更新)
-- ============================================================

DROP TRIGGER IF EXISTS `contest_trigger`;
DELIMITER $$
CREATE TRIGGER `contest_trigger` BEFORE INSERT ON `contest` FOR EACH ROW
BEGIN
  SET NEW.status = (
    CASE
      WHEN NOW() < NEW.start_time THEN -1
      WHEN NOW() >= NEW.start_time AND NOW() < NEW.end_time THEN 0
      WHEN NOW() >= NEW.end_time THEN 1
    END
  );
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS `contest_status`;
DELIMITER $$
CREATE PROCEDURE `contest_status`()
BEGIN
  UPDATE contest
  SET status = (
    CASE
      WHEN NOW() < start_time THEN -1
      WHEN NOW() >= start_time AND NOW() < end_time THEN 0
      WHEN NOW() >= end_time THEN 1
    END
  );
END$$
DELIMITER ;

SET GLOBAL event_scheduler = 1;

DROP EVENT IF EXISTS `contest_event`;
DELIMITER $$
CREATE EVENT `contest_event`
ON SCHEDULE EVERY 1 SECOND
STARTS NOW()
ON COMPLETION PRESERVE
ENABLE
DO CALL contest_status()$$
DELIMITER ;


-- ============================================================
-- 九、种子数据
-- ============================================================

-- 角色 (4 个)
INSERT INTO `role` (`id`, `role`, `description`, `status`) VALUES
(00000000000000001000, 'root',           '超级管理员',   0),
(00000000000000001001, 'admin',          '普通管理员',   0),
(00000000000000001002, 'default_user',   '普通用户',     0),
(00000000000000001008, 'problem_admin',  '题目管理员',   0);

-- 权限 (6 个)
INSERT INTO `auth` (`id`, `name`, `permission`, `status`) VALUES
(1, 'problem',      'problem_admin',      0),
(2, 'submit',       'submit',             0),
(3, 'contest',      'contest_admin',      0),
(4, 'rejudge',      'rejudge',            0),
(5, 'user',         'user_admin',         0),
(6, 'system_info',  'system_info_admin',  0);

-- 角色-权限关联
-- root(1000): 所有权限
INSERT INTO `role_auth` (`auth_id`, `role_id`) VALUES
(1,1000),(2,1000),(3,1000),(4,1000),(5,1000),(6,1000);
-- admin(1001): 题目+比赛+重判+用户
INSERT INTO `role_auth` (`auth_id`, `role_id`) VALUES
(1,1001),(3,1001),(4,1001),(5,1001);
-- default_user(1002): 只有提交权限
INSERT INTO `role_auth` (`auth_id`, `role_id`) VALUES (2,1002);
-- problem_admin(1008): 只有题目管理权限
INSERT INTO `role_auth` (`auth_id`, `role_id`) VALUES (1,1008);

-- 语言 (9 种用户语言 + 4 种内部语言)
-- 用户语言
INSERT INTO `language` (`content_type`, `description`, `name`, `compile_command`, `template`, `code_template`, `is_spj`, `oj`, `seq`) VALUES
('text/x-csrc',   'GCC 9.4.0',   'C',          '/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=1 -std=c11 {src_path} -lm -o {exe_path}',        '#include <stdio.h>\nint main() {\n    int a,b;\n    scanf(\"%d %d\",&a,&b);\n    printf(\"%d\",a+b);\n    return 0;\n}', '//PREPEND BEGIN\n#include <stdio.h>\n//PREPEND END\n\n//TEMPLATE BEGIN\nint add(int a, int b) {\n  return ___________;\n}\n//TEMPLATE END\n\n//APPEND BEGIN\nint main() {\n  printf(\"%d\", add(1, 2));\n  return 0;\n}\n//APPEND END', 1, 'ME', 10),
('text/x-c++src', 'G++ 9.4.0',   'C++',        '/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=1 -std=c++14 {src_path} -lm -o {exe_path}',       '#include<iostream>\nusing namespace std;\nint main() {\n    int a,b;\n    cin >> a >> b;\n    cout << a + b;\n    return 0;\n}', '//PREPEND BEGIN\n#include <iostream>\nusing namespace std;\n//PREPEND END\n\n//TEMPLATE BEGIN\nint add(int a, int b) {\n  return ___________;\n}\n//TEMPLATE END\n\n//APPEND BEGIN\nint main() {\n  cout << add(1, 2);\n  return 0;\n}\n//APPEND END', 1, 'ME', 5),
('text/x-java',   'OpenJDK 1.8', 'Java',       '/bin/bash -c "javac -encoding utf-8 {src_path} && jar -cvf {exe_path} *.class"',                'import java.util.Scanner;\npublic class Main{\n    public static void main(String[] args){\n        Scanner in=new Scanner(System.in);\n        int a=in.nextInt();\n        int b=in.nextInt();\n        System.out.println((a+b));\n    }\n}', '//PREPEND BEGIN\nimport java.util.Scanner;\n//PREPEND END\n\npublic class Main{\n    //TEMPLATE BEGIN\n    public static Integer add(int a,int b){\n        return _______;\n    }\n    //TEMPLATE END\n\n    //APPEND BEGIN\n    public static void main(String[] args){\n        System.out.println(add(1,2));\n    }\n    //APPEND END\n}\n', 0, 'ME', 0),
('text/x-go',     'Golang 1.19', 'Go',         '/usr/bin/go build -o {exe_path} {src_path}',                                                   'package main\nimport \"fmt\"\n\nfunc main(){\n    var x int\n    var y int\n    fmt.Scanln(&x,&y)\n    fmt.Printf(\"%d\",x+y)\n}', '\npackage main\n\n//PREPEND BEGIN\nimport \"fmt\"\n//PREPEND END\n\n//TEMPLATE BEGIN\nfunc add(a,b int)int{\n    return ______\n}\n//TEMPLATE END\n\n//APPEND BEGIN\nfunc main(){\n    var x int\n    var y int\n    fmt.Printf(\"%d\",add(x,y))\n}\n//APPEND END\n', 0, 'ME', 0),
('text/x-csharp', 'C# Mono 4.6.2','C#',         '/usr/bin/mcs -optimize+ -out:{exe_path} {src_path}',                                        'using System;\nusing System.Linq;\n\nclass Program {\n    public static void Main(string[] args) {\n        Console.WriteLine(Console.ReadLine().Split().Select(int.Parse).Sum());\n    }\n}', '//PREPEND BEGIN\nusing System;\n//PREPEND END\n\nclass Solution\n{\n    //TEMPLATE BEGIN\n    static int add(int a,int b){\n        return _______;\n    }\n    //TEMPLATE END\n\n    //APPEND BEGIN\n    static void Main(string[] args)\n    {\n        Console.WriteLine(add(1,2));\n    }\n    //APPEND END\n}', 0, 'ME', 0),
('text/x-python', 'Python 3.7.5','Python3',     '/usr/bin/python3 -m py_compile {src_path}',                                                  'a, b = map(int, input().split())\nprint(a + b)', '//PREPEND BEGIN\n//PREPEND END\n\n//TEMPLATE BEGIN\ndef add(a, b):\n    return a + b\n//TEMPLATE END\n\n\nif __name__ == \'__main__\':\n    //APPEND BEGIN\n    a, b = 1, 1\n    print(add(a, b))\n    //APPEND END', 0, 'ME', 0),
('text/javascript', 'Node.js 14.19.0','JavaScript','/usr/bin/node {src_path}',                                                                'var readline = require(\'readline\');\nconst rl = readline.createInterface({\n        input: process.stdin,\n        output: process.stdout\n});\nrl.on(\'line\', function(line){\n   var tokens = line.split(\' \');\n    console.log(parseInt(tokens[0]) + parseInt(tokens[1]));\n});', NULL, 0, 'ME', 0),
('text/x-rustsrc', 'Rust 1.49.0', 'Rust',       '/usr/bin/rustc -O -o {exe_path} {src_path}',                                                 'use std::io;\nfn main() {\n    let mut line = String::new();\n    io::stdin().read_line(&mut line).unwrap();\n    let nums: Vec<i32> = line.split_whitespace().map(|x| x.parse().unwrap()).collect();\n    println!(\"{}\", nums[0] + nums[1]);\n}', '//PREPEND BEGIN\nuse std::io;\n//PREPEND END\n\n//TEMPLATE BEGIN\nfn add(a:i32, b:i32) -> i32 {\n    ______\n}\n//TEMPLATE END\n\n//APPEND BEGIN\nfn main() {\n    println!(\"{}\", add(1,2));\n}\n//APPEND END', 0, 'ME', 0),
('text/x-php',    'PHP 7.3.33',  'PHP',        '/usr/bin/php {src_path}',                                                                     '<?=array_sum(fscanf(STDIN, \"%d %d\"));', NULL, 0, 'ME', 0);

-- 内部语言 (SPJ/Interactive)
INSERT INTO `language` (`content_type`, `description`, `name`, `compile_command`, `is_spj`, `oj`, `seq`) VALUES
('text/x-csrc',   'GCC SPJ',   'SPJ-C',            '/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {src_path} -lm -o {exe_path}',   1, 'ME', 0),
('text/x-c++src', 'G++ SPJ',   'SPJ-C++',          '/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {src_path} -lm -o {exe_path}', 1, 'ME', 0),
('text/x-csrc',   'GCC Inter', 'INTERACTIVE-C',    '/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {src_path} -lm -o {exe_path}',   1, 'ME', 0),
('text/x-c++src', 'G++ Inter', 'INTERACTIVE-C++',  '/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {src_path} -lm -o {exe_path}', 1, 'ME', 0);

-- 默认 root 账号 (密码: root123, BCrypt)
INSERT INTO `user_info` (`uuid`, `username`, `password`, `nickname`, `status`) VALUES
('1', 'root', '$2a$12$K906bmODKIEdM5eHHz.r9enhnwpfeEpcqj/JyqU/6faW805U4n18G', '超级管理员', 0);

INSERT INTO `user_role` (`uid`, `role_id`) VALUES ('1', 00000000000000001000);


SET FOREIGN_KEY_CHECKS = 1;
