package com.aim.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * 构建核心 ChatClient Bean
     * 遵循 Spring AI 推荐的 Builder 模式，便于后续追加 Advisors (如记忆、拦截、审计等)
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,ChatMemory chatMemory) {
        return builder
                .defaultSystem("你是一个资深的电商营销与商家服务中台智能助手，回答专业、严谨且具备技术深度。")
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }
}
