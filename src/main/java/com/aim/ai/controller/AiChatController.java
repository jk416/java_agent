package com.aim.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final ChatClient chatClient;

    public AiChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam("prompt") String prompt,@RequestParam("chatId") String chatId) {
        if (!StringUtils.hasText(prompt)) {
            return Flux.just("请输入有效的提问内容");
        }
        if (!StringUtils.hasText(chatId)) {
            chatId = UUID.randomUUID().toString();
        }
        String finalChatId = chatId;
        return chatClient.prompt().user(prompt).advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, finalChatId)).stream().content().onErrorResume(e -> {
            log.error("AI 流式对话异常，prompt: {}", prompt, e);
            return Flux.just("\n[服务繁忙或调用出错，请稍后重试]");
        });
    }


}
