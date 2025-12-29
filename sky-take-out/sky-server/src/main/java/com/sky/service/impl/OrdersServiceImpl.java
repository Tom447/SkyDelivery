package com.sky.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.BusinessException;
import com.sky.mapper.*;
import com.sky.properties.SkyProperties;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.BaiduDirectionUtil;
import com.sky.utils.BeanHelper;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrdersDetailVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {


    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrdersDetailMapper ordersDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private SkyProperties skyProperties;
    @Autowired
    private BaiduDirectionUtil baiduDirectionUtil;


    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //0. 查询收货地址信息，地址为空，不能下单
        AddressBook addressBook = addressMapper.getAddressById(ordersSubmitDTO.getAddressBookId());
        if (Objects.isNull(addressBook)) {
            throw new BusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //0. 购物车数据为空也不能下单
        ShoppingCart shoppingCartCondition = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
        // 得到购物车数据列表
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCartCondition);
        if (CollectionUtils.isEmpty(shoppingCartList)) {
            throw new BusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //1.保存订单数据
        Orders orders = BeanHelper.copyProperties(ordersSubmitDTO, Orders.class);
        orders.setNumber(String.valueOf(System.nanoTime()));//idworker雪花算法,订单编号
        orders.setStatus(Orders.ORDER_STAUTS_PENDING_PAYMENT);//代付款
        orders.setUserId(BaseContext.getCurrentId());//订单的用户
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.PAY_STATUS_UN_PAID);//未支付


        //收货人
        orders.setPhone(addressBook.getPhone());//手机号
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());//收获人

        ordersMapper.insert(orders);

        //2.保存订单明细数据. --来源于购物车
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(cart -> {
            //拷贝属性
            OrderDetail orderDetail = BeanHelper.copyProperties(cart, OrderDetail.class);
            //赋值订单ID
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        ordersDetailMapper.insertBatch(orderDetailList);
        //3.清空用户的购物车数据
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        //4.组装数据并返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId()).orderTime(orders.getOrderTime()).orderNumber(orders.getNumber()).orderAmount(orders.getAmount()).build();


        //TODO...用户下单判断配送距离是否超了
        /**
         * 1.得到shop、user各自的经纬度location1、 location2
         * 2.通过location1和location2得到距离distance
         * 3.判断distance是否超过5公里，超过5公里抛异常
         */
//        1.得到shop、user各自的经纬度location1、 location2
        String shopAddressName = skyProperties.getShop().toString();
        String userAddressName = orders.getAddress();
        BaiduLocation shopLocation = baiduDirectionUtil.getAddrOfLatAndLng(shopAddressName);
        BaiduLocation userLocation = baiduDirectionUtil.getAddrOfLatAndLng(userAddressName);
//        2.通过location1和location2得到距离distance
        Integer distance = baiduDirectionUtil.getDirecirectionLite(shopLocation.getLoation(), userLocation.getLoation());
        if (distance > skyProperties.getDistance().getDistanceByM()){
            throw new BusinessException("超出配送距离");
        }
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new BusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        log.info("✅ 生成订单{}", vo.toString());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder().id(ordersDB.getId()).status(Orders.ORDER_STAUTS_TO_BE_CONFIRMED).payStatus(Orders.PAY_STATUS_PAID).checkoutTime(LocalDateTime.now()).build();

        ordersMapper.update(orders);
        // ✅ 学习专用】打印日志 + 抛出运行时异常（仅用于打断和观察）
        log.info("✅ 模拟支付成功");
    }

    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {

        //1.设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 2. 查询主订单（自动分页）
        Orders ordersCondition = Orders.builder().status(ordersPageQueryDTO.getStatus()).build();
        List<Orders> ordersList = ordersMapper.list(ordersCondition);
        // 获取原始的 PageInfo（包含 total）

        PageInfo<Orders> pageInfo = new PageInfo<>(ordersList);
        // 3. 如果没有订单，直接返回空
        if (ordersList.isEmpty()) {
            return new PageResult(0L, Collections.emptyList());
        }

        // 4. 提取所有订单 ID
        List<Long> orderIds = ordersList.stream()
                .map(Orders::getId)
                .collect(Collectors.toList());


        // 5. 批量查询所有订单明细
        List<OrderDetail> orderDetails = ordersDetailMapper.getOrderDetailsByOrderIds(orderIds);

        // 6. 按 orderId 分组：Map<orderId, List<OrderDetail>>
        Map<Long, List<OrderDetail>> detailMap = orderDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 7. 构建 VO 列表
        List<OrdersDetailVO> voList = ordersList.stream()
                .map(order -> {
                    OrdersDetailVO vo = BeanHelper.copyProperties(order, OrdersDetailVO.class);
                    vo.setOrderDetailList(detailMap.getOrDefault(order.getId(), Collections.emptyList()));
                    return vo;
                }).collect(Collectors.toList());

        //3.解析并封装结果
        return new PageResult(pageInfo.getTotal(), voList);
    }


    @Override
    public OrdersDetailVO getOrdersDetailById(Long id) {

        Orders orderCondition = Orders.builder().id(id).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        if (list.isEmpty()) {
            throw new BusinessException("没有对应订单");
        }
        Orders order = list.get(0);

        List<OrderDetail> orderDetailsList = ordersDetailMapper.getOrderDetailsByOrderIds(Arrays.asList(order.getId()));
        OrdersDetailVO ordersDetailVO = BeanHelper.copyProperties(order, OrdersDetailVO.class);
        ordersDetailVO.setOrderDetailList(orderDetailsList);
        return ordersDetailVO;
    }

    @Override
    public void cancel(Long id) {
        Orders orderCondition = Orders.builder().id(id).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        if (list.isEmpty()) {
            throw new BusinessException("没有对应订单");
        }
        Orders order = list.get(0);
        order.setStatus(Orders.ORDER_STAUTS_CANCELLED);

        ordersMapper.update(order);
    }

    @Override
    @Transactional
    public void again(Long orderId) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查询历史订单
        Orders oldOrder = ordersMapper.getById(orderId);
        if (oldOrder == null || !oldOrder.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 2. 查询该订单的明细
        List<OrderDetail> orderDetailList = ordersDetailMapper.getByOrderId(orderId);
        if (CollectionUtils.isEmpty(orderDetailList)) {
            throw new BusinessException("订单无商品明细，无法再来一单");
        }

        // 3. 获取当前用户默认地址（用于购物车关联，部分系统需要）
        AddressBook defaultAddress = addressMapper.getDefaultAddressBook();
        Long addressBookId = (defaultAddress != null) ? defaultAddress.getId() : null;

        // 4. 将每个订单明细转换为购物车项，并批量插入
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(detail -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setDishId(detail.getDishId());
            cart.setSetmealId(detail.getSetmealId());
            cart.setName(detail.getName());
            cart.setImage(detail.getImage());
            cart.setDishFlavor(detail.getDishFlavor());
            cart.setNumber(detail.getNumber()); // 数量复用
            cart.setAmount(detail.getAmount());  // 单价复用（或可查最新价格）
            cart.setCreateTime(LocalDateTime.now());
            return cart;
        }).collect(Collectors.toList());

        // 5. 批量插入购物车（注意：你的 ShoppingCartMapper 需支持 insertBatch）
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public OrderStatisticsVO statistics() {
        /**
         * confirmed 待派送数量
         * deliveryInProgress 派送中数量
         * toBeConfirmed 待接单数量
         */
        //获得全部订单数据
        List<Orders> list = ordersMapper.list(null);

        Integer confirmed = 0;
        Integer deliveryInProgress = 0;
        Integer toBeConfirmed = 0;
        for (Orders order : list) {
            switch (order.getStatus()) {
                case 2:
                    toBeConfirmed++;
                    break;
                case 3:
                    confirmed++;
                    break;
                case 4:
                    deliveryInProgress++;
                    break;
            }
        }

        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orderCondition = Orders.builder().id(ordersConfirmDTO.getId()).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        //订单只有状态为2（待接单）时才可以接单
        if (list.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders = list.get(0);

        if (orders.getStatus() == Orders.ORDER_STAUTS_TO_BE_CONFIRMED) {//当前订单存在且状态为2
            orders.setStatus(Orders.ORDER_STAUTS_CONFIRMED);
            ordersMapper.update(orders);
        } else {//不满足接单条件
            return;
        }
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orderCondition = Orders.builder().id(ordersRejectionDTO.getId()).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        //判断订单是否存在
        if (list.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders = list.get(0);
        //如果当前订单的支付状态为已支付，拒单需要退款
        if (orders.getPayStatus() == Orders.PAY_STATUS_PAID) {
            try {
                weChatPayUtil.refund(
                        orders.getNumber(),
                        "REFUND_" + System.currentTimeMillis(),
                        orders.getAmount(),
                        orders.getAmount()
                );
                // 退款成功，更新订单状态
                orders.setPayStatus(Orders.PAY_STATUS_REFUND);
            } catch (Exception e) {
                log.error("退款失败", e);
                throw new BusinessException("退款失败，请联系客服");
            }
        }
        //更新订单状态为已取消
        orders.setStatus(Orders.ORDER_STAUTS_CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        ordersMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        /**
         * - 校验订单是否存在
         * - 如果订单的支付状态为 **已支付** , 是需要退款的 (调用weChatUtil.refund方法退款)， 且将支付状态改为 **退款**
         * - 更新订单的状态为 **已取消**
         */
        Orders orderCondition = Orders.builder().id(ordersCancelDTO.getId()).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        //判断订单是否存在
        if (list.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders = list.get(0);
        //如果当前订单的支付状态为已支付，拒单需要退款
        if (orders.getPayStatus() == Orders.PAY_STATUS_PAID) {
            try {
                weChatPayUtil.refund(
                        orders.getNumber(),
                        "REFUND_" + System.currentTimeMillis(),
                        orders.getAmount(),
                        orders.getAmount()
                );
                // 退款成功，更新订单状态
                orders.setPayStatus(Orders.PAY_STATUS_REFUND);
            } catch (Exception e) {
                log.error("退款失败", e);
                throw new BusinessException("退款失败，请联系客服");
            }
        }
        //更新订单状态为已取消
        orders.setStatus(Orders.ORDER_STAUTS_CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        ordersMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        /**
         * - 判断订单是否存在 以及 订单的状态是否正确 （只有订单状态为 **已接单** 的订单， 才可以进行派送）
         * - 更新订单的状态为 **派送中**
         */
        Orders orderCondition = Orders.builder().id(id).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        //判断订单是否存在
        if (list.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders = list.get(0);
        //只有订单状态为已接单才可进行配送
        if (orders.getStatus() == Orders.ORDER_STAUTS_CONFIRMED) {
            //更新派送状态为派送中
            orders.setStatus(Orders.ORDER_STAUTS_DELIVERY_IN_PROGRESS);
            ordersMapper.update(orders);
        }
    }

    @Override
    public void complete(Long id) {
        /**
         * - 判断订单是否存在 以及 订单的状态是否正确 （只有订单状态为 **派送中** 的订单， 才可以进行完成订单）
         * //        - 更新订单的状态为 **已完成**
         */
        Orders orderCondition = Orders.builder().id(id).build();
        List<Orders> list = ordersMapper.list(orderCondition);
        //判断订单是否存在
        if (list.isEmpty()) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders orders = list.get(0);
        if (orders.getStatus() == Orders.ORDER_STAUTS_DELIVERY_IN_PROGRESS) {//只有订单状态为 **派送中** 的订单， 才可以进行完成订单
//            更新订单的状态为 **已完成**
            orders.setStatus(Orders.ORDER_STAUTS_COMPLETED);
            ordersMapper.update(orders);
        }
    }
}
