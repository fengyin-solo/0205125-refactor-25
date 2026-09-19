package com.redtourism.common.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * 订单类型注册表：系统支持的全部订单类型在此一处声明。
 * <p>
 * 普通订单（景点 / 线路 / 美食）与酒店预订目前共用同一套下单与状态流转规则，
 * 因此各类型默认继承 {@link OrderAction} 中定义的状态机。
 * 新增一种订单类型时，只需在此增加一个常量（常量名即 order_info.order_type 的类型码）；
 * 若该类型的可执行动作集合与默认不同（例如某类订单不允许退款），只需重写
 * {@link #allowedActions()}，下单、支付、取消、退款及列表筛选的链路代码均无需改动。
 */
public enum OrderType {

    /** 景点预约（含免费景点预约）。 */
    SPOT,
    /** 线路预订。 */
    ROUTE,
    /** 酒店预订。 */
    HOTEL,
    /** 美食下单。 */
    FOOD;

    /**
     * 该类型允许执行的操作，默认沿用全部通用操作。
     * 特殊订单类型重写此方法即可收紧或放开链路判断，不需要改动 Service / Controller。
     */
    public Set<OrderAction> allowedActions() {
        return EnumSet.allOf(OrderAction.class);
    }

    /**
     * 该类型是否允许执行指定操作。
     */
    public boolean allows(OrderAction action) {
        return allowedActions().contains(action);
    }

    /**
     * 按持久化的类型码解析；未注册的类型码返回 {@code null}，保持对存量数据的宽容。
     */
    public static OrderType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OrderType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
