package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.common.enums.OrderAction;
import com.redtourism.common.enums.OrderStatus;
import com.redtourism.common.enums.OrderType;
import com.redtourism.entity.OrderInfo;
import com.redtourism.mapper.OrderInfoMapper;
import com.redtourism.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Override
    public OrderInfo createOrder(OrderInfo order) {
        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setStatus(OrderStatus.PENDING.getCode());
        save(order);
        return order;
    }

    @Override
    public boolean cancelOrder(Long orderId, Long userId) {
        return executeUserAction(orderId, userId, OrderAction.CANCEL, null);
    }

    @Override
    public boolean payOrder(Long orderId, String payMethod, Long userId) {
        return executeUserAction(orderId, userId, OrderAction.PAY, payMethod);
    }

    @Override
    public boolean refundOrder(Long orderId, Long userId) {
        return executeUserAction(orderId, userId, OrderAction.REFUND, null);
    }

    @Override
    public IPage<OrderInfo> listUserOrders(int page, int size, Long userId, String orderType, String status) {
        return pageOrders(page, size, userId, orderType, status);
    }

    @Override
    public IPage<OrderInfo> listAllOrders(int page, int size, String orderType, String status) {
        return pageOrders(page, size, null, orderType, status);
    }

    @Override
    public boolean forceChangeStatus(Long orderId, OrderAction action) {
        OrderInfo order = requireOrder(orderId);
        order.setStatus(action.getTargetStatus().getCode());
        return updateById(order);
    }

    // ==================== 订单链路的内部判断，全部集中在此 ====================

    /**
     * 取订单，不存在时统一抛出"订单不存在"，用户侧与后台侧共用此判断。
     */
    private OrderInfo requireOrder(Long orderId) {
        OrderInfo order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    /**
     * 取订单并校验归属，"订单不存在"、"无权操作此订单"的判断只此一处。
     */
    private OrderInfo requireOwnedOrder(Long orderId, Long userId) {
        OrderInfo order = requireOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        return order;
    }

    /**
     * 用户侧取消 / 支付 / 退款的统一链路：
     * 订单存在 → 归属当前用户 → 当前状态与订单类型允许该操作 → 按操作声明流转到目标状态。
     * 不允许时的提示语由 {@link OrderAction} 统一提供。
     */
    private boolean executeUserAction(Long orderId, Long userId, OrderAction action, String payMethod) {
        OrderInfo order = requireOwnedOrder(orderId, userId);
        OrderStatus currentStatus = OrderStatus.fromCode(order.getStatus());
        OrderType orderType = OrderType.fromCode(order.getOrderType());
        boolean typeAllows = orderType == null || orderType.allows(action);
        if (!action.isAllowedFrom(currentStatus) || !typeAllows) {
            throw new RuntimeException(action.getInvalidMessage());
        }
        order.setStatus(action.getTargetStatus().getCode());
        if (action == OrderAction.PAY) {
            order.setPayMethod(payMethod);
            order.setPayTime(new Date());
        }
        return updateById(order);
    }

    /**
     * 用户列表（带 userId）与后台列表（不带 userId）共用同一段筛选条件。
     */
    private IPage<OrderInfo> pageOrders(int page, int size, Long userId, String orderType, String status) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(OrderInfo::getUserId, userId);
        }
        if (StringUtils.hasText(orderType)) {
            wrapper.eq(OrderInfo::getOrderType, orderType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}
