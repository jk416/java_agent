# AIM-AI-Hub：Java + AI 应用架构转型 30 天实战计划与进度追踪

> **提示**：本文档为本项目的**总纲与执行状态锚点**。当上下文窗口重置、开启新会话或切换 AI 时，只需读取本项目根目录的 `AGENTS.md` 与本文档，即可无缝接续当前学习与编码进度。

---

## 一、 背景与角色定位

- **学员背景**：周子龙（8年经验资深 Java 后端开发，精通 Spring Boot、Spring Cloud Alibaba、MySQL/PostgreSQL、Redis、高并发架构）。
- **转型定位**：**资深企业级 AI 应用架构师 / Java 大模型工程化专家**。
- **业务土壤**：以简历中的经历为基础，构建 **AIM 智能营销与商家服务 Agent 平台（AIM-AI-Hub）**。
- **核心策略**：
  - 以 8 年 Java 微服务高可用架构为底座；
  - 深度融合 **Spring AI Alibaba + PgVector + RAG + Agent (Function Calling/MCP) + LLMOps**；
  - 顺带将 **JVM 调优、JUC 并发线程模型、分布式锁、Redis 高级特性、MQ** 等大厂高频八股与 AI 实战深度串联打通。

---

## 二、 交互契约核心法则（必须严格遵守）

1. **单步推进**：一次只推进一个小关卡，严禁单次灌输过多内容。
2. **业务本质先行**：从真实电商后台业务场景出发解释“这功能解决什么现实痛点”。
3. **经典 Java 架构类比优先**：遇到 AI 新概念，先映射到熟悉的 Spring、MyBatis、RPC、事务或状态机概念。
4. **明确位置**：具体到类文件路径与方法名。
5. **先让用户写，再 Review**：严禁主动贴出大段完整实现。
6. **Review 严格三段式**：肯定写对的 → 指出生产 Bug/缺陷（含链路追踪） → 给出下一步。
7. **吃透三层标准**：原理层、实践层、权衡层。
8. **节奏控制**：用户明确说**“开始下一关”**后，才推进下一关！

---

## 三、 30 天实战任务路线图与当前进度追踪

### 阶段一：大模型交互与长连接工程化（Day 1 ~ Day 7）
- [x] **第 1 关：搭建骨架与打通 SSE 流式推流通道**
  - **落地文件**：`src/main/java/com/aim/ai/controller/AiChatController.java`
  - **核心产出**：`Flux<String>` 响应式流，`text/event-stream` 协议声明，入参防御与 `.onErrorResume` 优雅降级。
  - **八股串联**：SSE vs WebSocket 选型权衡、Tomcat 异步非阻塞线程模型。
- [x] **第 2 关：多轮会话记忆管理（Chat Memory）与滑动窗口**
  - **落地文件**：`src/main/java/com/aim/ai/config/AiConfig.java`、`AiChatController.java`
  - **核心产出**：独立 `ChatMemory` Bean 注册，`MessageChatMemoryAdvisor` AOP 环绕增强，前端会话 `chatId` 上下文绑定。
  - **八股串联**：`ConcurrentHashMap` 内存泄露与 JVM 老年代 Full GC 风险、Redis List 滑动窗口与 BigKey 防护。

---

### 阶段二：企业级生产 RAG 知识库全链路（Day 8 ~ Day 15）
- [x] **第 3 关：文档工程与智能切分（Chunking）**
  - **落地文件**：`src/main/java/com/aim/ai/rag/DocumentService.java`
  - **核心产出**：`TokenTextSplitter` 文档切分，重叠窗口（Overlap）防御语义割裂。
  - **八股串联**：文档切分策略（固定 Token vs 语义切分）、年轻代 GC 压力防范。
- [x] **第 4 关：向量化存储（Embedding & VectorStore 实践）**
  - **落地文件**：`src/main/java/com/aim/ai/rag/VectorStoreService.java`、`AiConfig.java`
  - **核心产出**：`EmbeddingModel` 文本转向量，`SimpleVectorStore` 向量检索与 `SearchRequest` Top-K。
  - **八股串联**：向量索引算法（HNSW vs IVFFlat）、向量相似度度量（余弦相似度 vs 欧氏距离）。
- [x] **第 5 关：RAG 检索增强组装与流式回答（RagChatService）**
  - **落地文件**：`src/main/java/com/aim/ai/rag/RagChatService.java`
  - **核心产出**：从向量库检索上下文，装配 System/User 角色 Prompt 模板，打通流式与记忆闭环。
  - **八股串联**：大模型角色权限划分（System vs User）、Prompt 注入防御、BM25 + Dense 双路混合检索。
- [x] **第 6 关：企业级 RAG 进阶——多租户权限隔离（Metadata Filter）与 Rerank 机制**
  - **落地文件**：`src/main/java/com/aim/ai/rag/SecureRagService.java`
  - **核心产出**：元数据过滤（Metadata Filter）多租户行级隔离，相似度置信度截断（0.7），防幻觉拦截。
  - **八股串联**：向量前过滤（Pre-filtering）vs 后过滤（Post-filtering）、Bi-Encoder vs Cross-Encoder（Rerank 重排）。

---

### 阶段三：Agent 智能体工作流与工具调用（Day 16 ~ Day 23）
- [x] **第 7 关：Function Calling（工具调用）实战**
  - **落地文件**：`src/main/java/com/aim/ai/agent/AgentChatService.java`、`PromotionService.java`
  - **核心产出**：通过方法引用（`::`）挂载工具，打通大模型意图识别、参数序列化与 Spring Bean 动态反射执行。
  - **八股串联**：Function Calling 两阶段调用底层全流程、工具死循环防御、资金高危工具的 Human-in-the-loop。
- [x] **第 8 关：工具调用的参数容错与分布式锁幂等**
  - **落地文件**：`src/main/java/com/aim/ai/agent/IdempotentPromotionService.java`
  - **核心产出**：参数自愈校验拦截，Redis 分布式锁防重幂等，UUID 唯一标识与防误删思想。
  - **八股串联**：Redis SETNX 误删锁缺陷、Lua 脚本原子释放、Redisson 看门狗续期与 Hash 可重入机制。
- [ ] **第 9 关：MCP（Model Context Protocol）协议落地** 👈 **【当前进行中】**
  - **目标文件**：`src/main/java/com/aim/ai/mcp/MerchantAuditMcpService.java`
  - **核心技术**：理解 MCP 客户端/服务端架构，将商户入驻资质状态机封装为标准的 MCP Tool 与 Resource 协议规范。
  - **八股串联**：MCP vs Function Calling 的本质区别、AI 时代的“JDBC”标准化协议。

---

### 阶段四：高可用工程化护城河与简历突击（Day 24 ~ Day 30）
- [ ] **第 10 关：多模型熔断降级与 Redis 令牌桶限流（RPM/TPM）**
  - **核心技术**：主备模型灾备切换（DeepSeek 超时降级 Qwen），Redis + Lua 分布式限流防击穿。
- [ ] **第 11 关：Langfuse 链路可观测性接入**
  - **核心技术**：Trace 链路追踪、Token 消耗与 Latency 监控。
- [ ] **第 12 关：简历深度精修与 30 题全真模拟面试**
  - **核心产出**：输出高通过率的最新简历，进行求职批量投递。

---

## 四、 面试真题复盘索引
所有产出的面试题与八股解析均收录在：
👉 [docs/INTERVIEW_QUESTIONS.md](file:///Users/jk416/run/docs/INTERVIEW_QUESTIONS.md)
