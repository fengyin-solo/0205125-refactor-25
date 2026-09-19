package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.common.OrderChain;
import com.redtourism.entity.OrderInfo;
import com.redtourism.mapper.OrderInfoMapper;
import com.redtourism.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Override
    public OrderInfo createOrder(OrderInfo order) {
        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setStatus(OrderChain.STATUS_PENDING);
        save(order);
        return order;
    }

    @Override
    public boolean cancelOrder(Long orderId, Long userId) {
        return transitionByUser(orderId, userId, OrderChain.Action.CANCEL, null);
    }

    @Override
    public boolean payOrder(Long orderId, String payMethod, Long userId) {
        return transitionByUser(orderId, userId, OrderChain.Action.PAY, payMethod);
    }

    @Override
    public boolean refundOrder(Long orderId, Long userId) {
        return transitionByUser(orderId, userId, OrderChain.Action.REFUND, null);
    }

    @Override
    public boolean transitionByAdmin(Long orderId, OrderChain.Action action) {
        // 管理端强制流转：仅校验订单存在，不校验归属与前置状态（保持既有行为）
        OrderInfo order = OrderChain.requireExists(getById(orderId));
        OrderChain.apply(order, action, null);
        return updateById(order);
    }

    /** 用户端流转：订单存在 → 归属当前用户 → 前置状态校验 → 应用流转 */
    private boolean transitionByUser(Long orderId, Long userId, OrderChain.Action action, String payMethod) {
        OrderInfo order = OrderChain.requireExists(getById(orderId));
        OrderChain.requireOwner(order, userId);
        OrderChain.checkTransition(order, action);
        OrderChain.apply(order, action, payMethod);
        return updateById(order);
    }

    @Override
    public IPage<OrderInfo> listUserOrders(int page, int size, Long userId, String orderType, String status) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId, userId);
        if (StringUtils.hasText(orderType)) {
            wrapper.eq(OrderInfo::getOrderType, orderType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<OrderInfo> listAllOrders(int page, int size, String orderType, String status) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
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
