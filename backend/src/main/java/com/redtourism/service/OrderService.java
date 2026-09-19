package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.common.enums.OrderAction;
import com.redtourism.entity.OrderInfo;

public interface OrderService extends IService<OrderInfo> {
    OrderInfo createOrder(OrderInfo order);
    boolean cancelOrder(Long orderId, Long userId);
    boolean payOrder(Long orderId, String payMethod, Long userId);
    boolean refundOrder(Long orderId, Long userId);
    IPage<OrderInfo> listUserOrders(int page, int size, Long userId, String orderType, String status);
    IPage<OrderInfo> listAllOrders(int page, int size, String orderType, String status);

    /**
     * 后台订单操作：按操作声明的目标状态强制流转，不做归属与起始状态校验，
     * 与后台原先的处理方式保持一致；订单不存在时统一抛出"订单不存在"。
     */
    boolean forceChangeStatus(Long orderId, OrderAction action);
}
