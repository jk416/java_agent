package com.aim.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecureRagService {

    private static final Logger log = LoggerFactory.getLogger(SecureRagService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public SecureRagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * 带租户权限隔离与相似度置信度阈值的安全 RAG 对话
     *
     * @param prompt   用户提问
     * @param tenantId 当前登录商家的租户 ID（用于数据行级隔离）
     * @param chatId   多轮会话 ID
     * @return 流式文本
     */
    public Flux<String> secureRagChatStream(String prompt, String tenantId, String chatId) {
        if (!StringUtils.hasText(prompt)) {
            return Flux.just("请输入有效的问题内容");
        }
        if (!StringUtils.hasText(tenantId)) {
            return Flux.just("【安全拦截】未获取到有效的商户租户凭据，禁止访问知识库");
        }

        // -------------------------------------------------------------------------
        // 【你的核心实战区域】：多租户元数据过滤与检索组装
        // 目标：
        // 1. 构建 SearchRequest：
        //    - query: prompt
        //    - topK: 3
        //    - 相似度阈值 withSimilarityThreshold(0.7)：过滤掉低于 0.7 相似度的低质量切片
        //    - 过滤表达式 withFilterExpression("tenantId == '" + tenantId + "'")：严格限制只搜索当前商户的数据
        if (!tenantId.matches("^[a-zA-Z0-9_-]+$")) {
            throw new SecurityException("非法租户标识");
        }
        SearchRequest request = SearchRequest
                .query(prompt)
                .withTopK(3)
                .withSimilarityThreshold(0.7)
                .withFilterExpression("tenantId == '" + tenantId + "'");
        // 2. 调用 vectorStore.similaritySearch(request) 检索出文档列表；
        List<Document> documents = vectorStore.similaritySearch(request);

        // 3. 判空防幻觉拦截：如果检索结果为空，直接返回安全提示；
        if (documents.isEmpty()) {
            return Flux.just("【安全拦截】没有找到相关文档，请检查输入问题或联系管理员以获取帮助。");
        }
        // 4. 采用生产最佳实践：把提取出的资料拼在 System Prompt 里，用户的问题传给 User Prompt；
        String systemPrompt = "请根据提供的文档内容回答用户问题，如果文档内容无法回答，请返回【文档中没有找到相关答案】。\n---\n文档内容：\n" + documents.stream().map(Document::getContent).collect(Collectors.joining("\n---\n"));
        log.info("systemPrompt: {}", systemPrompt);
        // 5. 挂载 chatId 会话记忆，返回流式推流。
        // -------------------------------------------------------------------------
        return chatClient.prompt().system(systemPrompt).user(prompt).advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)).stream().content().onErrorResume(e -> {
            log.error("AI 流式对话异常，prompt: {}", prompt, e);
            return Flux.just("\n[服务繁忙或调用出错，请稍后重试]");
        });
    }
}
