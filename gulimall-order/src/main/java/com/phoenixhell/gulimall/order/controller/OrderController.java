package com.phoenixhell.gulimall.order.controller;

import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.phoenixhell.gulimall.order.entity.OrderReturnReasonEntity;
import com.phoenixhell.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * 订单
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:42:06
 */
//@RabbitListener(queues = "phoenixhell-queue")
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 供远程库存查询订单状态
     */
    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable String orderSn){
        OrderEntity orderEntity = orderService.query().eq("order_sn", orderSn).one();
        //模拟远程调用失败
        //int s=10/0;
        return R.ok().put("order",orderEntity);
    }





    //监听mq消息
    //自动监听注解 参数就是接收到的内容
    //参数  Message message  原生消息类型   //class org.springframework.amqp.core.Message
    //参数如果直接写实体类  spring 自动帮我们封装
    //参数3 import com.rabbitmq.client.Channel; 当前传输数据的通道
    //同一个消息只能被一个客户端收到

    //@RabbitListener 可以标注在类和方法
    //@RabbitHandler 只能标注在方法上
    //@RabbitHandler
    void  receiveMessage(Message message, OrderReturnReasonEntity entity, Channel channel){
        System.out.println(message);
        System.out.println(entity);
        //System.out.println(channel);
        //业务处理期间当前消息处理完释放了才可以接受下一个销售
        //try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }


        System.out.println("=================================================");
    }

    //@RabbitHandler
    void  receiveMessage(Message message, String entity, Channel channel) {
        System.out.println(message);
        System.out.println(entity);
        //System.out.println(channel);
        //业务处理期间当前消息处理完释放了才可以接受下一个销售
        //try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) { e.printStackTrace(); }


        //消费端确认回调(通知broker 删除消息) 默认自动确认
        //问题 丢消息：
        //      从channel收到很多消息是是一次性全部ACK的  当消息没有处理完成时候 服务器死机 剩余的消息没有处理
        //      而服务器只要客户端回复了ack 就会删除消息 导致消息丢失
        //解决： 手动确认 处理一个确认一个  没有发送ack 消息为unacked状态
        //而且 consumer死机时候 消息会变成ready状态 新的consumer链接进来后还会继续发送


        //deliveryTag 当前通道内按照消息顺序自增的数字
        // multiple  是否批量确认  false 一个一个手动确认
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if (deliveryTag%2==0) {
                //ack 签收
                channel.basicAck(deliveryTag,false);
            } else {
                //拒签 自己这边处理不了 第三个参数requeue  true 通知服务器 消息重新入队重新发送消息 false 通知服务器删除消息
                channel.basicNack(deliveryTag,false,false);
                //一样的功能   void basicReject(long deliveryTag, boolean requeue)
                //少了可以批量拒绝
               // channel.basicReject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("=================================================");
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        OrderEntity order = orderService.getById(id);
        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody OrderEntity order){
        orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody OrderEntity order){
        orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

