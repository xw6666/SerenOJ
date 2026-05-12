# SerenOJ 数据库表结构文档

## 概览

共 **21 张表**，分为 7 个域。

```
用户域 (5张)    题目域 (9张)    判题域 (2张)
比赛域 (5张)    训练域 (6张)    文件 (1张)
```

所有表使用 `utf8mb4` 字符集，`InnoDB` 引擎。

---

## 一、用户域 (5 张)

### 1.1 `user_info` — 用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| `uuid` | varchar(32) PK | 用户唯一标识(UUID) |
| `username` | varchar(100) UNIQUE | 登录用户名 |
| `password` | varchar(100) | 密码(MD5哈希) |
| `nickname` | varchar(100) | 昵称 |
| `school` | varchar(100) | 学校 |
| `course` | varchar(100) | 专业 |
| `number` | varchar(20) | 学号 |
| `realname` | varchar(100) | 真实姓名 |
| `gender` | varchar(20) | 性别(secrecy/male/female) |
| `github` | varchar(255) | GitHub 主页 |
| `blog` | varchar(255) | 博客地址 |
| `email` | varchar(320) UNIQUE | 邮箱 |
| `avatar` | varchar(255) | 头像 URL |
| `signature` | mediumtext | 个性签名 |
| `title_name` | varchar(255) | 头衔称号(管理员设置) |
| `title_color` | varchar(255) | 头衔颜色 |
| `status` | int | 0=正常, 1=封禁 |
| `gmt_create` | datetime | 创建时间 |
| `gmt_modified` | datetime | 修改时间(自动更新) |

---

### 1.2 `role` — 角色定义

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint(20) PK | 角色ID(zerofill) |
| `role` | varchar(50) | 角色标识符(root/admin/default_user/problem_admin) |
| `description` | varchar(100) | 角色描述 |
| `status` | tinyint | 0=可用, 1=禁用 |
| `gmt_create` | datetime | 创建时间 |
| `gmt_modified` | datetime | 修改时间 |

**种子数据：**

| ID | role | 说明 |
|----|------|------|
| 1000 | root | 超级管理员，全部权限 |
| 1001 | admin | 普通管理员，题目/比赛/用户管理 |
| 1002 | default_user | 普通用户，仅可提交代码 |
| 1008 | problem_admin | 题目管理员，仅管理题目 |

---

### 1.3 `auth` — 权限定义

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | 权限ID |
| `name` | varchar(100) | 权限名称 |
| `permission` | varchar(100) | 权限字符串(Shiro注解使用) |
| `status` | tinyint | 0=可用, 1=禁用 |

**种子数据：**

| ID | name | permission | 说明 |
|----|------|-----------|------|
| 1 | problem | problem_admin | 题目管理 |
| 2 | submit | submit | 提交代码 |
| 3 | contest | contest_admin | 比赛管理 |
| 4 | rejudge | rejudge | 重判 |
| 5 | user | user_admin | 用户管理 |
| 6 | system_info | system_info_admin | 系统信息管理 |

---

### 1.4 `role_auth` — 角色-权限关联 (多对多)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `auth_id` | bigint FK→auth.id | 权限ID |
| `role_id` | bigint FK→role.id | 角色ID |

**种子数据：**
- root(1000): 全部6个权限
- admin(1001): problem_admin, contest_admin, rejudge, user_admin
- default_user(1002): submit
- problem_admin(1008): problem_admin

---

### 1.5 `user_role` — 用户-角色关联 (多对多)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `uid` | varchar(32) FK→user_info.uuid | 用户ID |
| `role_id` | bigint FK→role.id | 角色ID |

---

## 二、编程语言 (1 张)

### 2.1 `language` — 编程语言定义

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | 语言ID |
| `content_type` | varchar(255) | CodeMirror 语法高亮类型(text/x-c++src等) |
| `description` | varchar(255) | 版本描述(如 "G++ 9.4.0") |
| `name` | varchar(255) | 语言名称(C++, Python3等) |
| `compile_command` | mediumtext | 编译命令(DB存储用,实际以language.yml为准) |
| `template` | longtext | A+B 示例代码 |
| `code_template` | longtext | 默认代码模板(编辑器初始代码) |
| `is_spj` | tinyint | 0=普通语言, 1=可作为SPJ/交互语言 |
| `oj` | varchar(255) | 所属OJ(固定 ME) |
| `seq` | int | 排序号(越小越靠前) |

**种子数据 (13种)：**

| 类别 | 语言 |
|------|------|
| 用户语言 (9种) | C, C++, Java, Go, C#, Python3, JavaScript, Rust, PHP |
| 内部语言 (4种) | SPJ-C, SPJ-C++, INTERACTIVE-C, INTERACTIVE-C++ |

---

## 三、题目域 (9 张)

### 3.1 `problem` — 题目主表

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | 题目ID(自增) |
| `problem_id` | varchar(255) | 题号(如 P1000) |
| `title` | varchar(255) | 标题 |
| `author` | varchar(255) | 作者(纯字符串,无FK约束) |
| `type` | int | 0=ACM, 1=OI |
| `time_limit` | int | 时间限制(ms,默认1000) |
| `memory_limit` | int | 内存限制(KB,默认65535→64MB) |
| `stack_limit` | int | 栈限制(MB,默认128) |
| `description` | longtext | 题目描述(Markdown/HTML) |
| `input` | longtext | 输入格式说明 |
| `output` | longtext | 输出格式说明 |
| `examples` | longtext | 题面样例(JSON格式) |
| `source` | text | 题目来源(如"NOIP 2023") |
| `difficulty` | int | 难度：0=简单, 1=中等, 2=困难 |
| `hint` | longtext | 提示/备注 |
| `auth` | int | 1=公开, 2=私有, 3=比赛专用 |
| `io_score` | int | OI题目总分(默认100) |
| `code_share` | tinyint | 用户代码是否可被他人查看(默认1) |
| `judge_mode` | varchar(255) | 判题模式: default / spj / interactive |
| `judge_case_mode` | varchar(255) | 样例模式: default / subtask_lowest / subtask_average |
| `user_extra_file` | mediumtext | 用户需额外提供的文件(JSON) |
| `judge_extra_file` | mediumtext | 判题需额外提供的文件(JSON) |
| `spj_code` | longtext | SPJ/交互程序源代码 |
| `spj_language` | varchar(255) | SPJ程序语言(SPJ-C / SPJ-C++等) |
| `is_remove_end_blank` | tinyint | 是否去除文末空格(默认1) |
| `open_case_result` | tinyint | 是否公开测试点详情(默认1) |
| `is_upload_case` | tinyint | 测试数据是否上传为文件(默认1) |
| `case_version` | varchar(40) | 测试数据版本号 |
| `modified_user` | varchar(255) | 最后修改者用户名 |
| `is_file_io` | tinyint | 是否文件IO模式(默认0) |
| `io_read_file_name` | varchar(255) | 输入文件名(file io模式) |
| `io_write_file_name` | varchar(255) | 输出文件名(file io模式) |
| `gmt_create` | datetime | 创建时间 |
| `gmt_modified` | datetime | 修改时间 |

> **判题模式说明：**
> - `default`：标准输出比对(MD5)
> - `spj`：Special Judge，运行特判程序比较输出
> - `interactive`：交互题，用户程序与交互程序通过管道通信
>
> **样例模式说明：**
> - `default`：遇到第一个错误即停止
> - `subtask_lowest`：按子任务组取最低分(OI)
> - `subtask_average`：按子任务组取平均分(OI)

---

### 3.2 `problem_case` — 测试用例

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `pid` | bigint FK→problem.id | 题目ID |
| `input` | longtext | 测试输入 |
| `output` | longtext | 期望输出 |
| `score` | int | 该测试点分值(OI模式) |
| `status` | int | 0=正常, 1=禁用 |
| `group_num` | int | subtask分组号(默认1) |
| `gmt_create` | datetime | |
| `gmt_modified` | datetime | |

---

### 3.3 `tag_classification` — 标签分类

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `name` | varchar(255) | 分类名(如"算法"、"数据结构"、"入门") |
| `oj` | varchar(255) | 所属OJ(ME) |
| `rank` | int zerofill | 排序优先级(越小越前) |
| `gmt_create` | datetime | |
| `gmt_modified` | datetime | |

---

### 3.4 `tag` — 题目标签

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `name` | varchar(255) | 标签名(如"动态规划"、"DFS") |
| `color` | varchar(10) | 标签颜色(如 #FF5722) |
| `oj` | varchar(255) | 所属OJ(ME) |
| `tcid` | bigint FK→tag_classification.id | 所属分类 |
| `gmt_create` | datetime | |
| `gmt_modified` | datetime | |

UNIQUE(`name`, `oj`) 确保同OJ下标签名唯一。

---

### 3.5 `problem_tag` — 题目-标签关联 (多对多)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `pid` | bigint FK→problem.id | |
| `tid` | bigint FK→tag.id | |

---

### 3.6 `problem_language` — 题目-语言关联 (多对多)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `pid` | bigint FK→problem.id | |
| `lid` | bigint FK→language.id | |

> 每个题目可指定允许哪些语言提交。如果不指定，默认允许所有 ME 语言。

---

### 3.7 `code_template` — 代码模板

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | int PK | |
| `pid` | bigint FK→problem.id | |
| `lid` | bigint FK→language.id | |
| `code` | longtext | 该题该语言的默认代码模板 |
| `status` | tinyint | 0=正常, 1=禁用 |

> 每题每种语言可设置一个默认模板，用户打开编辑器时自动填充。

---

### 3.8 `problem_count` — 题目提交统计 (物化计数)

| 字段 | 类型 | 说明 |
|------|------|------|
| `pid` | bigint PK/FK→problem.id | 题目ID(主键,一一对应) |
| `total` | int | 总提交数 |
| `ac` | int | Accepted 数 |
| `wa` | int | Wrong Answer 数 |
| `tle` | int | Time Limit Exceeded 数 |
| `mle` | int | Memory Limit Exceeded 数 |
| `re` | int | Runtime Error 数 |
| `pe` | int | Presentation Error 数 |
| `ce` | int | Compile Error 数 |
| `se` | int | System Error 数 |
| `pa` | int | Partial Accepted 数(OI) |
| `version` | bigint | 乐观锁版本号 |

> 每次判题完成后，异步更新对应题目的统计计数。用于题目列表页展示通过率。

---

## 四、判题域 (2 张)

### 4.1 `judge` — 提交记录

| 字段 | 类型 | 说明 |
|------|------|------|
| `submit_id` | bigint PK | 提交ID(自增) |
| `pid` | bigint FK→problem.id | 题目ID |
| `display_pid` | varchar(255) | 题目展示ID(如 P1000) |
| `uid` | varchar(32) FK→user_info.uuid | 提交者UUID |
| `username` | varchar(255) | 提交者用户名 |
| `submit_time` | datetime | 提交时间 |
| `status` | int | 判题结果码(见下方) |
| `share` | tinyint | 0=仅自己可见, 1=公开 |
| `error_message` | mediumtext | 编译错误/运行时错误信息 |
| `time` | int | 最大运行时间(ms) |
| `memory` | int | 最大运行内存(KB) |
| `score` | int | OI得分 |
| `length` | int | 代码长度(字节) |
| `code` | longtext | 源代码 |
| `language` | varchar(255) | 编程语言名称 |
| `cid` | bigint | 比赛ID(0=非比赛提交) |
| `cpid` | bigint | 比赛中题目排序ID |
| `judger` | varchar(20) | 判题机标识 |
| `ip` | varchar(64) | 提交者IP |
| `oi_rank_score` | int | OI排行榜得分 |

**判题状态码：**

| 值 | 状态 | 说明 |
|----|------|------|
| -10 | Not Submitted | 未提交 |
| -4 | Cancelled | 已取消 |
| -3 | Presentation Error | 格式错误 |
| -2 | Compile Error | 编译错误 |
| -1 | Wrong Answer | 答案错误 |
| 0 | Accepted | 通过 |
| 1 | Time Limit Exceeded | 超时 |
| 2 | Memory Limit Exceeded | 超内存 |
| 3 | Runtime Error | 运行错误 |
| 4 | System Error | 系统错误 |
| 5 | Pending | 等待判题 |
| 6 | Compiling | 编译中 |
| 7 | Judging | 判题中 |
| 8 | Partial Accepted | 部分通过(OI) |
| 9 | Submitting | 提交中(远程OJ) |
| 10 | Submit Failed | 提交失败(远程OJ) |

---

### 4.2 `judge_case` — 单测试点结果

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `submit_id` | bigint FK→judge.submit_id | 提交ID |
| `uid` | varchar(32) | 用户UUID |
| `pid` | bigint | 题目ID |
| `case_id` | bigint | 测试点ID |
| `status` | int | 该测试点结果码 |
| `time` | int | 耗时(ms) |
| `memory` | int | 内存(KB) |
| `score` | int | OI该测试点得分 |
| `group_num` | int | subtask分组号 |
| `seq` | int | 测试点序号 |
| `mode` | varchar(255) | 评测模式 |
| `input_data` | longtext | 测试点输入(赛后可见) |
| `output_data` | longtext | 期望输出(赛后可见) |
| `user_output` | longtext | 用户实际输出(赛后可见) |

> 每条 judge 记录对应多条 judge_case。比赛进行时 input_data/output_data/user_output 不返回给前端。

---

## 五、比赛域 (5 张)

### 5.1 `contest` — 比赛

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | 比赛ID |
| `uid` | varchar(32) FK→user_info.uuid | 创建者 |
| `author` | varchar(255) | 创建者用户名 |
| `title` | varchar(255) | 比赛标题 |
| `type` | int | 0=ACM(罚时制), 1=OI(分数制) |
| `description` | longtext | 比赛说明(Markdown) |
| `source` | int | 0=原创, 非0=克隆源ID |
| `auth` | int | 0=公开, 1=私有(需密码访问), 2=保护(需密码提交) |
| `pwd` | varchar(255) | 密码 |
| `start_time` | datetime | 开始时间 |
| `end_time` | datetime | 结束时间 |
| `duration` | bigint | 时长(秒) |
| `seal_rank` | tinyint | 是否封榜 |
| `seal_rank_time` | datetime | 封榜时间(此后不刷新排名) |
| `auto_real_rank` | tinyint | 结束后自动解封(默认1) |
| `status` | int | -1=未开始, 0=进行中, 1=已结束(触发器自动维护) |
| `visible` | tinyint | 是否可见(0=隐藏) |
| `rank_show_name` | varchar(20) | 排名显示: username / nickname / realname |
| `oi_rank_score_type` | varchar(255) | OI计分: Recent(取最近) / Highest(取最高) |
| `allow_end_submit` | tinyint | 是否允许赛后补交(默认0) |

> `contest` 表有 `BEFORE INSERT` 触发器自动计算 `status`，另有 MySQL Event `contest_event` 每秒执行一次存储过程 `contest_status()` 更新所有比赛的 `status`。

---

### 5.2 `contest_problem` — 比赛题目

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `display_id` | varchar(255) | 比赛内显示ID(如 A/B/C/D 或 1/2/3) |
| `cid` | bigint FK→contest.id | 比赛ID |
| `pid` | bigint FK→problem.id | 题目ID |
| `display_title` | varchar(255) | 比赛内显示的标题(可不同于原题) |
| `color` | varchar(255) | 气球颜色(ACM发气球用) |

UNIQUE(`display_id`, `cid`, `pid`) 确保同一比赛内 display_id 唯一。

---

### 5.3 `contest_record` — 比赛提交记录 (每题每个用户一条)

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `cid` | bigint FK→contest.id | 比赛ID |
| `uid` | varchar(255) FK→user_info.uuid | 用户UUID |
| `pid` | bigint FK→problem.id | 题目ID |
| `cpid` | bigint FK→contest_problem.id | 比赛题目ID |
| `username` | varchar(255) | 用户名 |
| `realname` | varchar(255) | 真实姓名 |
| `display_id` | varchar(255) | 比赛展示ID |
| `submit_id` | bigint FK→judge.submit_id | 提交ID(用于重判) |
| `status` | int | 0=未AC无罚时, 1=AC, -1=未AC有罚时 |
| `submit_time` | datetime | 提交时间 |
| `time` | bigint | 距比赛开始的秒数(用于计算罚时) |
| `score` | int | OI得分 |
| `use_time` | int | 运行耗时 |
| `first_blood` | tinyint | (废弃) |
| `checked` | tinyint | AC是否经管理员审核 |

> 每次比赛提交的判题结果，同时写入 `judge` 表和 `contest_record` 表。排名通过聚合 `contest_record` 表计算。

---

### 5.4 `contest_register` — 比赛报名

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `cid` | bigint FK→contest.id | 比赛ID |
| `uid` | varchar(32) FK→user_info.uuid | 用户UUID |
| `status` | int | 0=正常, 1=失效 |

UNIQUE(`cid`, `uid`) 确保每场比赛每人只报一次名。

---

### 5.5 `user_acproblem` — 用户AC记录

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `uid` | varchar(32) | 用户UUID |
| `pid` | bigint FK→problem.id | 题目ID |
| `submit_id` | bigint FK→judge.submit_id | 达成AC的提交ID |

> 非比赛提交获得 AC 后，自动写入此表。用于题目列表页标记"已通过"状态。

---

## 六、训练域 (6 张)

### 6.1 `training_category` — 训练分类

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `name` | varchar(255) | 分类名(如"基础"、"提高"、"省选") |
| `color` | varchar(255) | 分类颜色 |

---

### 6.2 `training` — 训练题单

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `title` | varchar(255) | 题单名称 |
| `description` | longtext | 题单简介 |
| `author` | varchar(255) | 创建者用户名 |
| `auth` | varchar(255) | Public(公开) / Private(需密码+注册) |
| `private_pwd` | varchar(255) | 私有题单密码 |
| `rank` | int | 排序号(升序, 越小越靠前) |
| `status` | tinyint | 0=禁用, 1=正常 |

---

### 6.3 `mapping_training_category` — 题单-分类关联

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `tid` | bigint FK→training.id | 题单ID |
| `cid` | bigint FK→training_category.id | 分类ID |

---

### 6.4 `training_problem` — 题单题目

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `tid` | bigint FK→training.id | 题单ID |
| `pid` | bigint FK→problem.id | 题目ID |
| `rank` | int | 排序号(升序) |
| `display_id` | varchar(255) | 展示ID(如 T1, T2) |

---

### 6.5 `training_record` — 训练提交记录

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `tid` | bigint FK→training.id | 题单ID |
| `tpid` | bigint FK→training_problem.id | 题单题目ID |
| `pid` | bigint FK→problem.id | 题目ID |
| `uid` | varchar(255) FK→user_info.uuid | 用户UUID |
| `submit_id` | bigint FK→judge.submit_id | 提交ID |

> 用户在训练题单中的每次提交，写入此表。用于追踪训练进度。

---

### 6.6 `training_register` — 训练注册

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `tid` | bigint FK→training.id | 题单ID |
| `uid` | varchar(255) FK→user_info.uuid | 用户UUID |
| `status` | tinyint | 0=失效, 1=正常 |

> 私有题单需注册（输入密码）后方可访问。

---

## 七、文件 (1 张)

### 7.1 `file` — 文件管理

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | bigint PK | |
| `uid` | varchar(32) FK→user_info.uuid | 上传者UUID(删除用户后置NULL) |
| `name` | varchar(255) | 文件名 |
| `suffix` | varchar(255) | 后缀 |
| `folder_path` | varchar(255) | 文件夹路径(相对) |
| `file_path` | varchar(255) | 文件绝对路径 |
| `type` | varchar(255) | 类型(avatar / testcase / markdown 等) |
| `delete` | tinyint | 0=正常, 1=已删除(软删除) |

> 用于追踪头像、题目中 Markdown 图片、上传的测试数据等文件。

---

## 八、实体关系图 (简化)

```
user_info ───── user_role ───── role ───── role_auth ───── auth
  │                │
  │                └── user_acproblem
  │
  ├── judge ───────── judge_case
  │    │
  │    ├── contest_record ─── contest ─── contest_problem
  │    │       │                              │
  │    │       └── contest_register          problem
  │    │                                        │
  │    ├── training_record ─── training_problem ─┤
  │    │       │                  │               │
  │    │       ├── training ───────┘     problem_case
  │    │       │     │                   problem_tag ─── tag ─── tag_classification
  │    │       │     └── mapping_training_category  problem_language
  │    │       │              │                  code_template
  │    │       └── training_category              problem_count
  │    │
  │    └── file
  │
  └── contest (owner)
```

---

## 九、数据库特殊对象

### 触发器 `contest_trigger`
- 时机：`BEFORE INSERT ON contest`
- 作用：插入比赛时根据 `start_time`/`end_time` 自动计算 `status`

### 存储过程 `contest_status`
- 作用：遍历所有比赛，按当前时间更新 `status` 字段

### 事件 `contest_event`
- 调度：每 1 秒执行一次
- 调用：`CALL contest_status()`
- 依赖：需开启 MySQL 事件调度器 (`SET GLOBAL event_scheduler = 1`)
