package com.phoenixhell.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.phoenixhell.common.exception.MyException;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.order.service.OrderItemService;
import com.phoenixhell.gulimall.order.service.OrderService;
import com.phoenixhell.gulimall.order.vo.OrderConfirmVo;
import com.phoenixhell.gulimall.order.vo.OrderSubmitVo;
import com.phoenixhell.gulimall.order.vo.SubmitResponseVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class WebController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/detail.html")
    public String detail() {
        return "detail";
    }

    @GetMapping("/confirm.html")
    public String confirm(Model model) {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("order", orderConfirmVo);
        return "confirm";
    }

    //提交订单
    @PostMapping("/submit")
    public String submit(OrderSubmitVo orderSubmitVo, RedirectAttributes redirectAttributes, Model model) {
        //服务器创建订单，验令牌，验价格，锁库存。。。
        //下单成功来到支付选择页
        //下单失败回到订单确认页 ，重新确认订单信息
        String msg = "";
        SubmitResponseVo responseVo;
        try {
            responseVo = orderService.submit(orderSubmitVo);
            //下单成功 去支付页
            if (responseVo.getCode() == 0) {
                model.addAttribute("responseVo", responseVo);
                return "pay";
            }
        } catch (MyException e) {
            //转成自定义错误 获取错误信息
            msg=e.getMsg();
        } catch (Exception e) {
            msg=e.getMessage();
        }
        redirectAttributes.addFlashAttribute("response", msg);
        return "redirect:http://order.gulimall.com/confirm.html";
    }

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String test(){
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order","hello");
        return "ok";
    }



    @RequestMapping({"/list.html","/"})
    public String listOrderItems(@RequestParam Map<String, Object> params,Model model){
        PageUtils page = orderService.queryListOrderItems(params);
        System.out.println(JSON.toJSONString(page));
        model.addAttribute("page",page);
        return "list";
    }
}
