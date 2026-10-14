package com.aim.ai.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class MerchantAuditMcpService {

    private static final Logger log = LoggerFactory.getLogger(MerchantAuditMcpService.class);

    /**
     * 模拟商户进件审核状态机领域对象（对应简历里的 AimMerchantEntryAuditMainDO）
     */
    public record MerchantAuditState(
            String merchantId,
            String auditStatus, // PENDING(待审核), REJECTED(已驳回), APPROVED(已通过)
            String rejectReason,
            String auditTime
    ) {}

    /**
     * MCP Tool 入参
     */
    public record AuditStatusQueryRequest(String merchantId) {}

    /**
     * MCP Resource 资源元数据对象
     */
    public record McpResourceResponse(String uri, String name, String mimeType, String content) {}

    /**
     * 1. 标准化 MCP Tool：查询商户进件状态机当前节点与驳回原因
     *
     * @param request 请求入参
     * @return 状态机领域数据
     */
    public MerchantAuditState queryAuditStatus(AuditStatusQueryRequest request) {
        log.info("【MCP Tool 调用】收到商户状态机核验指令, merchantId={}", request.merchantId());

        // -------------------------------------------------------------------------
        // 【你的核心实战区域 1】：状态机查询与自愈防御
        // 目标：
        // 1. 如果 merchantId 为空，返回非法提示状态；
        // 2. 模拟根据 merchantId 返回状态机真实数据：
        //    - 若 merchantId 是 "9999"，模拟返回 REJECTED(驳回)，驳回原因为 "营业执照经营范围与所选主营类目不符，缺少食品经营许可证"；
        //    - 其它商户ID，模拟返回 APPROVED(已通过)，驳回原因留空；
        // -------------------------------------------------------------------------

        // TODO: 请替换下方的 return 语句，写下你的核心实现
        return new MerchantAuditState(request.merchantId(), "UNKNOWN", "未查询到商户进件记录", "");
    }

    /**
     * 2. 标准化 MCP Resource：暴露只读资质审核标准规则字典
     *
     * @return MCP 统一资源结构
     */
    public McpResourceResponse getAuditStandardsResource() {
        log.info("【MCP Resource 读取】跨系统拉取商户资质审核规则协议字典");

        // -------------------------------------------------------------------------
        // 【你的核心实战区域 2】：构建 MCP 统一只读资源
        // 目标：
        // 返回 McpResourceResponse：
        // - uri: "merchant://audit/standards"
        // - name: "商家入驻资质标准核验手册"
        // - mimeType: "text/markdown"
        // - content: "1. 营业执照有效期必须大于3个月\n2. 涉及食品类目必须提供《食品经营许可证》\n3. 法人身份证正反面必须清晰无反光"
        // -------------------------------------------------------------------------

        // TODO: 请替换下方的 return 语句，写下你的核心实现
        return new McpResourceResponse("merchant://audit/standards", "资质标准", "text/plain", "");
    }
}
