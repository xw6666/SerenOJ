# 代码查重系统设计文档

## 一、问题定义

比赛结束后，对同一题的所有提交两两比较，检测相似度异常高的代码对，标记疑似作弊。

**输入：** 同一题目下 N 份源代码
**输出：** 相似度矩阵 + 高出阈值的疑似作弊对列表

---

## 二、为什么「字符串 diff」不行

```c
// 代码 A                           // 代码 B
int main() {                        int main() {
    int n, sum = 0;                     int N, total = 0;
    scanf("%d", &n);                    scanf("%d", &N);
    for (int i = 1; i <= n; i++)        for (int i = 1; i <= N; i++)
        sum += i;                           total += i;
    printf("%d\n", sum);                printf("%d\n", total);
}                                   }
```

这两段代码**语义完全相同**，只是变量名不同。字符串 diff（Levenshtein 距离）会认为差异很大。需要**结构比对**。

---

## 三、两阶段检测架构

```
Phase 1: Winnowing 指纹 (快速筛选)
  N 份代码 → 每份提取 ~20 个指纹
  → 倒排索引映射 指纹→代码列表
  → 共享指纹数 > 阈值的代码对 → 进入 Phase 2
  → 复杂度: O(N) 指纹提取 + O(M) 倒排查找 (M=总指纹数)

Phase 2: AST 树编辑距离 (精确计算)
  候选对 → 各自解析为 AST → 归一化 → 树编辑距离
  → 相似度 = 1 - (编辑距离 / 较大树的节点数)
  → 复杂度: O(T²) 每对 (T=节点数)，但候选对数量已大幅减少
```

### 为什么分两阶段

- Phase 1 的 Winnowing 把 N 份代码里**大部分无关联**的对在 O(N) 时间内排除了
- Phase 2 的 AST 树编辑距离很准但很慢（O(n³)，用优化算法降到 O(n²)），只对少数候选对计算
- 一场比赛 300 人 × 8 题，Winnowing 后每题的候选对通常 < 50 对（而不是 300×299/2 = 44850 对全量比较）

---

## 四、Phase 1 详解：Winnowing 指纹算法

Winnowing 是斯坦福 MOSS 系统使用的算法，核心思想：

### 4.1 预处理：代码归一化

```
原始代码 → 去除注释和字符串 → 统一空白符 → 转小写
  → 得到「骨架字符串」
```

注意：**不去掉变量名**（留到 Phase 2 的 AST 阶段处理），只去掉明确无关的部分。

### 4.2 k-gram 切分 + 哈希

把骨架字符串切成长度为 k 的子串（k 一般取 5）：

```
骨架: "intmainscanf"
k=5:  [intma][ntmai][tmai1][main1]...
```

对每个 k-gram 求哈希（如 Rolling Hash，保证相邻 gram 的哈希能 O(1) 递推）：

```
gram:  intma  ntmai  tmain  main1  ain1s  ...
hash:  48291  73104  20593  66271  33845  ...
```

### 4.3 Winnowing 窗口选指纹

在大小为 w 的滑动窗口内，选哈希值最小的 gram 作为代表指纹：

```
窗口大小 w=4:
[48291, 73104, 20593, 66271] → 选 20593
[73104, 20593, 66271, 33845] → 选 20593（和上一个重复，不重复记录）
[20593, 66271, 33845, 91742] → 选 20593
...
```

**关键性质（保证检测到重合段）：** 两段代码只要有长度 ≥ k + w - 1 的相同子串，Winnowing **一定**会产生至少一个相同指纹。这是可证明的。

### 4.4 倒排索引匹配

```
指纹 → 包含该指纹的代码列表
20593 → [submission_1, submission_7, submission_23]
73104 → [submission_7]
...

统计每对代码共享的指纹数：
(sub_1, sub_7): 共享 12 个指纹 → 候选
(sub_1, sub_23): 共享 3 个指纹 → 不候选
```

阈值：共享指纹数 ≥ 代码指纹总数的 30% 才进入 Phase 2。

---

## 五、Phase 2 详解：AST 树编辑距离

### 5.1 解析为 AST

```
int main() {                          FunctionDef
    int n, sum = 0;                   ├── name: "main"
    scanf("%d", &n);                  ├── body:
    for (...) sum += i;                   ├── DeclStmt: int n, sum=0
    printf(...);                           ├── CallExpr: scanf(...)
}                                          ├── ForLoop: ...
                                           └── CallExpr: printf(...)
```

用 tree-sitter 做解析（支持多语言，比 javalang/antlr 快 10-50 倍）。

### 5.2 AST 归一化

把变量名全部替换为占位符，消除命名差异：

```
原 AST:  DeclStmt(type=int, name="n", init=0)
归一化:  DeclStmt(type=int, name="$VAR1", init=0)
```

- 变量名 → `$VAR1`, `$VAR2`, ...
- 函数名保留（调用 `scanf` 和调用 `printf` 的本质不同）
- 字面量 → 按类型归一化（整数 → `$INT`, 字符串 → `$STR`）

### 5.3 树编辑距离（Tree Edit Distance, TED）

**定义：** 把树 A 变成树 B 的最小操作数（插入节点、删除节点、替换节点），每操作代价为 1。

```
相似度 = 1 - TED(A, B) / max(sizeof(A), sizeof(B))
```

例如：
- 两棵完全相同的树：TED=0，相似度=1.0
- 一棵 100 节点的树变成另一棵需要 20 步编辑：相似度=0.8
- 作弊代码通常相似度 > 0.85

**算法：** Zhang-Shasha 是经典算法 O(n⁴)，实用中 RTED (Robust TED) 可以降到 O(n²) 平均。

### 5.4 阈值判断

| 相似度 | 判定 |
|--------|------|
| ≥ 0.95 | 几乎确定抄袭 |
| 0.85 ~ 0.95 | 疑似抄袭，人工审核 |
| 0.70 ~ 0.85 | 可能借鉴（留档不告警） |
| < 0.70 | 无关联 |

---

## 六、工程实现要点

### 6.1 tree-sitter 集成

不需要额外服务，tree-sitter 有 C 核心 + 各语言语法文件，通过 JNI 或 tree-sitter-java 绑定调用。或者直接用 command-line 调用生成 JSON AST。

### 6.2 性能估算

一场 ACM 比赛：300 人 × 10 题，每题取最近一次提交 = 3000 份代码。

```
Phase 1 (Winnowing):
  代码预处理: 3000 份 × 平均 500 字符 = 1.5MB → < 1 秒
  k-gram 哈希: 3000 × 300 × O(1) = 90 万次 → < 1 秒
  倒排查找: 每个指纹平均出现在 3-5 份代码中 → < 1 秒
  总计: < 3 秒

Phase 2 (AST 树编辑距离):
  候选对: 每题约 30-50 对
  AST 解析: 50 × 10 题 × 0.05 秒 = 25 秒
  TED 计算: 50 × 10 × O(平均50节点²) = 约 5 秒
  总计: < 30 秒
```

整场 300 人比赛查重，**30 秒内出结果**。

### 6.3 存储设计

查重结果不存 MySQL（太碎），存 JSON 文件或专门一张轻量表：

```sql
CREATE TABLE plagiarism_result (
  id        bigint PK,
  cid       bigint NOT NULL,       -- 比赛ID
  pid       bigint NOT NULL,       -- 题目ID
  uid_a     varchar(32) NOT NULL,  -- 用户A
  uid_b     varchar(32) NOT NULL,  -- 用户B
  submit_a  bigint NOT NULL,       -- 提交A的ID
  submit_b  bigint NOT NULL,       -- 提交B的ID
  similarity decimal(5,4),         -- 相似度 0.0000 ~ 1.0000
  status    int DEFAULT 0,         -- 0=待审 1=确认 2=误报
  gmt_create datetime
);
```

### 6.4 触发方式

- 比赛结束后管理员手动触发「代码查重」按钮
- 后台异步执行（@Async），不阻塞请求
- 执行结果通过 WebSocket 或轮询展示进度条

---

## 七、与现有判题系统的接入点

```
JudgeTaskConsumer 判题完成后
  └── 比赛提交 → 插入 contest_record

比赛结束后，管理员点击「查重」
  └── PlagiarismService.check(cid, pid)
        ├── 从 judge 表拉取该题所有提交的 code
        ├── Phase 1: Winnowing 指纹筛选候选对
        ├── Phase 2: AST 树编辑距离精确计算
        ├── 结果写入 plagiarism_result 表
        └── 返回疑似作弊对列表

管理员审核
  ├── 确认 → 标记该用户的比赛成绩
  └── 误报 → 不处理
```

---

## 八、逐步实现规划

| 阶段 | 内容 | 工作量 |
|------|------|--------|
| Step 1 | Winnowing 算法核心（k-gram + 哈希 + 窗口选指纹 + 倒排索引） | 1 天 |
| Step 2 | tree-sitter 集成（C/C++/Java 三种语言的 AST 解析 + 归一化） | 1.5 天 |
| Step 3 | 树编辑距离算法（RTED 或简化版的 Zhang-Shasha） | 1.5 天 |
| Step 4 | Service + Controller + 异步执行 + 结果存储 | 1 天 |
| Step 5 | 前端结果展示页面（抄袭对列表、代码 diff 高亮） | 1 天 |
| **合计** | | **5 天** |

---

## 九、简化启动方案（MVP）

如果嫌 AST 复杂，可以先上纯 Winnowing 版本：

1. 只做 Phase 1（Winnowing 指纹 + 倒排索引）
2. 共享指纹比例直接作为相似度分数
3. 对不同语言调各自的阈值
4. C/C++ 的 Winnowing 准确率已经 > 85%（MOSS 论文数据）

然后逐步加 AST 作为 Phase 2 提升准确率。这样**3 天能出一个可用的 MVP**。
