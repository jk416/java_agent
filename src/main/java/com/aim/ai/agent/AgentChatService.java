package com.aim.ai.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AgentChatService {

    private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);

    private final ChatClient chatClient;
    private final PromotionService promotionService;

    public AgentChatService(ChatClient chatClient, PromotionService promotionService) {
        this.chatClient = chatClient;
        this.promotionService = promotionService;
    }

    /**
     * Agent 智能体对话（具备自主调用营销发券工具的能力）
     *
     * @param userPrompt 用户自然语言指令（例如：“我是商户 8888，帮我发一张100减20看播满减券”）
     * @return 大模型自主决策执行后的最终回答
     */
    public String agentChat(String userPrompt) {
        if (!StringUtils.hasText(userPrompt)) {
            return "请输入有效的指令";
        }

        // -------------------------------------------------------------------------
        // 【你的核心实战区域】：给大模型赋予“手脚”（挂载工具）
        // 目标：
        // 调用 chatClient.prompt().user(userPrompt)
        // 挂载两个核心工具：
        // 1. queryCoupons 工具：
        //    - 工具名："queryMerchantCoupons"
        //    - 描述："根据商户ID查询该商户当前可领取的优惠券列表"
        //    - 执行函数：promotionService::queryCoupons
        // 2. issueCoupon 工具：
        //    - 工具名："issueMerchantCoupon"
        //    - 描述："为指定商户发放指定名称的营销优惠券"
        //    - 执行函数：promotionService::issueCoupon
        // 3. 执行同步调用 .call().content() 并返回给用户
        // -------------------------------------------------------------------------
        return chatClient.prompt().user(userPrompt)
                .function("queryMerchantCoupons", "根据商户ID查询该商户当前可领取的优惠券列表", promotionService::queryCoupons)
                .function("issueMerchantCoupon", "为指定商户发放指定名称的营销优惠券", promotionService::issueCoupon)
                .call().content();
    }
}
