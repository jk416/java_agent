package com.aim.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);

    private final ChatClient chatClient;

    private final VectorStoreService vectorStoreService;

    public RagChatService(ChatClient chatClient, VectorStoreService vectorStoreService) {
        this.chatClient = chatClient;
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * RAG 知识库检索增强对话（流式）
     *
     * @param prompt 用户的提问
     * @param chatId 会话唯一标识
     * @return 打字机流式事件
     */
    public Flux<String> ragChatStream(String prompt, String chatId) {
        if (!StringUtils.hasText(prompt)) {
            return Flux.just("请输入有效的问题内容");
        }

        // 1. 从向量库检索 Top 2 最相关的参考规章条款
        List<Document> documents = vectorStoreService.searchSimilar(prompt, 2);

        // -------------------------------------------------------------------------
        // 【你的核心实战区域】：请在此处实现 RAG 核心装配逻辑
        // 目标：
        // 1. 如果 documents 为空，如何进行防幻觉拦截（或直接返回友好提示）？
        if (documents.isEmpty()) {
            return Flux.just("没有找到相关文档，请重新输入");
        }
        // 2. 将 documents 中的每一项文本提取出来，拼成一段完整的 context 上下文（用换行隔开）；
        // 1. 提取文档内容并用换行拼接
        String context = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n---\n"));

        // 3. 构建包含【参考资料】与【用户问题】的 Prompt 模板；

        // 生产级推荐架构写法：
        String systemPrompt = """
                你是一个严谨的电商业务助手。请严格根据下列【参考资料】回答问题。
                资料中未提及的内容直接回复“暂未收录”，严禁编造。
                
                【参考资料】：
                %s
                """.formatted(context);
        // 4. 调用 chatClient 发起流式对话，并通过 advisor 注入 chatId，返回 Flux<String>。
        // -------------------------------------------------------------------------
        return chatClient.prompt().system(systemPrompt).user(prompt).advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)).stream().content().onErrorResume(e -> {
            log.error("AI 流式对话异常，prompt: {}", prompt, e);
            return Flux.just("\n[服务繁忙或调用出错，请稍后重试]");
        });
    }
}
