package com.frye.trading.controller;

import com.frye.trading.pojo.model.Order;
import com.frye.trading.service.CommodityService;
import com.frye.trading.service.CustomerService;
import com.frye.trading.service.OrderService;
import com.frye.trading.utils.DataJsonUtils;
import com.frye.trading.utils.GenerateIdUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    OrderService orderService;
    @Autowired
    CommodityService commodityService;
    @Autowired
    CustomerService customerService;


    /**
     * 获取订单列表
     * @param page            页码
     * @param limit           每页数据条数
     * @param orderId         订单id
     * @param commodityId     商品id
     * @param buyerId         买方id
     * @param sellerId        卖方id
     * @param state           订单状态
     * @return                order list的json字符串
     */
    @RequestMapping("/op/orderList")
    @ResponseBody
    public String getOrderList(@Param("page") int page, @Param("limit") int limit, @Param("orderId") String orderId, @Param("commodityId") String commodityId,
                               @Param("buyerId") String buyerId, @Param("sellerId") String sellerId, @Param("state") String state) {
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("orderId", orderId);
        params.put("commodityId", commodityId);
        params.put("buyerId", buyerId);
        params.put("sellerId", sellerId);
        params.put("state", state);
        List<Order> orders = orderService.getOrderList(page, limit, params);
        int count = orderService.getCount(params);
        if (count >= 0) {
            dataJsonUtils.setCode(200);
            dataJsonUtils.setMsg("get data successfully");
            dataJsonUtils.setCount(count);
            dataJsonUtils.setData(orders);
        } else {
            dataJsonUtils.setCode(0);
            dataJsonUtils.setMsg("get data failed");
        }
        return dataJsonUtils.toString();
    }

    /**
     * 增加order
     * @param map order信息
     * @return    增加结果
     */
    @RequestMapping("/op/orderAdd")
    @ResponseBody
    public String addOrder(@RequestBody Map<String, String> map) {
        String commodityId = map.get("commodityId");
        String buyerId = map.get("buyerId");
        String sellerId = map.get("sellerId");
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        if (!commodityService.checkCommodity(commodityId)) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("the commodity can not be ordered");
            return dataJsonUtils.toString();
        }
        if (customerService.getCustomerById(buyerId) == null) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("the buyer does not exist");
            return dataJsonUtils.toString();
        }
        if (customerService.getCustomerById(sellerId) == null) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("the seller does not exist");
            return dataJsonUtils.toString();
        }

        Order order = new Order();
        String orderId = GenerateIdUtils.generateOrderID();
        while (orderService.getOrderById(orderId) != null) {
            orderId = GenerateIdUtils.generateOrderID();
        }
        order.setOrderId(orderId);
        order.setCommodityId(commodityId);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setState("Ordered");
        if (orderService.addOrder(order) < 0) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("add order error! please check the data you enter.");
        } else {
            dataJsonUtils.setCode(200);
            dataJsonUtils.setMsg("add order successfully!");
        }
        return dataJsonUtils.toString();
    }

    /**
     * 删除order
     * @return json
     */
    @RequestMapping(value = "/op/orderDelete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteOrder(@RequestBody String[] idList) {
        boolean error = false;
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        for (String id : idList){
            if (orderService.deleteOrder(id) < 0){
                error = true;
            }
        }
        if (error) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("delete error");
        } else {
            dataJsonUtils.setCode(200);
            dataJsonUtils.setMsg("delete successfully!");
        }
        return dataJsonUtils.toString();
    }

    /**
     * 跳转到修改页面
     * @param id 修改的order id
     * @param model 传递参数
     * @return 页面url
     */
    @RequestMapping( "/admin/orderUpdate/{id}")
    public String toUpdatePage(@PathVariable("id") String id, Model model) {
        model.addAttribute(id);
        return "/admin/orderUpdate";
    }

    /**
     * 从后台获取order的信息填充到form中
     * @param id order id
     * @return 返回order的json
     */
    @RequestMapping(value = "/op/getOrder", method = RequestMethod.POST)
    @ResponseBody
    public String getCustomer(@RequestBody String id) {
        Order order = orderService.getOrderById(id);
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        if (order != null) {
            dataJsonUtils.setCode(200);
            dataJsonUtils.setData(order);
            dataJsonUtils.setMsg("Pull data successfully");
        } else {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("Error pulling data");
        }
        return dataJsonUtils.toString();
    }

    /**
     * 修改order
     * @param map order信息
     * @return    修改的结果
     */
    @RequestMapping(value = "/op/orderUpdate", method = RequestMethod.POST)
    @ResponseBody
    public String updateCustomer(@RequestBody Map<String, String> map) {
        DataJsonUtils dataJsonUtils = new DataJsonUtils();
        Order order = new Order();
        order.setOrderId(map.get("orderId"));
        order.setState(map.get("state"));
        order.setBuyerScore(map.get("buyerScore"));
        order.setSellerScore(map.get("buyerScore"));
        if (orderService.updateOrder(order) < 0) {
            dataJsonUtils.setCode(100);
            dataJsonUtils.setMsg("update order error! please check the data you enter.");
        } else {
            dataJsonUtils.setCode(200);
            dataJsonUtils.setMsg("update order successfully!");
        }
        return dataJsonUtils.toString();
    }
}
