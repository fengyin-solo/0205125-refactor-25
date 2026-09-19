package com.redtourism.common.enums;

/**
 * 订单状态：订单生命周期中的全部状态。
 * <p>
 * {@code code} 即 order_info.status 中持久化的值，也是用户订单列表与后台订单详情
 * 共同使用的状态词汇，已有常量的 code 不可调整，以保证历史订单展示不变。
 */
public enum OrderStatus {

    PENDING("PENDING", "待支付"),
    PAID("PAID", "已支付"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String label;

    OrderStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 按持久化的状态码解析；遇到无法识别的历史状态码时返回 {@code null}，由调用方按既有规则处理。
     */
    public static OrderStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
