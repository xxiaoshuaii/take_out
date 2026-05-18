package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServicelmpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    final int PackAmount = 2;

    //配置商家地址
    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
    //检查地址是否为空
    Long addressBookId = ordersSubmitDTO.getAddressBookId();
    AddressBook addressBook = new AddressBook();
    addressBook = addressBookMapper.getById(addressBookId);
    if (addressBook == null) {
        throw new RuntimeException(MessageConstant.ADDRESS_BOOK_IS_NULL);
    }
    //检查地址是否存在
    checkOutOfRange(addressBook.getProvinceName()+ addressBook.getCityName()+ addressBook.getDistrictName()+ addressBook.getDetail());
    //检查购物车数据
    Long userId = BaseContext.getCurrentId();
    ShoppingCart shoppingCart = new ShoppingCart();
    shoppingCart.setUserId(userId);
    List<ShoppingCart> list = shopCartMapper.list(shoppingCart);
    if (list == null || list.size() == 0) {
        throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
    }
    //设置数据
    Orders orders = new Orders();
    BeanUtils.copyProperties(ordersSubmitDTO, orders);
    orders.setUserId(userId);
    orders.setStatus(Orders.PENDING_PAYMENT);
    orders.setPayMethod(Orders.UN_PAID);
    orders.setNumber(String.valueOf(System.currentTimeMillis()));
    orders.setPhone(addressBook.getPhone());
    orders.setAddress(addressBook.getProvinceName()
            + addressBook.getCityName()
            + addressBook.getDistrictName()
            + addressBook.getDetail());
    orders.setConsignee(addressBook.getConsignee());
    orders.setPayMethod(1);
    orders.setOrderTime(LocalDateTime.now());

    orderMapper.insert(orders);

    //订单详情
    List<OrderDetail> orderDetailList =new ArrayList<>();
        for (ShoppingCart cart : list) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orders.getId());
        BeanUtils.copyProperties(cart, orderDetail);
        orderDetailList.add(orderDetail);
    }
    //批量插入订单详情数据
    orderDetailMapper.insertBatch(orderDetailList);

    //清空购物车数据
    shopCartMapper.deleteById(userId);

        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();
        return submitVO;
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
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        //由于支付测试，暂时写死
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端推送消息  JOSN（type：类型1来单 类型2催单 orderId  content 订单号）
        HashMap map = new HashMap();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content","订单号："+ orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 用户端订单查询
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders order = orderMapper.getOrder(id);
        Long OrderId = order.getId();
        List<OrderDetail> orderDetail = orderDetailMapper.getOrderDetail(OrderId);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setPackAmount(PackAmount);
        orderVO.setOrderDetailList(orderDetail);
        return orderVO;
    }

    /**
     * 历史订单查询
     * @param page
     * @return
     */
    @Override
    public PageResult pageQueryUser(OrdersPageQueryDTO page) {
        PageHelper.startPage(page.getPage(), page.getPageSize());
        page.setUserId(BaseContext.getCurrentId());
        Page<Orders> pageResult = orderMapper.pageQueryUser(page);
        List<OrderVO> list = new ArrayList<>();
        for (Orders orders : pageResult) {
            OrderVO orderVO = new OrderVO();
            Long OrderId = orders.getId();
            BeanUtils.copyProperties(orders, orderVO);
            List<OrderDetail> orderDetail = orderDetailMapper.getOrderDetail(OrderId);
            orderVO.setOrderDetailList(orderDetail);
            list.add(orderVO);
        }
        return new PageResult(pageResult.getTotal(), list);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) throws Exception {
    Orders orders = orderMapper.getOrder(id);
    //判断订单是否可以直接退款
    if(orders.getStatus() >= Orders.CONFIRMED)
    {
     throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
    }

    //订单取消
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
    Long userId = BaseContext.getCurrentId();
    List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetail(id);
    //利用stream流来遍历集合因为集合的属性不相同，所以利用stream流来遍历集合
    List<ShoppingCart> list =orderDetailList.stream().map(x -> {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(x, shoppingCart, "id");
        shoppingCart.setUserId(userId);
        shoppingCart.setCreateTime(LocalDateTime.now());
        return shoppingCart;
    }).collect(Collectors.toList());

    shopCartMapper.insertBatch(list);
}

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.pageQueryUser(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 统计订单数据
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 订单确认
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
    Orders orders = Orders.builder()
            .id(ordersConfirmDTO.getId())
            .status(Orders.CONFIRMED)
            .build();
    orderMapper.update(orders);
    }

    /**
     * 订单拒绝
     * @param rejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO rejectionDTO) {
    Orders orders =orderMapper.getOrder(rejectionDTO.getId());
    //判断订单状态是否为待接单
    if(orders.getStatus() != Orders.TO_BE_CONFIRMED)
    {
     throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
    }
    //订单取消
    orders.setStatus(Orders.CANCELLED);
    orders.setRejectionReason(rejectionDTO.getRejectionReason());
    orders.setCancelTime(LocalDateTime.now());
    //调用微信支付退款接口（由于支付接口未开通直接将支付状态改为退款）
    orders.setPayStatus(Orders.REFUND);
    orderMapper.update(orders);
    }

    /**
     * 管理端取消订单
     * @param orderCancelDTO
     */
    @Override
    public void cancelAdmin(OrdersCancelDTO orderCancelDTO) {
        Orders orders =orderMapper.getOrder(orderCancelDTO.getId());
        //判断订单状态是否为已经完成
        if(orders.getStatus() == Orders.COMPLETED){
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单取消
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(orderCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        //调用微信支付退款接口（由于支付接口未开通直接将支付状态改为退款）
        orders.setPayStatus(Orders.REFUND);
        orderMapper.update(orders);
    }

    /**
     * 订单派送
     * @param id
     */
    @Override
    public void delivery(Long id) {
     Orders orders =orderMapper.getOrder(id);
     //判断订单状态是否为待派送
     if(orders.getStatus() != Orders.CONFIRMED)
     {
     throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
     }
     //订单派送
     orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
     orderMapper.update(orders);
    }

    /**
     * 订单完成
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders =orderMapper.getOrder(id);
        //判断订单状态是否为派送中
        if(orders.getStatus() != Orders.DELIVERY_IN_PROGRESS)
        {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单派送完成
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    /**
     * 订单催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getOrder(id);
        //通过websocket向客户端推送消息  JOSN（type：类型1来单 类型2催单 orderId  content 订单号）
        HashMap map = new HashMap();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号："+ orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }


    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetail(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 10000){
            //配送距离超过10000米
            throw new OrderBusinessException("超出配送范围");
        }
    }
}

