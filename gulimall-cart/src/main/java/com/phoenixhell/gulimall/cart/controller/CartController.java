package com.phoenixhell.gulimall.cart.controller;

import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.cart.service.CartService;
import com.phoenixhell.gulimall.cart.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    //远程调用接口 获取当前登录用户的所有选择的购物项目
    @ResponseBody
    @GetMapping("/user/cartItem/checked")
    public List<Cart.CartItem> getCartItem(){
        List<Cart.CartItem> cartItems=cartService.getCheckedCartItems();
        return cartItems;
    }




    //京东 保存有一个在cookie中 user-key 来识别没有登录的用户    保存期限一个月
    @GetMapping({"/", "/cart.html"})
    public String cartListPage(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }


    //清空购物车
    @ResponseBody
    @GetMapping("/cart/clear")
    public R clearCart() {
        cartService.clearCart();
        return R.ok();
    }

    //删除购物车的一些
    @ResponseBody
    @PostMapping("/cart/delete")
    public R deleteCartItems(@RequestParam("skuIds") String[] skuIds) {
        System.out.println(skuIds);
        cartService.deleteCartItems(skuIds);
        return R.ok();
    }

    //改变购物车checkd的状态
    @ResponseBody
    @PostMapping("/checkCart")
    public R check(@RequestParam("isChecked") String isChecked,@RequestParam("skuIds") List<String> skuIds) {
        cartService.check(isChecked,skuIds);
        return R.ok();
    }

    @ResponseBody
    @GetMapping("/cartList/json")
    public Cart cartJson() {
        Cart cart = cartService.getCart();
        return cart;
    }

    @ResponseBody
    @PostMapping("/addCount")
    public R addCount(@RequestParam("skuId") String skuId,@RequestParam("count") Integer count) {
        cartService.addCount(skuId,count);
        return R.ok();
    }

    //添加商品到购物车
    @GetMapping("/addToCard")
    public String addToCard(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(skuId, num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //这边不返回数据 重定向的时候返回查询即可  防止多次刷新增加数量
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCardSuccess.html";
    }

    @GetMapping("/addToCardSuccess.html")
    public String addToCardSuccess(@RequestParam("skuId") Long skuId, Model model) {
        Cart.CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }

}
