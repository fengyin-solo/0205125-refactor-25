package com.redtourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.common.Constants;
import com.redtourism.common.OrderChain;
import com.redtourism.common.Result;
import com.redtourism.entity.OrderInfo;
import com.redtourism.entity.User;
import com.redtourism.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/create")
    public Result<OrderInfo> create(@RequestParam String orderType,
                                     @RequestParam Long targetId,
                                     @RequestParam String targetName,
                                     @RequestParam BigDecimal amount,
                                     @RequestParam(defaultValue = "1") Integer quantity,
                                     @RequestParam(required = false) String checkInDate,
                                     @RequestParam(required = false) String checkOutDate,
                                     HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        OrderInfo order = new OrderInfo();
        order.setUserId(user.getId());
        order.setOrderType(orderType);
        order.setTargetId(targetId);
        order.setTargetName(targetName);
        order.setAmount(amount);
        order.setQuantity(quantity);
        return Result.success("下单成功", orderService.createOrder(order));
    }

    @GetMapping("/cancel")
    public Result<String> cancel(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        orderService.cancelOrder(orderId, user.getId());
        return Result.success("取消成功", null);
    }

    @GetMapping("/pay")
    public Result<String> pay(@RequestParam Long orderId,
                               @RequestParam String payMethod,
                               HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        orderService.payOrder(orderId, payMethod, user.getId());
        return Result.success("支付成功（模拟）", null);
    }

    @GetMapping("/refund")
    public Result<String> refund(@RequestParam Long orderId, HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        orderService.refundOrder(orderId, user.getId());
        return Result.success("退款成功（模拟）", null);
    }

    @GetMapping("/myList")
    public Result<IPage<OrderInfo>> myList(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String orderType,
                                            @RequestParam(required = false) String status,
                                            HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(orderService.listUserOrders(page, size, user.getId(), orderType, status));
    }

    @GetMapping("/detail")
    public Result<OrderInfo> detail(@RequestParam Long id) {
        return Result.success(orderService.getById(id));
    }

    /**
     * 订单元数据：订单类型与订单状态的展示信息。
     * 用户端与管理端共用此数据源，新增订单类型只需调整 OrderChain。
     */
    @GetMapping("/meta")
    public Result<Map<String, Object>> meta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("types", OrderChain.TYPES);
        meta.put("statuses", OrderChain.STATUSES);
        return Result.success(meta);
    }
}
