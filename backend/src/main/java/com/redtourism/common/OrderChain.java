package com.redtourism.common;

import com.redtourism.entity.OrderInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 订单链路统一定义：订单类型、订单状态、状态流转规则，以及链路上的
 * 各类判定（订单不存在 / 无权操作 / 当前状态不允许操作）都集中在该类中。
 * 新增订单类型或调整流转规则时，只需修改本类。
 */
public final class OrderChain {

    private OrderChain() {
    }

    /* ==================== 订单类型 ==================== */

    public static final String TYPE_SPOT = "SPOT";
    public static final String TYPE_ROUTE = "ROUTE";
    public static final String TYPE_HOTEL = "HOTEL";
    public static final String TYPE_FOOD = "FOOD";

    /* ==================== 订单状态 ==================== */

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    /** 订单类型展示信息（新增类型时在此追加一行，用户端与管理端展示同步生效） */
    public static final List<MetaItem> TYPES = Collections.unmodifiableList(Arrays.asList(
            new MetaItem(TYPE_SPOT, "景点", "fa-mountain"),
            new MetaItem(TYPE_ROUTE, "线路", "fa-route"),
            new MetaItem(TYPE_HOTEL, "酒店", "fa-hotel"),
            new MetaItem(TYPE_FOOD, "美食", "fa-utensils")
    ));

    /** 订单状态展示信息（icon 字段恒为 null，状态徽标样式由各前端页面自行决定） */
    public static final List<MetaItem> STATUSES = Collections.unmodifiableList(Arrays.asList(
            new MetaItem(STATUS_PENDING, "待支付", null),
            new MetaItem(STATUS_PAID, "已支付", null),
            new MetaItem(STATUS_COMPLETED, "已完成", null),
            new MetaItem(STATUS_CANCELLED, "已取消", null),
            new MetaItem(STATUS_REFUNDED, "已退款", null)
    ));

    /**
     * 订单动作（状态流转）定义。
     * requiredStatus / deniedMessage 用于用户端流转的前置校验；
     * 管理端为强制流转，不校验前置状态（保持既有行为）。
     */
    public enum Action {
        PAY(STATUS_PENDING, STATUS_PAID, "当前订单状态不可支付"),
        CANCEL(STATUS_PENDING, STATUS_CANCELLED, "当前订单状态不可取消"),
        REFUND(STATUS_PAID, STATUS_REFUNDED, "当前订单状态不可退款"),
        COMPLETE(null, STATUS_COMPLETED, null);

        /** 用户端执行该动作时订单必须处于的状态，null 表示不校验前置状态 */
        private final String requiredStatus;
        /** 动作执行后订单进入的状态 */
        private final String targetStatus;
        /** 前置状态不满足时抛出的提示文案 */
        private final String deniedMessage;

        Action(String requiredStatus, String targetStatus, String deniedMessage) {
            this.requiredStatus = requiredStatus;
            this.targetStatus = targetStatus;
            this.deniedMessage = deniedMessage;
        }

        public String getRequiredStatus() {
            return requiredStatus;
        }

        public String getTargetStatus() {
            return targetStatus;
        }

        public String getDeniedMessage() {
            return deniedMessage;
        }
    }

    /* ==================== 链路判定 ==================== */

    /** 订单必须存在，否则抛出“订单不存在” */
    public static OrderInfo requireExists(OrderInfo order) {
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    /** 订单必须属于当前用户，否则抛出“无权操作此订单” */
    public static void requireOwner(OrderInfo order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
    }

    /** 用户端流转前置校验：订单当前状态必须允许执行该动作 */
    public static void checkTransition(OrderInfo order, Action action) {
        if (action.getRequiredStatus() != null && !action.getRequiredStatus().equals(order.getStatus())) {
            throw new RuntimeException(action.getDeniedMessage());
        }
    }

    /** 应用流转结果：写入目标状态；支付动作额外记录支付方式与支付时间 */
    public static void apply(OrderInfo order, Action action, String payMethod) {
        order.setStatus(action.getTargetStatus());
        if (action == Action.PAY) {
            order.setPayMethod(payMethod);
            order.setPayTime(new Date());
        }
    }

    /** 订单类型/状态展示信息 */
    @Data
    @AllArgsConstructor
    public static class MetaItem {
        private String code;
        private String name;
        private String icon;
    }
}
