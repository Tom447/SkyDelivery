package com.sky.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.BusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.AddressService;
import com.sky.service.OrdersService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.BeanHelper;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.HistoryOrdersVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        List<HistoryOrdersVO> voList = ordersList.stream()
                .map(order -> {
                    HistoryOrdersVO vo = BeanHelper.copyProperties(order, HistoryOrdersVO.class);
                    vo.setOrderDetailList(detailMap.getOrDefault(order.getId(), Collections.emptyList()));
                    return vo;
                }).collect(Collectors.toList());

        //3.解析并封装结果
        return new PageResult(pageInfo.getTotal(), voList);
    }

}
