package com.redtourism.common.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * 订单操作：下单后用户与后台对订单执行的动作。
 * <p>
 * 每个操作在此一处声明它的目标状态、允许发起的起始状态以及状态不符时的提示语，
 * 用户侧取消 / 支付 / 退款共用同一套状态机判断，不再各处各写一遍。
 * 规则需要因订单类型而不同时，可在 {@link OrderType} 中重写
 * {@link OrderType#allowedActions()} 覆盖默认规则。
 */
public enum OrderAction {

    /** 用户取消：仅待支付订单可取消。 */
    CANCEL("取消", OrderStatus.CANCELLED, "当前订单状态不可取消", OrderStatus.PENDING),
    /** 用户支付：仅待支付订单可支付。 */
    PAY("支付", OrderStatus.PAID, "当前订单状态不可支付", OrderStatus.PENDING),
    /** 用户退款：仅已支付订单可退款。 */
    REFUND("退款", OrderStatus.REFUNDED, "当前订单状态不可退款", OrderStatus.PAID),
    /** 后台完成：已支付订单由后台标记完成。 */
    COMPLETE("完成", OrderStatus.COMPLETED, "当前订单状态不可完成", OrderStatus.PAID);

    private final String label;
    private final OrderStatus targetStatus;
    private final String invalidMessage;
    private final Set<OrderStatus> allowedSources;

    OrderAction(String label, OrderStatus targetStatus, String invalidMessage, OrderStatus... allowedSources) {
        this.label = label;
        this.targetStatus = targetStatus;
        this.invalidMessage = invalidMessage;
        EnumSet<OrderStatus> sources = EnumSet.noneOf(OrderStatus.class);
        for (OrderStatus source : allowedSources) {
            sources.add(source);
        }
        this.allowedSources = sources;
    }

    public String getLabel() {
        return label;
    }

    public OrderStatus getTargetStatus() {
        return targetStatus;
    }

    public String getInvalidMessage() {
        return invalidMessage;
    }

    /**
     * 当前状态是否允许执行本操作。无法识别的历史状态一律不允许，与原字面量判断一致。
     */
    public boolean isAllowedFrom(OrderStatus current) {
        return current != null && allowedSources.contains(current);
    }
}
